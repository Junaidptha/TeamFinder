package org.teamfinder.Service;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.teamfinder.Entity.ProjectEntry;
import org.teamfinder.Entity.User;
import org.teamfinder.Repository.ProjectRepository;
import org.teamfinder.Repository.UserRepository;
import org.teamfinder.dto.RecommendationResponse;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Skill-based recommendations.
 *
 * <pre>
 *   matchScore = |user ∩ project| / |project|
 *   jaccard    = |user ∩ project| / |user ∪ project|
 *   finalScore = 0.7 * matchScore + 0.3 * jaccard   (+ 0.05 if the project is newer than 14 days)
 * </pre>
 * The final score is capped at 1.0 and exposed as an integer 0-100.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    static final double MATCH_WEIGHT = 0.7;
    static final double JACCARD_WEIGHT = 0.3;
    static final double RECENCY_BOOST = 0.05;
    static final int RECENCY_DAYS = 14;
    /** Upper bound of candidates loaded from MongoDB per request. */
    static final int MAX_CANDIDATES = 1000;

    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final Clock clock;

    // ------------------------------------------------------------------ projects for a user

    public Page<RecommendationResponse> recommendProjects(String userName, Pageable pageable) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<String> skills = SkillNormalizer.normalize(user.getSkills());
        if (skills.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Add skills to your profile first (PUT /api/user/skills) to get recommendations");
        }
        Set<String> userSkills = new LinkedHashSet<>(skills);

        // Pre-filter in MongoDB: only projects sharing at least one technology, minus excluded ones
        Query query = new Query(
                Criteria.where("technologies").in(skills)
                        .and("userName").ne(userName)
                        .and("members").ne(userName)
                        .and("joinRequests").ne(userName))
                .with(Sort.by(Sort.Direction.DESC, "date"))
                .limit(MAX_CANDIDATES);

        List<ProjectEntry> candidates = mongoTemplate.find(query, ProjectEntry.class);
        LocalDateTime now = LocalDateTime.now(clock);

        List<Scored> scored = new ArrayList<>();
        for (ProjectEntry project : candidates) {
            if (isExcludedFor(project, userName)) continue;   // defensive: mirrors the DB filter

            Scored s = score(userSkills, SkillNormalizer.normalize(project.getTechnologies()),
                    isRecent(project, now));
            if (s == null) continue;                          // no skill in common -> score 0
            s.response = new RecommendationResponse(
                    project.getId(), null, project.getTitle(), s.percent, s.matched, s.missing);
            s.tieBreaker = project.getTitle();
            scored.add(s);
        }
        return toPage(scored, pageable);
    }

    // ------------------------------------------------------------------ developers for a project

    public Page<RecommendationResponse> recommendDevelopers(ObjectId projectId, String requester, Pageable pageable) {
        ProjectEntry project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (project.getUserName() == null || !project.getUserName().equals(requester)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the project owner can view developer recommendations");
        }

        List<String> tech = SkillNormalizer.normalize(project.getTechnologies());
        if (tech.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Set<String> excluded = new HashSet<>();
        excluded.add(project.getUserName());
        if (project.getMembers() != null) excluded.addAll(project.getMembers());

        Query query = new Query(
                Criteria.where("skills").in(tech)
                        .and("userName").nin(excluded))
                .limit(MAX_CANDIDATES);
        query.fields().include("userName").include("skills");   // never load password/email/roles

        List<User> candidates = mongoTemplate.find(query, User.class);

        List<Scored> scored = new ArrayList<>();
        for (User candidate : candidates) {
            if (excluded.contains(candidate.getUserName())) continue;   // defensive

            Scored s = score(new LinkedHashSet<>(SkillNormalizer.normalize(candidate.getSkills())), tech, false);
            if (s == null) continue;
            s.response = new RecommendationResponse(
                    null, candidate.getUserName(), null, s.percent, s.matched, s.missing);
            s.tieBreaker = candidate.getUserName();
            scored.add(s);
        }
        return toPage(scored, pageable);
    }

    // ------------------------------------------------------------------ scoring

    /**
     * @return the score, or {@code null} when there is no skill overlap (score 0 → excluded)
     */
    Scored score(Set<String> userSkills, List<String> projectTech, boolean recent) {
        if (projectTech.isEmpty() || userSkills.isEmpty()) return null;

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String t : projectTech) {
            (userSkills.contains(t) ? matched : missing).add(t);
        }
        if (matched.isEmpty()) return null;

        int intersection = matched.size();
        int union = userSkills.size() + projectTech.size() - intersection;

        double matchScore = (double) intersection / projectTech.size();
        double jaccard = (double) intersection / union;
        double finalScore = MATCH_WEIGHT * matchScore + JACCARD_WEIGHT * jaccard + (recent ? RECENCY_BOOST : 0);
        finalScore = Math.min(1.0, finalScore);

        Scored s = new Scored();
        s.value = finalScore;
        s.percent = (int) Math.round(finalScore * 100);
        s.matched = matched;
        s.missing = missing;
        return s;
    }

    private boolean isRecent(ProjectEntry project, LocalDateTime now) {
        return project.getDate() != null && !project.getDate().isBefore(now.minusDays(RECENCY_DAYS));
    }

    private boolean isExcludedFor(ProjectEntry project, String userName) {
        return userName.equals(project.getUserName())
                || (project.getMembers() != null && project.getMembers().contains(userName))
                || (project.getJoinRequests() != null && project.getJoinRequests().contains(userName));
    }

    private Page<RecommendationResponse> toPage(List<Scored> scored, Pageable pageable) {
        scored.sort(Comparator.comparingDouble((Scored s) -> s.value).reversed()
                .thenComparing(s -> s.tieBreaker == null ? "" : s.tieBreaker));

        int from = (int) Math.min(pageable.getOffset(), scored.size());
        int to = Math.min(from + pageable.getPageSize(), scored.size());
        List<RecommendationResponse> content = new ArrayList<>();
        for (Scored s : scored.subList(from, to)) content.add(s.response);
        return new PageImpl<>(content, pageable, scored.size());
    }

    static class Scored {
        double value;
        int percent;
        List<String> matched;
        List<String> missing;
        String tieBreaker;
        RecommendationResponse response;
    }
}

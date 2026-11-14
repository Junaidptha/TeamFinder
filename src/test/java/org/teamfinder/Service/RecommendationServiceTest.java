package org.teamfinder.Service;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.teamfinder.Entity.ProjectEntry;
import org.teamfinder.Entity.User;
import org.teamfinder.Repository.ProjectRepository;
import org.teamfinder.Repository.UserRepository;
import org.teamfinder.dto.RecommendationResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecommendationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 31, 12, 0);
    private static final PageRequest PAGE = PageRequest.of(0, 10);

    @Mock MongoTemplate mongoTemplate;
    @Mock UserRepository userRepository;
    @Mock ProjectRepository projectRepository;

    RecommendationService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new RecommendationService(mongoTemplate, userRepository, projectRepository, clock);
    }

    // ---------------------------------------------------------------- helpers

    private User user(String name, String... skills) {
        User u = new User(name);
        u.setSkills(List.of(skills));
        return u;
    }

    private ProjectEntry project(String id, String title, String owner, LocalDateTime date, String... tech) {
        ProjectEntry p = new ProjectEntry();
        p.setId(id);
        p.setTitle(title);
        p.setUserName(owner);
        p.setDate(date);
        p.setTechnologies(List.of(tech));
        p.setMembers(new ArrayList<>(List.of(owner)));
        return p;
    }

    private ProjectEntry oldProject(String id, String title, String... tech) {
        return project(id, title, "owner", NOW.minusDays(60), tech);
    }

    private List<RecommendationResponse> recommend(User me, ProjectEntry... candidates) {
        when(userRepository.findByUserName(me.getUserName())).thenReturn(Optional.of(me));
        when(mongoTemplate.find(any(Query.class), eq(ProjectEntry.class))).thenReturn(List.of(candidates));
        return service.recommendProjects(me.getUserName(), PAGE).getContent();
    }

    // ---------------------------------------------------------------- scoring

    @Test
    void perfectMatch_scores100() {
        List<RecommendationResponse> r = recommend(user("me", "java", "spring"),
                oldProject("1", "P", "java", "spring"));

        assertEquals(1, r.size());
        assertEquals(100, r.get(0).getScore());                     // 0.7*1 + 0.3*1
        assertEquals(List.of("java", "spring"), r.get(0).getMatchedSkills());
        assertTrue(r.get(0).getMissingSkills().isEmpty());
        assertEquals("1", r.get(0).getProjectId());
        assertNull(r.get(0).getUserName());
    }

    @Test
    void partialMatch_usesWeightedFormula() {
        // user {java, react, docker}, project {java, spring}: inter=1, union=4
        // 0.7*(1/2) + 0.3*(1/4) = 0.35 + 0.075 = 0.425 -> 43 (rounded)
        List<RecommendationResponse> r = recommend(user("me", "java", "react", "docker"),
                oldProject("1", "P", "java", "spring"));

        assertEquals(43, r.get(0).getScore());
        assertEquals(List.of("java"), r.get(0).getMatchedSkills());
        assertEquals(List.of("spring"), r.get(0).getMissingSkills());
    }

    @Test
    void noMatch_isExcluded() {
        List<RecommendationResponse> r = recommend(user("me", "java"),
                oldProject("1", "P", "python", "django"));

        assertTrue(r.isEmpty());
    }

    @Test
    void recentNoMatch_isStillExcluded_boostDoesNotCreateScore() {
        List<RecommendationResponse> r = recommend(user("me", "java"),
                project("1", "P", "owner", NOW.minusDays(1), "python"));

        assertTrue(r.isEmpty());
    }

    @Test
    void projectWithoutTechnologies_isExcluded() {
        List<RecommendationResponse> r = recommend(user("me", "java"), oldProject("1", "P"));

        assertTrue(r.isEmpty());
    }

    @Test
    void skillComparisonIsCaseInsensitive() {
        List<RecommendationResponse> r = recommend(user("me", "  Java "),
                oldProject("1", "P", "JAVA"));

        assertEquals(100, r.get(0).getScore());
    }

    // ---------------------------------------------------------------- recency

    @Test
    void recentProject_getsBoost_oldProjectDoesNot() {
        // inter=1, project=2, user=1 -> union=2: 0.7*0.5 + 0.3*0.5 = 0.5
        User me = user("me", "java");
        List<RecommendationResponse> r = recommend(me,
                project("old", "Old", "owner", NOW.minusDays(15), "java", "spring"),
                project("new", "New", "owner", NOW.minusDays(14), "java", "spring"));

        assertEquals("new", r.get(0).getProjectId());
        assertEquals(55, r.get(0).getScore());   // 0.5 + 0.05
        assertEquals("old", r.get(1).getProjectId());
        assertEquals(50, r.get(1).getScore());
    }

    @Test
    void boostIsCappedAt100() {
        List<RecommendationResponse> r = recommend(user("me", "java"),
                project("1", "P", "owner", NOW.minusDays(1), "java"));

        assertEquals(100, r.get(0).getScore());
    }

    // ---------------------------------------------------------------- exclusions

    @Test
    void excludesOwnedMemberAndRequestedProjects() {
        User me = user("me", "java");
        ProjectEntry owned = project("owned", "Owned", "me", NOW.minusDays(60), "java");
        ProjectEntry member = oldProject("member", "Member", "java");
        member.getMembers().add("me");
        ProjectEntry requested = oldProject("requested", "Requested", "java");
        requested.getJoinRequests().add("me");
        ProjectEntry open = oldProject("open", "Open", "java");

        List<RecommendationResponse> r = recommend(me, owned, member, requested, open);

        assertEquals(1, r.size());
        assertEquals("open", r.get(0).getProjectId());
    }

    @Test
    void query_prefiltersOnTechnologiesAndExclusions() {
        recommend(user("me", "java", "react"), oldProject("1", "P", "java"));

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(ProjectEntry.class));
        String q = captor.getValue().getQueryObject().toJson();
        assertTrue(q.contains("technologies") && q.contains("$in"), q);
        assertTrue(q.contains("userName") && q.contains("members") && q.contains("joinRequests"), q);
    }

    // ---------------------------------------------------------------- ordering & paging

    @Test
    void resultsAreSortedByScoreDescending_andPaged() {
        User me = user("me", "java", "spring");
        ProjectEntry low = oldProject("low", "Low", "java", "a", "b", "c");     // 1/4 cov
        ProjectEntry high = oldProject("high", "High", "java", "spring");       // perfect
        ProjectEntry mid = oldProject("mid", "Mid", "java", "spring", "a");     // 2/3 cov
        when(userRepository.findByUserName("me")).thenReturn(Optional.of(me));
        when(mongoTemplate.find(any(Query.class), eq(ProjectEntry.class))).thenReturn(List.of(low, high, mid));

        List<RecommendationResponse> all = service.recommendProjects("me", PAGE).getContent();
        assertEquals(List.of("high", "mid", "low"), all.stream().map(RecommendationResponse::getProjectId).toList());
        for (int i = 1; i < all.size(); i++) {
            assertTrue(all.get(i - 1).getScore() >= all.get(i).getScore());
        }

        Page<RecommendationResponse> page2 = service.recommendProjects("me", PageRequest.of(1, 2));
        assertEquals(3, page2.getTotalElements());
        assertEquals(List.of("low"), page2.getContent().stream().map(RecommendationResponse::getProjectId).toList());
    }

    // ---------------------------------------------------------------- validation

    @Test
    void userWithoutSkills_gets400() {
        when(userRepository.findByUserName("me")).thenReturn(Optional.of(user("me")));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.recommendProjects("me", PAGE));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }

    // ---------------------------------------------------------------- developers

    @Test
    void developers_ownerOnly_otherwise403() {
        ObjectId id = new ObjectId();
        when(projectRepository.findById(id)).thenReturn(Optional.of(oldProject("1", "P", "java")));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.recommendDevelopers(id, "intruder", PAGE));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    }

    @Test
    void developers_unknownProject_404() {
        ObjectId id = new ObjectId();
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.recommendDevelopers(id, "owner", PAGE));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void developers_excludeOwnerAndMembers_sortedWithSkillBreakdown() {
        ObjectId id = new ObjectId();
        ProjectEntry p = oldProject("1", "P", "java", "spring");
        p.getMembers().add("member");
        when(projectRepository.findById(id)).thenReturn(Optional.of(p));
        when(mongoTemplate.find(any(Query.class), eq(User.class))).thenReturn(List.of(
                user("partial", "java", "go"),
                user("owner", "java", "spring"),       // owner -> excluded
                user("member", "java", "spring"),      // member -> excluded
                user("best", "java", "spring"),
                user("nomatch", "cobol")));

        List<RecommendationResponse> r = service.recommendDevelopers(id, "owner", PAGE).getContent();

        assertEquals(List.of("best", "partial"), r.stream().map(RecommendationResponse::getUserName).toList());
        assertEquals(100, r.get(0).getScore());
        assertNull(r.get(0).getProjectId());
        assertEquals(List.of("java"), r.get(1).getMatchedSkills());
        assertEquals(List.of("spring"), r.get(1).getMissingSkills());
    }
}

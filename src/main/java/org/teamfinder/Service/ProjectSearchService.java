package org.teamfinder.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;
import org.teamfinder.Entity.ProjectEntry;
import org.teamfinder.dto.ProjectSearchRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectSearchService {

    private final MongoTemplate mongoTemplate;

    public Page<ProjectEntry> searchProjects(ProjectSearchRequest req) {

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // 🔍 Keyword search (title + description)
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(req.getKeyword(), "i"),
                    Criteria.where("description").regex(req.getKeyword(), "i")
            ));
        }

        // 🧪 Technology filter (ARRAY FIELD)
        if (req.getTech() != null && !req.getTech().isBlank()) {
            criteriaList.add(
                    Criteria.where("technologies").in(req.getTech())
            );
        }

        // 👑 Owner / Leader filter
        if (req.getOwner() != null && !req.getOwner().isBlank()) {
            criteriaList.add(
                    Criteria.where("leader").is(req.getOwner())
            );
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(
                    criteriaList.toArray(new Criteria[0])
            ));
        }

        // 🔃 Sorting
        Sort sort = Sort.by(
                req.getSortDirection().equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                req.getSortBy()
        );
        query.with(sort);

        // 📄 Pagination
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize());
        query.with(pageable);

        List<ProjectEntry> result =
                mongoTemplate.find(query, ProjectEntry.class);

        long count =
                mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ProjectEntry.class);

        return new PageImpl<>(result, pageable, count);
    }
}

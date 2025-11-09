package org.teamfinder.Repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.teamfinder.Entity.ProjectEntry;

import java.util.List;

public interface ProjectRepository extends MongoRepository<ProjectEntry, ObjectId> {
    List<ProjectEntry> findByOwnerEmail(String ownerEmail );

    boolean existsByTitleAndOwnerName(String title, String userName);
}




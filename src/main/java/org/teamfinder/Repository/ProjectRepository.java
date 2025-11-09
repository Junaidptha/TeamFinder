package org.teamfinder.Repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.teamfinder.Entity.ProjectEntry;

import java.util.List;

public interface ProjectRepository extends MongoRepository<ProjectEntry, ObjectId> {
//    List<ProjectEntry> findByOwnerEmail(String Email );

    boolean existsByTitleAndUserName(String title, String userName);

    List<ProjectEntry> findByUserName(String userName);

}




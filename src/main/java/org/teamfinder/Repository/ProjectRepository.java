package org.teamfinder.Repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.teamfinder.Entity.ProjectEntry;

import java.util.List;

public interface ProjectRepository extends MongoRepository<ProjectEntry, ObjectId> {
//    List<ProjectEntry> findByOwnerEmail(String Email );

    boolean existsByTitleAndUserName(String title, String userName);

    List<ProjectEntry> findByUserName(String userName);

//    List<ProjectEntry> searchByKeyword(String keyword);
//
//    // 🎯 Filter by technology
//    List<ProjectEntry> findByTechnologyIgnoreCase(String tech);
//
//    // 🎯 Filter by owner
//    List<ProjectEntry> findByOwnerUserName(String ownerUserName);
//
//    // 🎯 Date range
//    @Query("{ 'date': { $gte: ?0, $lte: ?1 } }")
//    List<ProjectEntry> filterByDateRange(String startDate, String endDate);
}




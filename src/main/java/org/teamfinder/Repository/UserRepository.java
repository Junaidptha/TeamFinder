package org.teamfinder.Repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.teamfinder.Entity.ProjectEntry;
import org.teamfinder.Entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User , ObjectId> {
    Optional<User> findByUserName(String userName);
    ;

    Void deleteByUserName(String userName);

    User findByEmail(String email);

}

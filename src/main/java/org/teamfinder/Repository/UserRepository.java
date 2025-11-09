package org.teamfinder.Repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.teamfinder.Entity.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User , ObjectId> {
    Optional<User> findByUserName(String userName);
    ;

    Void deleteByUserName(String userName);
    User findByEmail(String email);

}

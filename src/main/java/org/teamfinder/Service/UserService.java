package org.teamfinder.Service;


import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.teamfinder.Entity.User;
import org.teamfinder.Repository.UserRepository;

import java.util.*;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();



    public boolean saveNewEntry(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            user.setRoles(Arrays.asList("USER"));
            userRepository.save(user);
            return true;

        }catch (Exception e){
            log.error("error occurred for {}:" , user.getUserName());
            log.info(" hahhah");
            log.debug("hjahsjhdjahjdhsa");
            return false;
        }

    }

    public  void saveUser(User user){
//        List<String> roles = new ArrayList<>(user.getRoles());
//        if (!roles.contains("LEADER")) {
//            roles.add("LEADER");
//        }
//        user.setRoles(roles);
        userRepository.save(user);
    }


    public List<User> getAll(){

        return userRepository.findAll();
    }


    public Optional<User> getById(ObjectId id){

        return userRepository.findById(id);
    }


    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public User findByUserName(String userName){

        return userRepository.findByUserName(userName)
                .orElse(null);
    }

    public boolean deleteById(ObjectId id){
        if(userRepository.existsById(id)){
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

}

package org.teamfinder.Service;


import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.teamfinder.Entity.User;
import org.teamfinder.Repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static final int MAX_SKILLS = 50;
    public static final int MAX_SKILL_LENGTH = 50;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();



    public void saveNewEntry(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            user.setRoles(Arrays.asList("USER"));
            userRepository.save(user);

        }catch (Exception e){
            log.error("error occurred for {}:" , user.getUserName());
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


    /**
     * Replaces the skills of the given user (normalized: trimmed, lowercase, deduplicated).
     * @throws IllegalArgumentException if no valid skill remains or limits are exceeded
     */
    public User updateSkills(String userName, List<String> skills) {
        List<String> normalized = SkillNormalizer.normalize(skills);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Skills list must contain at least one non-blank skill");
        }
        if (normalized.size() > MAX_SKILLS) {
            throw new IllegalArgumentException("A maximum of " + MAX_SKILLS + " skills is allowed");
        }
        if (normalized.stream().anyMatch(s -> s.length() > MAX_SKILL_LENGTH)) {
            throw new IllegalArgumentException("Each skill must be at most " + MAX_SKILL_LENGTH + " characters");
        }
        User user = findByUserName(userName);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }
        user.setSkills(normalized);
        userRepository.save(user);
        return user;
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

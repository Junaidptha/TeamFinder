package org.teamfinder.Service;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.teamfinder.Entity.ProjectEntry;
import org.teamfinder.Entity.User;
import org.teamfinder.Repository.ProjectRepository;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final UserService userService;

    private final ProjectRepository projectRepository;


    public void saveProject( ProjectEntry projectEntry ,String userName){


            boolean exists = projectRepository.existsByTitleAndOwnerName(
                    projectEntry.getTitle(),
                    userName
            );

            if (exists) {
                throw new IllegalStateException("You already have a project with this title.");
            }
            User user = userService.findByUserName(userName);
            projectEntry.setDate(LocalDateTime.now());
            projectEntry.setOwnerName(user.getUserName());
            projectEntry.setOwnerEmail(user.getEmail());
            projectEntry.setLeader(user.getUserName());

            if (projectEntry.getMembers() == null) {
                projectEntry.setMembers(new ArrayList<>());
            }
            if (!projectEntry.getMembers().contains(userName)) {
                projectEntry.getMembers().add(userName);
            }
            ProjectEntry saved = projectRepository.save(projectEntry);
            user.getProjectEntry().add(saved);


            userService.saveUser(user);
    }
//    public void createProject(ProjectEntry projectEntry){
//         projectRepository.save(projectEntry);
//    }

//    public  void saveProject(ProjectEntry projectEntry){
//        projectRepository.save(projectEntry);
//    }
//
    public List<ProjectEntry> getUserProjects(String ownerEmail){
        return projectRepository.findByOwnerEmail(ownerEmail);
    }

    public List<ProjectEntry> getAll(){

        return projectRepository.findAll();
    }
    public Optional<ProjectEntry> getById(ObjectId id){

        return projectRepository.findById(id);
    }

    public List<ProjectEntry> getProjectsByUserId(ObjectId userId) {
        Optional<User> userOpt = userService.getById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getProjectEntry();
        }
        return List.of();
    }


    public boolean deleteById(ObjectId id){
        if(projectRepository.existsById(id)){
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean deleteById(ObjectId id , String userName){
        boolean removed = false;
        User user = userService.findByUserName(userName);
        removed = user.getProjectEntry().removeIf(x -> x.getId().equals(id));
        if (removed) {
            userService.saveUser(user);
            projectRepository.deleteById(id);
        }
        return removed;
    }





}

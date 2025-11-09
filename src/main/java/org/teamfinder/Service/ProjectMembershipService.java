package org.teamfinder.Service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.teamfinder.Entity.ProjectEntry;
import org.teamfinder.Entity.User;
import org.teamfinder.Repository.ProjectRepository;
import org.teamfinder.Repository.UserRepository;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class ProjectMembershipService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // 🔹 1. User sends a join request


    public String sendJoinRequest(ObjectId projectId, String username) {
        Optional<ProjectEntry> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) return "Project not found";

        ProjectEntry project = projectOpt.get();


        if (project.getOwnerName().equals(username)) return "Owner cannot join their own project";

        if (project.getJoinRequests() == null) project.setJoinRequests(new ArrayList<>());
        if (project.getMembers().contains(username)) return "Already a member";
        if (project.getJoinRequests().contains(username)) return "Request already sent";

        project.getJoinRequests().add(username);
        projectRepository.save(project);

        return "Join request sent successfully";
    }




    // 🔹 2. Owner accepts a join request
    public String acceptJoinRequest(ObjectId projectId, String ownerUsername, String requesterUsername) {
        Optional<ProjectEntry> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) return "Project not found";

        ProjectEntry project = projectOpt.get();
        if (!project.getOwnerName().equals(ownerUsername)) {
            return "Only the owner can accept requests";
        }

        if (!project.getJoinRequests().contains(requesterUsername)) {
            return "Join request not found";
        }

        project.getJoinRequests().remove(requesterUsername);

        // Add user to project members
        project.getMembers().add(requesterUsername);
        projectRepository.save(project);

        // Also update user's projectJoined
        Optional<User> userOpt = userRepository.findByUserName(requesterUsername);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getProjectJoined() == null) user.setProjectJoined(new ArrayList<>());
            user.getProjectJoined().add(projectId);
            userRepository.save(user);
        }

        return "User added as member successfully";
    }


    // 🔹 3. Owner rejects a join request
    public String rejectJoinRequest(ObjectId projectId, String ownerUserName, String requesterUserName) {
        Optional<ProjectEntry> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) return "Project not found.";

        ProjectEntry project = projectOpt.get();

        if (!project.getOwnerName().equals(ownerUserName)) {
            return "Only project owner can reject requests.";
        }


        if (project.getJoinRequests() != null && project.getJoinRequests().contains(requesterUserName)) {
            project.getJoinRequests().remove(requesterUserName);
            projectRepository.save(project);
            return "Join request rejected.";
        } else {
            return "No such join request found.";
        }
    }

    // 🔹 4. Owner removes an existing member
    public String removeMember(ObjectId projectId, String ownerUsername, String memberUsername) {
        Optional<ProjectEntry> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) return "Project not found";

        ProjectEntry project = projectOpt.get();
        if (!project.getOwnerName().equals(ownerUsername)) {
            return "Only owner can remove members";
        }


        if (!project.getMembers().contains(memberUsername)) {
            return "This user is not a member of the project";
        }

        project.getMembers().remove(memberUsername);
        projectRepository.save(project);
        // Also remove project from user's joined list
        Optional<User> userOpt = userRepository.findByUserName(memberUsername);
        userOpt.ifPresent(user -> {
            if (user.getProjectJoined() != null) {
                user.getProjectJoined().removeIf(p -> p.equals(projectId));
                userRepository.save(user);
            }
        });
        return "Member removed successfully";
    }
    
}

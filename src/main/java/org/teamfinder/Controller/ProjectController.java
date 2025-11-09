package org.teamfinder.Controller;


import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.teamfinder.Entity.ProjectEntry;
import org.teamfinder.Service.ProjectMembershipService;
import org.teamfinder.Service.ProjectService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor  // create constructor
public class ProjectController {


    private final ProjectService projectService;


    private final ProjectMembershipService projectMembershipService;

    @GetMapping("/secure-test")
    public String secureTest() {
        return "✅ Token is valid, secure endpoint accessed!";
    }

    //Create new project (only logged-in user can create)
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectEntry projectEntry) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            projectService.saveProject(projectEntry, userName);
            return new ResponseEntity<>(projectEntry, HttpStatus.CREATED);

        } catch (IllegalStateException e) {
            // Duplicate project
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating project", HttpStatus.BAD_REQUEST);
        }
    }




    @GetMapping
    public ResponseEntity<List<ProjectEntry>> getUserProjects() {
        try {
            // Get the currently authenticated user's email/username
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String ownerEmail = authentication.getName();

            // Fetch only that user's projects
            List<ProjectEntry> userProjects = projectService.getUserProjects(ownerEmail);

            return new ResponseEntity<>(userProjects, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Get all projects (everyone can view)
    @GetMapping("/all")
    public ResponseEntity<List<ProjectEntry>> getAllProjects() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String ownerEmail = authentication.getName();
            List<ProjectEntry> allProjects = projectService.getAll();
            return new ResponseEntity<>(allProjects, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





     // Get single project by ID
    @GetMapping("/id/{projectId}")
    public ResponseEntity<ProjectEntry> getProjectById(@PathVariable ObjectId projectId) {
        Optional<ProjectEntry> project = projectService.getById(projectId);
        return project.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProjectEntry>> getProjectsByUserId(@PathVariable ObjectId userId) {
        try {
            System.out.println("📢 Request hit /project/user/" + userId);

            List<ProjectEntry> projects = projectService.getProjectsByUserId(userId);
            if (projects.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(projects, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete project by ID (only owner can delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable ObjectId id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            boolean deleted = projectService.deleteById(id, userName);
            if (deleted) {
                return new ResponseEntity<>("Project deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("You are not allowed to delete this project", HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting project: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/update/{id}")
    public void updateProject(@PathVariable ObjectId id , @RequestBody ProjectEntry newProjectEntry){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        Optional<ProjectEntry> projectOpt = projectService.getById(id);
        if (projectOpt.isPresent()) {
            ProjectEntry oldProjectEntry = projectOpt.get();
            oldProjectEntry.setTitle(newProjectEntry.getTitle() != null && !newProjectEntry.getTitle().isEmpty()
                    ? newProjectEntry.getTitle()
                    : oldProjectEntry.getTitle());
            oldProjectEntry.setDescription(
                    newProjectEntry.getDescription() != null && !newProjectEntry.getDescription().isEmpty()
                            ? newProjectEntry.getDescription()
                            : oldProjectEntry.getDescription()
            );

            oldProjectEntry.setTechnology(
                    newProjectEntry.getTechnology() != null && !newProjectEntry.getTechnology().isEmpty()
                            ? newProjectEntry.getTechnology()
                            : oldProjectEntry.getTechnology()
            );

            oldProjectEntry.setOwnerEmail(
                    newProjectEntry.getOwnerEmail() != null && !newProjectEntry.getOwnerEmail().isEmpty()
                            ? newProjectEntry.getOwnerEmail()
                            : oldProjectEntry.getOwnerEmail()
            );
//            projectService.saveProject(oldProjectEntry);

        }
    }
}

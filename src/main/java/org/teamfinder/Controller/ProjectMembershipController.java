package org.teamfinder.Controller;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.teamfinder.Service.ProjectMembershipService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/project/membership")
public class ProjectMembershipController {


    private final ProjectMembershipService membershipService;

    // 🔹 1. Send join request (user requests to join a project)
    @PostMapping("/{projectId}/join")
    public ResponseEntity<String> sendJoinRequest(@PathVariable ObjectId projectId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        String message = membershipService.sendJoinRequest(projectId, username);
        return ResponseEntity.ok(message);
    }

    // 🔹 2. Owner accepts a join request
    @PostMapping("/{projectId}/accept/{requesterUsername}")
    public ResponseEntity<String> acceptJoinRequest(@PathVariable ObjectId projectId,
                                                    @PathVariable String requesterUsername) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String ownerUsername = auth.getName();

        String message = membershipService.acceptJoinRequest(projectId, ownerUsername, requesterUsername);
        return ResponseEntity.ok(message);
    }

    // 🔹 3. Owner rejects a join request
    @DeleteMapping("/{projectId}/reject/{requesterUsername}")
    public ResponseEntity<String> rejectJoinRequest(@PathVariable ObjectId projectId,
                                                    @PathVariable String requesterUsername) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String ownerUsername = auth.getName();

        String message = membershipService.rejectJoinRequest(projectId, ownerUsername, requesterUsername);
        return ResponseEntity.ok(message);
    }

    // 🔹 4. Owner removes an existing member
    @DeleteMapping("/{projectId}/remove/{memberUsername}")
    public ResponseEntity<String> removeMember(@PathVariable ObjectId projectId,
                                               @PathVariable String memberUsername) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String ownerUsername = auth.getName();

        String message = membershipService.removeMember(projectId, ownerUsername, memberUsername);
        return ResponseEntity.ok(message);
    }
}

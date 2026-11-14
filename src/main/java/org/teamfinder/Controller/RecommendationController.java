package org.teamfinder.Controller;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.teamfinder.Service.RecommendationService;
import org.teamfinder.dto.PagedResponse;
import org.teamfinder.dto.RecommendationResponse;


@RestController
@RequiredArgsConstructor
@RequestMapping("/recommendations")
public class RecommendationController {

    private static final int MAX_PAGE_SIZE = 50;

    private final RecommendationService recommendationService;

    // Best projects for the logged-in user
    @GetMapping("/projects")
    public ResponseEntity<PagedResponse<RecommendationResponse>> recommendProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userName = currentUserName();
        return ResponseEntity.ok(PagedResponse.of(
                recommendationService.recommendProjects(userName, pageRequest(page, size))));
    }

    // Best developers for a project (owner only)
    @GetMapping("/projects/{projectId}/developers")
    public ResponseEntity<PagedResponse<RecommendationResponse>> recommendDevelopers(
            @PathVariable ObjectId projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userName = currentUserName();
        return ResponseEntity.ok(PagedResponse.of(
                recommendationService.recommendDevelopers(projectId, userName, pageRequest(page, size))));
    }

    private String currentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private PageRequest pageRequest(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return PageRequest.of(page, size);
    }
}

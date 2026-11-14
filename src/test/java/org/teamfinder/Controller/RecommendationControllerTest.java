package org.teamfinder.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.teamfinder.Security.JwtUtil;
import org.teamfinder.Service.RecommendationService;
import org.teamfinder.Service.UserDetailsServiceImpl;
import org.teamfinder.dto.RecommendationResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean RecommendationService recommendationService;
    // required by the JwtAuthFilter bean that @WebMvcTest picks up
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean UserDetailsServiceImpl userDetailsService;

    @Test
    void recommendProjects_returnsScoredPage_withoutSensitiveFields() throws Exception {
        var rec = new RecommendationResponse("p1", null, "Chat App", 85, List.of("java"), List.of("react"));
        when(recommendationService.recommendProjects(eq("alice"), any()))
                .thenReturn(new PageImpl<>(List.of(rec), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/recommendations/projects").param("page", "0").param("size", "10")
                        .with(user("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].projectId").value("p1"))
                .andExpect(jsonPath("$.content[0].title").value("Chat App"))
                .andExpect(jsonPath("$.content[0].score").value(85))
                .andExpect(jsonPath("$.content[0].matchedSkills[0]").value("java"))
                .andExpect(jsonPath("$.content[0].missingSkills[0]").value("react"))
                .andExpect(jsonPath("$.content[0].userName").doesNotExist())
                .andExpect(jsonPath("$.content[0].email").doesNotExist())
                .andExpect(jsonPath("$.content[0].password").doesNotExist())
                .andExpect(jsonPath("$.content[0].roles").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(recommendationService).recommendProjects("alice", PageRequest.of(0, 10));
    }

    @Test
    void recommendProjects_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/recommendations/projects"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void recommendProjects_userWithoutSkills_returns400() throws Exception {
        when(recommendationService.recommendProjects(eq("alice"), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Add skills first"));

        mockMvc.perform(get("/recommendations/projects").with(user("alice")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recommendProjects_invalidPageSize_returns400() throws Exception {
        mockMvc.perform(get("/recommendations/projects").param("size", "0").with(user("alice")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void developers_nonOwner_returns403() throws Exception {
        when(recommendationService.recommendDevelopers(any(), eq("bob"), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "owner only"));

        mockMvc.perform(get("/recommendations/projects/65f1a2b3c4d5e6f708091a2b/developers").with(user("bob")))
                .andExpect(status().isForbidden());
    }
}

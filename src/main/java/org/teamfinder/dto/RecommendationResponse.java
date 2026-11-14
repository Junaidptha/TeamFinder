package org.teamfinder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Recommendation result. For a project recommendation {@code projectId} and {@code title} are set;
 * for a developer recommendation {@code userName} is set. Null fields are omitted from the JSON.
 * Deliberately contains no email, password or roles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationResponse {
    private String projectId;
    private String userName;
    private String title;
    private int score;                      // 0-100
    private List<String> matchedSkills;
    private List<String> missingSkills;
}

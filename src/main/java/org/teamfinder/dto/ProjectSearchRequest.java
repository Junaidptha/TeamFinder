package org.teamfinder.dto;
import lombok.Data;

@Data
public class ProjectSearchRequest {
    private String keyword;
    private String tech;
    private String owner;
    private String startDate;
    private String endDate;

    private int page = 0;
    private int size = 10;

    private String sortBy = "date";
    private String sortDirection = "desc";
}


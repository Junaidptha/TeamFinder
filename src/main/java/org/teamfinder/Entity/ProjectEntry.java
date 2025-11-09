package org.teamfinder.Entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Document(collection = "ProjectEntry")
@Data
@NoArgsConstructor
public class ProjectEntry {
    @Id
    private String id;
    private String title;
    private String description;
    private String technology;
    private String ownerEmail;
    private String ownerName;
    private LocalDateTime date;
    private String leader;


    private List<String> joinRequests = new ArrayList<>();

    private List<String> members = new ArrayList<>();
}

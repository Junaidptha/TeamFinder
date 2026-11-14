package org.teamfinder.Entity;

import lombok.Data;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import org.teamfinder.Service.SkillNormalizer;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "User")
@Data
public class User {

    @Id
    private ObjectId id;
    @Indexed(unique = true)
    @NonNull
    private String userName;
    private String email;
    private String password;

    @DBRef
    private List<ProjectEntry> projectEntry= new ArrayList<>();

    @DBRef
    private List<ObjectId> projectJoined = new ArrayList<>();
    private List<String> roles;

    // Always stored trimmed + lowercase (see setSkills)
    private List<String> skills = new ArrayList<>();

    public void setSkills(List<String> skills) {
        this.skills = SkillNormalizer.normalize(skills);
    }

}

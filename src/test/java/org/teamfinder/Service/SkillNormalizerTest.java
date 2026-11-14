package org.teamfinder.Service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.teamfinder.Entity.User;
import org.teamfinder.Repository.UserRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SkillNormalizerTest {

    @Test
    void trimsLowercasesDedupesAndDropsBlanks() {
        assertEquals(List.of("java", "spring boot"),
                SkillNormalizer.normalize(Arrays.asList("  Java ", "JAVA", "", null, "Spring Boot")));
        assertTrue(SkillNormalizer.normalize(null).isEmpty());
    }

    @Test
    void updateSkills_rejectsEmptyAndBlankLists_andNormalizesValidOnes() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        UserService service = new UserService(repo);
        User u = new User("alice");
        when(repo.findByUserName("alice")).thenReturn(java.util.Optional.of(u));

        assertThrows(IllegalArgumentException.class, () -> service.updateSkills("alice", List.of()));
        assertThrows(IllegalArgumentException.class, () -> service.updateSkills("alice", List.of("  ", "")));

        assertEquals(List.of("java", "react"), service.updateSkills("alice", List.of(" Java", "REACT")).getSkills());
        Mockito.verify(repo).save(u);
    }
}

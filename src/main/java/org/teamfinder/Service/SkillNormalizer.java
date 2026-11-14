package org.teamfinder.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Normalizes skill / technology names: trimmed, lowercase, no blanks, no duplicates
 * (original order preserved).
 */
public final class SkillNormalizer {

    private SkillNormalizer() {
    }

    public static List<String> normalize(List<String> raw) {
        if (raw == null) {
            return new ArrayList<>();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String item : raw) {
            if (item == null) continue;
            String cleaned = item.trim().toLowerCase(Locale.ROOT);
            if (!cleaned.isEmpty()) {
                result.add(cleaned);
            }
        }
        return new ArrayList<>(result);
    }
}

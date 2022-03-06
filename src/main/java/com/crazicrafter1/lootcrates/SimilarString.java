package com.crazicrafter1.lootcrates;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class SimilarString implements Comparable<SimilarString> {

    public final int distance;
    public final String s;

    public SimilarString(String base, String other) {
        distance = StringUtils.getLevenshteinDistance(base, other);
        s = base;
    }

    @Override
    public int compareTo(@NotNull SimilarString o) {
        return distance - o.distance;
    }

    @Override
    public String toString() {
        return s;
    }
}

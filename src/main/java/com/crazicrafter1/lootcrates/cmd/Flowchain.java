package com.crazicrafter1.lootcrates.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

public class Flowchain {

    private final String primary;
    private final LinkedList<String> args;
    private final Multimap<Class<?>, Object> captured = ArrayListMultimap.create();

    public Flowchain(String primary, String[] args) {
        this.primary = primary;
        this.args = new LinkedList<>(Arrays.asList(args));
    }

    @Nonnull
    private String next() {
        return Objects.requireNonNull(args.pollFirst());
    }

    @Nonnull
    public <T> T get(Type<T> type) {
        Iterator<T> itr = (Iterator<T>) captured.get(type.getType()).iterator();

        T o = itr.next();
        itr.remove();
        return o;
    }

    public <T> Flowchain capture(@Nonnull Type<T> type) {
        captured.put(type.getType(), type.get(next()));
        return this;
    }

    public <T> Flowchain capture(@Nonnull Type<T> type, @Nonnull T def) {
        try {
            captured.put(type.getType(), type.get(next()));
        } catch (Exception e) {
            e.printStackTrace();
            captured.put(type.getType(), def);
        }
        return this;
    }

    public void compile(@Nonnull Consumer<Flowchain> action) {
        action.accept(this);
    }

}

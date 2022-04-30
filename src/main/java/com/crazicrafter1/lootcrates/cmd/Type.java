package com.crazicrafter1.lootcrates.cmd;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.Function;

public final class Type<T> {
    private final Function<String, T> converter;
    private final Class<T> type;

    public static final Type<Number> NUMBER = new Type<>(s -> {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Not a number");
        }
    }, Number.class);
    public static final Type<String> STRING = new Type<>(s -> s, String.class);
    //public static final Expect<Object> NUMBER_OR_STRING = new Expect<>(s -> {
    //    try {
    //        return Double.parseDouble(s);
    //    } catch (NumberFormatException e) {
    //        return s;
    //    }
    //}, O);
    public static final Type<Player> PLAYER = new Type<>(s -> Objects.requireNonNull(Bukkit.getPlayer(s), "Player does not exist"), Player.class);
    @Deprecated
    public static final Type<OfflinePlayer> ANY_PLAYER = new Type<>(s -> {
        for (OfflinePlayer p : Bukkit.getOnlinePlayers())
            if (s.equals(p.getName())) return p;
        throw new NullPointerException("Player doesnt exist");
    }, OfflinePlayer.class);

    private Type(Function<String, T> converter, Class<T> type) {
        this.converter = converter;
        this.type = type;
    }

    public T get(String arg) {
        return converter.apply(arg);
    }

    public Class<T> getType() {
        return type;
    }
}

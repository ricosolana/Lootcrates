package com.crazicrafter1.lootcrates.cmd;

import com.crazicrafter1.crutils.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.Function;

public final class Expect<T> {
    private final Function<String, T> converter;
    private final Class<T> type;

    public static final Expect<Number> NUMBER = new Expect<>(s -> {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Not a number");
        }
    }, Number.class);
    public static final Expect<String> STRING = new Expect<>(s -> s, String.class);
    //public static final Expect<Object> NUMBER_OR_STRING = new Expect<>(s -> {
    //    try {
    //        return Double.parseDouble(s);
    //    } catch (NumberFormatException e) {
    //        return s;
    //    }
    //}, O);
    public static final Expect<Player> PLAYER = new Expect<>(s -> Objects.requireNonNull(Bukkit.getPlayer(s), "Player does not exist"), Player.class);
    @Deprecated
    public static final Expect<OfflinePlayer> ANY_PLAYER = new Expect<>(s -> {
        for (OfflinePlayer p : Bukkit.getOnlinePlayers())
            if (s.equals(p.getName())) return p;
        throw new NullPointerException("Player doesnt exist");
    }, OfflinePlayer.class);

    private Expect(Function<String, T> converter, Class<T> type) {
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

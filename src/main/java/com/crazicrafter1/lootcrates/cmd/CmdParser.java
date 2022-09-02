package com.crazicrafter1.lootcrates.cmd;

import com.google.common.collect.LinkedHashMultimap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CmdParser {

    private final CommandSender sender;
    private final LinkedList<String> args;

    private LinkedHashMultimap<Class<?>, Object> saved = LinkedHashMultimap.create();

    public CmdParser(CommandSender sender, String[] args) {
        this.sender = sender;
        this.args = new LinkedList<>(Arrays.asList(args));
    }

    /**
     * Get the next expect type captured by optional()
     * @param type The {@link Type} static instance
     * @param <T> The generic
     * @return The generic arg
     * @throws RuntimeException
     */
    @Nullable
    public <T> T get(Type<T> type) throws RuntimeException {
        // iterate the set
        //noinspection unchecked
        Set<T> f = (Set<T>) saved.get(type.getType());

        Iterator<T> itr = f.iterator();

        if (!itr.hasNext())
            throw new RuntimeException("Input more arguments");

        T o = itr.next();
        f.remove(o);
        return o;
    }

    public Player getPlayer() throws RuntimeException {
        return getPlayer(false);
    }

    public Player getPlayer(boolean orSender) throws RuntimeException {
        // if no player fallthrough, capture this

        try {
            return get(Type.PLAYER);
        } catch (RuntimeException e) {
            if (orSender) return (Player) sender;
            throw new RuntimeException("Must be a player to execute this command");
        }
    }

    public String getString() throws RuntimeException {
        return get(Type.STRING);
    }

    public Number getNumber() throws RuntimeException {
        return get(Type.NUMBER);
    }







    private static final int MASK_CHECK                     = 0b001;
    private static final int MASK_SAVE                      = 0b010;
    private static final int MASK_FALLTHROUGH               = 0b100;

    private static final int MODE_CHECK                     = MASK_CHECK;
    private static final int MODE_CHECK_SAVE                = MASK_CHECK | MASK_SAVE;
    private static final int MODE_CHECK_SAVE_FALLTHROUGH    = MASK_CHECK | MASK_SAVE | MASK_FALLTHROUGH;

    private void token( @Nonnull String arg,
                        @Nonnull Consumer<CmdParser> successFunction) {
        token(arg, null, successFunction);
    }

    private void token(int mode,
                        @Nonnull Collection<String> other,
                        @Nonnull Consumer<CmdParser> successFunction) {
        token(mode, other, null, successFunction);
    }

    private void token( @Nonnull String arg,
                        @Nullable Consumer<String> errorHandler,
                        @Nonnull Consumer<CmdParser> successFunction) {
        try {
            if (args.isEmpty())
                throw new RuntimeException("Input more arguments");

            if (args.poll().equals(arg)) {
                successFunction.accept(this);
            } else
                throw new RuntimeException("Illegal argument");
        } catch (RuntimeException e) {
            if (errorHandler != null)
                errorHandler.accept(e.getMessage());
        }
    }

    private void token(int mode,
                        @Nonnull Collection<String> other,
                        @Nullable Consumer<String> errorHandler,
                        @Nonnull Consumer<CmdParser> successFunction) {
        try {
            boolean f = (mode & MASK_FALLTHROUGH) == MASK_FALLTHROUGH;

            // if no fallthrough, and no arguments to test, throw
            if (!f && args.isEmpty())
                throw new RuntimeException("Input more arguments");

            String first = args.poll();

            if (f || other.contains(first)) {
                // contains, so save it
                if (!f) {
                    boolean s = (mode & MASK_SAVE) == MASK_SAVE;
                    if (s) saved.put(String.class, first);
                }

                successFunction.accept(this);
            }
        } catch (RuntimeException e) {
            if (errorHandler != null)
                errorHandler.accept(e.getMessage());
        }
    }

    private <T> void token(int mode,
                            @Nonnull Type<T> type,
                            @Nonnull Consumer<CmdParser> successFunction) {
        //token(expect, null, successFunction);
    }

    private <T> void token(int mode,
                            @Nonnull Type<T> type,
                            @Nullable Consumer<String> errorHandler,
                            @Nonnull Consumer<CmdParser> successFunction) {
        if (args.isEmpty()) {
            if (errorHandler != null) errorHandler.accept("Input more arguments");
        } else {
            // try to parse to expect
            try {
                type.get(args.poll());
                successFunction.accept(this);
            } catch (Exception e) {
                if (errorHandler != null) errorHandler.accept("Illegal argument");
            }
        }
    }





    public void isToken(@Nonnull String arg,
                        @Nonnull Consumer<CmdParser> successFunction) {
        isToken(arg, null, successFunction);
    }

    public void isToken(@Nonnull Collection<String> other,
                        @Nonnull Consumer<CmdParser> successFunction) {
        isToken(other, null, successFunction);
    }

    public void isToken(@Nonnull String arg,
                        @Nullable Consumer<String> errorHandler,
                        @Nonnull Consumer<CmdParser> successFunction) {
        try {
            if (args.isEmpty())
                throw new RuntimeException("Input more arguments");

            if (args.poll().equals(arg)) {
                successFunction.accept(this);
            } else
                throw new RuntimeException("Illegal argument");
        } catch (RuntimeException e) {
            if (errorHandler != null)
                errorHandler.accept(e.getMessage());
        }
    }

    public void isToken(@Nonnull Collection<String> other,
                        @Nullable Consumer<String> errorHandler,
                        @Nonnull Consumer<CmdParser> successFunction) {
        try {
            if (args.isEmpty())
                throw new RuntimeException("Input more arguments");

            if (other.contains(args.poll())) {
                successFunction.accept(this);
            }
        } catch (RuntimeException e) {
            if (errorHandler != null)
                errorHandler.accept(e.getMessage());
        }
    }

    public <T> void isToken(@Nonnull Type<T> type,
                            @Nonnull Consumer<CmdParser> successFunction) {
        isToken(type, null, successFunction);
    }

    public <T> void isToken(@Nonnull Type<T> type,
                            @Nullable Consumer<String> errorHandler,
                            @Nonnull Consumer<CmdParser> successFunction) {
        if (args.isEmpty()) {
            if (errorHandler != null) errorHandler.accept("Input more arguments");
        } else {
            // try to parse to expect
            try {
                type.get(args.poll());
                successFunction.accept(this);
            } catch (Exception e) {
                if (errorHandler != null) errorHandler.accept("Illegal argument");
            }
        }
    }



    public <T> CmdParser optional(Type<T> type, Consumer<CmdParser> successFunction) {
        try {
            T exp = type.get(args.get(0));
            saved.put(type.getType(), exp);
            args.remove(0);
            //Main.get().info("Expected: " + exp);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        successFunction.accept(this);
        return this;
    }

    public CommandSender getSender() {
        return sender;
    }
}

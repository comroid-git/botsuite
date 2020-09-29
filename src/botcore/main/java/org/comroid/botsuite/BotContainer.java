package org.comroid.botsuite;

import org.comroid.api.Invocable;
import org.comroid.mutatio.ref.Reference;
import org.comroid.util.ReflectionHelper;

import java.util.NoSuchElementException;

public final class BotContainer<T extends BotBase> {
    private final Class<T> type;
    private final Adapter adapter;
    private final Invocable<T> constr;
    private final Reference<T> ref = Reference.create();

    public BotContainer(Class<T> type) {
        this.type = type;
        this.adapter = Adapter.getFromBaseClass(type);
        this.constr = Invocable.ofClass(type);
    }

    public enum Adapter {
        CATNIP,
        DISCORD4J,
        JAVACORD,
        JDA;

        private static Adapter getFromBaseClass(Class<? extends BotBase> baseClass) {
            return BotBase.superclasses.stream()
                    .filter(s -> s.isAssignableFrom(baseClass))
                    .findAny()
                    .map(t -> ReflectionHelper.fieldByName(t, null, "ADAPTER", Adapter.class))
                    .orElseThrow(() -> new NoSuchElementException("Could not determine Adapter type from class: " + baseClass));
        }
    }
}

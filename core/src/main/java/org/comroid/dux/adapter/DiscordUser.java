package org.comroid.dux.adapter;

import org.comroid.mutatio.ref.Reference;

import java.util.function.LongFunction;

public final class DiscordUser<USR> implements DiscordEntity {
    private final LibraryAdapter<? super USR, ?, ?, USR, ?> adapter;
    private final long id;
    private final Reference<USR> reference;

    public long getID() {
        return id;
    }

    public boolean isHuman() {
        return reference.into(adapter::isHumanUser);
    }

    public DiscordUser(LibraryAdapter<? super USR, ?, ?, USR, ?> adapter, long id, LongFunction<USR> usrSupplier) {
        this.adapter = adapter;
        this.id = id;
        this.reference = Reference.provided(() -> usrSupplier.apply(this.id));
    }
}

package org.comroid.dux.adapter;

import org.comroid.mutatio.ref.Reference;

import java.util.function.LongFunction;

public final class DiscordUser<USR> implements DiscordEntity<USR> {
    private final LibraryAdapter<? super USR, ?, ?, USR, ?> adapter;
    private final long id;
    private final Reference<USR> reference;

    public long getID() {
        return id;
    }

    @Override
    public Reference<USR> getParentReference() {
        return reference;
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

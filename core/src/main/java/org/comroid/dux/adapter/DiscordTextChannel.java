package org.comroid.dux.adapter;

import org.comroid.mutatio.ref.Reference;

import java.util.function.LongFunction;

public final class DiscordTextChannel<TXT> implements DiscordEntity<TXT> {
    private final LibraryAdapter<? super TXT, ?, TXT, ?, ?> adapter;
    private final long id;
    private final Reference<TXT> reference;

    @Override
    public Reference<TXT> getParentReference() {
        return reference;
    }

    public long getID() {
        return id;
    }

    public DiscordTextChannel(LibraryAdapter<? super TXT, ?, TXT, ?, ?> adapter, long id, LongFunction<TXT> txtSupplier) {
        this.adapter = adapter;
        this.id = id;
        this.reference = Reference.provided(() -> txtSupplier.apply(this.id));
    }
}

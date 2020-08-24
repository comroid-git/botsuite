package org.comroid.dux.adapter;

import org.comroid.mutatio.ref.Reference;

import java.util.function.LongFunction;

public final class DiscordTextChannel<TXT> implements DiscordEntity {
    private final LibraryAdapter<? super TXT, ?, TXT, ?, ?> adapter;
    private final long id;
    private final Reference<TXT> reference;

    public long getID() {
        return id;
    }

    public DiscordTextChannel(LibraryAdapter<? super TXT, ?, TXT, ?, ?> adapter, long id, LongFunction<TXT> txtSupplier) {
        this.adapter = adapter;
        this.id = id;
        this.reference = Reference.provided(() -> txtSupplier.apply(this.id));
    }
}

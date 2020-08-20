package org.comroid.dux.model;

import org.comroid.dux.abstr.LibraryAdapter;
import org.comroid.mutatio.ref.Reference;

import java.util.function.LongFunction;

public final class DiscordMessage<MSG> {
    private final LibraryAdapter<? super MSG, ?, ?, ?, MSG> adapter;
    private final long id;
    private final Reference<MSG> reference;

    public long getID() {
        return id;
    }

    public String getContent() {
        return reference.into(adapter::getMessageContent);
    }

    public DiscordMessage(LibraryAdapter<? super MSG, ?, ?, ?, MSG> adapter, long id, LongFunction<MSG> msgSupplier) {
        this.adapter = adapter;
        this.id = id;
        this.reference = Reference.provided(() -> msgSupplier.apply(this.id));
    }
}

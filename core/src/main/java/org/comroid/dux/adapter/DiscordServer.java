package org.comroid.dux.adapter;

import org.comroid.dux.abstr.LibraryAdapter;
import org.comroid.mutatio.ref.Reference;

import java.util.function.LongFunction;

public final class DiscordServer<SRV> {
    private final LibraryAdapter<? super SRV, SRV, ?, ?, ?> adapter;
    private final long id;
    private final Reference<SRV> reference;

    public long getID() {
        return id;
    }

    public DiscordServer(LibraryAdapter<? super SRV, SRV, ?, ?, ?> adapter, long id, LongFunction<SRV> srvSupplier) {
        this.adapter = adapter;
        this.id = id;
        this.reference = Reference.provided(() -> srvSupplier.apply(this.id));
    }
}

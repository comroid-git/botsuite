package org.comroid.botsuite;

import org.comroid.common.io.FileHandle;
import org.comroid.dux.adapter.LibraryAdapter;
import org.comroid.mutatio.span.Span;
import org.comroid.uniform.SerializationAdapter;

import java.util.Set;

public abstract class BotBase<BASE, SRV extends BASE, TXT extends BASE, USR extends BASE, MSG extends BASE> {
    protected static final Span<Class<? extends BotBase>> superclasses = new Span<>();
    protected final SerializationAdapter<?, ?, ?> serializationAdapter;
    protected final LibraryAdapter<BASE, SRV, TXT, USR, MSG> libraryAdapter;
    protected final FileHandle dataDir;

    protected BotBase(
            SerializationAdapter<?, ?, ?> serializationAdapter,
            LibraryAdapter<BASE, SRV, TXT, USR, MSG> libraryAdapter,
            FileHandle dataDir
    ) {
        this.serializationAdapter = serializationAdapter;
        this.libraryAdapter = libraryAdapter;
        this.dataDir = dataDir;
    }

    public FileHandle getDataDir() {
        return dataDir;
    }
}

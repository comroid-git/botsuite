package org.comroid.botsuite;

import org.comroid.common.io.FileHandle;
import org.comroid.dux.adapter.LibraryAdapter;
import org.comroid.uniform.SerializationAdapter;

public abstract class BotBase<BASE, SRV extends BASE, TXT extends BASE, USR extends BASE, MSG extends BASE> {
    protected final SerializationAdapter<?, ?, ?> serializationAdapter;
    protected final LibraryAdapter<BASE, SRV, TXT, USR, MSG> libraryAdapter;
    protected final FileHandle dataDir;

    public SerializationAdapter<?, ?, ?> getSerializationAdapter() {
        return serializationAdapter;
    }

    public LibraryAdapter<BASE, SRV, TXT, USR, MSG> getLibraryAdapter() {
        return libraryAdapter;
    }

    public FileHandle getDataDir() {
        return dataDir;
    }

    protected BotBase(
            SerializationAdapter<?, ?, ?> serializationAdapter,
            LibraryAdapter<BASE, SRV, TXT, USR, MSG> libraryAdapter,
            FileHandle dataDir
    ) {
        this.serializationAdapter = serializationAdapter;
        this.libraryAdapter = libraryAdapter;
        this.dataDir = dataDir;
    }
}

package org.comroid.botsuite;

import org.comroid.api.ContextualProvider;
import org.comroid.common.io.FileHandle;
import org.comroid.dux.adapter.LibraryAdapter;
import org.comroid.uniform.SerializationAdapter;

public abstract class BotBase<BASE, SRV extends BASE, TXT extends BASE, USR extends BASE, MSG extends BASE>
        implements ContextualProvider.Underlying {
    protected final LibraryAdapter<BASE, SRV, TXT, USR, MSG> libraryAdapter;
    protected final FileHandle dataDir;
    private final ContextualProvider context;

    public LibraryAdapter<BASE, SRV, TXT, USR, MSG> getLibraryAdapter() {
        return libraryAdapter;
    }

    public FileHandle getDataDir() {
        return dataDir;
    }

    @Override
    public ContextualProvider getUnderlyingContextualProvider() {
        return context;
    }

    protected BotBase(
            ContextualProvider context,
            LibraryAdapter<BASE, SRV, TXT, USR, MSG> libraryAdapter,
            FileHandle dataDir
    ) {
        this.context = ContextualProvider.create(context, libraryAdapter);
        this.libraryAdapter = libraryAdapter;
        this.dataDir = dataDir;
    }
}

package org.comroid.dux.ui.io;

import org.comroid.common.ref.Named;
import org.comroid.dux.adapter.LibraryAdapter;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public abstract class CombinedAction<R, TXT, USR, MSG> extends DiscordDisplayable<TXT, MSG>
        implements InputSequence<R, USR, MSG>, Named {
    private final DiscordDisplayable<TXT, MSG> displayable;

    @Override
    public LibraryAdapter<Object, Object, TXT, Object, MSG> getAdapter() {
        return displayable.getAdapter();
    }

    protected CombinedAction(DiscordDisplayable<TXT, MSG> displayable) {
        this.displayable = displayable;
    }

    public abstract @Nullable String findFollowupKey(R forReponse);

    @Override
    public CompletableFuture<MSG> sendInto(TXT channel) {
        return displayable.sendInto(channel);
    }

    @Override
    public CompletableFuture<MSG> updateContent(MSG oldMessage) {
        return displayable.updateContent(oldMessage);
    }
}

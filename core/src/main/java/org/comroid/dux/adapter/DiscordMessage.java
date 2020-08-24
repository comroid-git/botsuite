package org.comroid.dux.adapter;

import org.comroid.mutatio.ref.Reference;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.LongFunction;

public final class DiscordMessage<MSG> implements DiscordEntity<MSG> {
    private final LibraryAdapter<? super MSG, ?, ?, ?, MSG> adapter;
    private final long id;
    private final Reference<MSG> reference;

    @Override
    public long getID() {
        return id;
    }

    @Override
    public Reference<MSG> getParentReference() {
        return reference;
    }

    public String getContent() {
        return reference.into(adapter::getMessageContent);
    }

    public DiscordMessage(LibraryAdapter<? super MSG, ?, ?, ?, MSG> adapter, long id, LongFunction<MSG> msgSupplier) {
        this.adapter = adapter;
        this.id = id;
        this.reference = Reference.provided(() -> msgSupplier.apply(this.id));
    }

    public CompletableFuture<?> addReaction(String emoji) {
        return reference.into(msg -> adapter.addReactionToMessage(msg, emoji));
    }

    public Runnable listenForReactions(BiConsumer<Long, String> handler) {
        return reference.into(msg -> adapter.listenForReactions(msg, handler));
    }

    public DiscordTextChannel<?> getChannel() {
        return reference.into(adapter::getChannelOfMessage);
    }
}

package org.comroid.dux.adapter;

import org.comroid.api.ContextualTypeProvider;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.uniform.SerializationAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface LibraryAdapter<BASE, SRV extends BASE, TXT extends BASE, USR extends BASE, MSG extends BASE> extends ContextualTypeProvider<SerializationAdapter<?, ?, ?>> {
    @Deprecated
    SerializationAdapter<?, ?, ?> getSerializationAdapter();

    DiscordDisplayable<TXT, MSG> wrapIntoDisplayable(Object display);

    long getID(BASE of);

    DiscordServer<SRV> getServerByID(long id);

    DiscordTextChannel<TXT> getTextChannelByID(long id);

    CompletableFuture<MSG> send(TXT channel, String message);

    Runnable listenForMessages(TXT channel, BiConsumer<Long, String> handler);

    DiscordUser<USR> getUserByID(long id);

    boolean isHumanUser(USR usr);

    DiscordMessage<MSG> getMessageByID(long chlID, long msgID);

    String getMessageContent(MSG message);

    CompletableFuture<?> addReactionsToMessage(MSG msg, String... emoji);

    Runnable listenForReactions(MSG msg, BiConsumer<Long, String> handler);

    boolean isMostRecentMessage(MSG msg);

    CompletableFuture<Void> deleteMessage(MSG msg);

    DiscordTextChannel<TXT> getChannelOfMessage(MSG message);
}

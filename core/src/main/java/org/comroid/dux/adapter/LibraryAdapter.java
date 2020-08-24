package org.comroid.dux.adapter;

import org.comroid.dux.model.ActionGenerator;
import org.comroid.uniform.SerializationAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface LibraryAdapter<BASE, SRV extends BASE, TXT extends BASE, USR extends BASE, MSG extends BASE> extends ActionGenerator<TXT, USR, MSG> {
    SerializationAdapter<?,?,?> getSerializationAdapter();

    long getID(BASE of);

    DiscordServer<SRV> getServerByID(long id);

    DiscordTextChannel<TXT> getTextChannelByID(long id);

    CompletableFuture<MSG> send(TXT channel, String message);

    DiscordUser<USR> getUserByID(long id);

    boolean isHumanUser(USR usr);

    DiscordMessage<MSG> getMessageByID(long chlID, long msgID);

    String getMessageContent(MSG message);

    CompletableFuture<?> addReactionToMessage(MSG msg, String emoji);

    Runnable listenForReactions(MSG msg, BiConsumer<Long, String> handler);

    boolean isMostRecentMessage(MSG msg);

    CompletableFuture<Void> deleteMessage(MSG msg);

    DiscordTextChannel<TXT> getChannelOfMessage(MSG message);
}

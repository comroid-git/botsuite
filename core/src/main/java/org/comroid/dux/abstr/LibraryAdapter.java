package org.comroid.dux.abstr;

import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordServer;
import org.comroid.dux.adapter.DiscordTextChannel;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.model.ActionGenerator;
import org.comroid.uniform.SerializationAdapter;

import java.util.concurrent.CompletableFuture;

public interface LibraryAdapter<BASE, SRV extends BASE, TXT extends BASE, USR extends BASE, MSG extends BASE> extends ActionGenerator<TXT, USR, MSG> {
    SerializationAdapter<?,?,?> getSerializationAdapter();

    long getID(BASE of);

    DiscordServer<SRV> getServerByID(long id);

    DiscordTextChannel<TXT> getTextChannelByID(long id);

    DiscordUser<USR> getUserByID(long id);

    boolean isHumanUser(USR usr);

    DiscordMessage<MSG> getMessageByID(long chlID, long msgID);

    String getMessageContent(MSG message);

    boolean isMostRecentMessage(MSG msg);

    CompletableFuture<Void> deleteMessage(MSG msg);

    DiscordTextChannel<TXT> getChannelOfMessage(MSG message);
}

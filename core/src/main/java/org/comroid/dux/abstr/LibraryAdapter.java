package org.comroid.dux.abstr;

import org.comroid.dux.model.DiscordMessage;
import org.comroid.dux.model.DiscordServer;
import org.comroid.dux.model.DiscordTextChannel;
import org.comroid.dux.model.DiscordUser;

public interface LibraryAdapter<BASE, SRV extends BASE, TXT extends BASE, USR extends BASE, MSG extends BASE> {
    long getID(BASE of);

    DiscordServer<SRV> getServerByID(long id);

    DiscordTextChannel<TXT> getTextChannelByID(long id);

    DiscordUser<USR> getUserByID(long id);

    boolean isHumanUser(USR usr);

    DiscordMessage<MSG> getMessageByID(long chlID, long msgID);

    String getMessageContent(MSG message);

    DiscordTextChannel<TXT> getChannelOfMessage(MSG message);
}

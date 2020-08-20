package org.comroid.dux;

import org.comroid.api.Polyfill;
import org.comroid.dux.abstr.LibraryAdapter;
import org.comroid.dux.builder.FormBuilder;
import org.comroid.dux.model.DiscordMessage;
import org.comroid.dux.model.DiscordServer;
import org.comroid.dux.model.DiscordTextChannel;
import org.comroid.dux.model.DiscordUser;
import org.comroid.uniform.HeldType;

public final class DiscordUX<SRV, TXT, USR, MSG> {
    private final LibraryAdapter<Object, SRV, TXT, USR, MSG> adapter;

    private DiscordUX(LibraryAdapter<?, ?, ?, ?, ?> adapter) {
        this.adapter = Polyfill.uncheckedCast(adapter);
    }

    public static <BASE, SRV extends BASE, TXT extends BASE, USR extends BASE, MSG extends BASE>
    DiscordUX<SRV, TXT, USR, MSG> create(
            LibraryAdapter<BASE, SRV, TXT, USR, MSG> adapter
    ) {
        return new DiscordUX<>(adapter);
    }

    public <R> FormBuilder<SRV, TXT, USR, MSG, R> createFormBuilder(TXT inChannel, HeldType<R> resultType) {
        return new FormBuilder<>(adapter, convertTXT(inChannel), resultType);
    }

    private DiscordServer<SRV> convertSRV(SRV srv) {
        return adapter.getServerByID(adapter.getID(srv));
    }

    private DiscordTextChannel<TXT> convertTXT(TXT txt) {
        return adapter.getTextChannelByID(adapter.getID(txt));
    }

    private DiscordUser<USR> convertUSR(USR usr) {
        return adapter.getUserByID(adapter.getID(usr));
    }

    private DiscordMessage<MSG> convertMSG(MSG msg) {
        return adapter.getMessageByID(adapter.getChannelOfMessage(msg).getID(), adapter.getID(msg));
    }
}

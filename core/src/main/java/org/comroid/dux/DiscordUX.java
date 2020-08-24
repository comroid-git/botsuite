package org.comroid.dux;

import org.comroid.api.Polyfill;
import org.comroid.common.ref.Named;
import org.comroid.dux.adapter.LibraryAdapter;
import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordServer;
import org.comroid.dux.adapter.DiscordTextChannel;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.form.DiscordForm;
import org.comroid.dux.model.ActionGenerator;
import org.comroid.dux.model.AdapterHolder;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.uniform.HeldType;

public final class DiscordUX<SRV, TXT, USR, MSG> implements AdapterHolder<SRV, TXT, USR, MSG>, ActionGenerator<TXT, USR, MSG> {
    private final LibraryAdapter<Object, SRV, TXT, USR, MSG> adapter;

    @Override
    public LibraryAdapter<Object, SRV, TXT, USR, MSG> getAdapter() {
        return adapter;
    }

    private DiscordUX(LibraryAdapter<?, ?, ?, ?, ?> adapter) {
        this.adapter = Polyfill.uncheckedCast(adapter);
    }

    public static <BASE, SRV extends BASE, TXT extends BASE, USR extends BASE, MSG extends BASE> DiscordUX<SRV, TXT, USR, MSG> create(
            LibraryAdapter<BASE, SRV, TXT, USR, MSG> adapter
    ) {
        return new DiscordUX<>(adapter);
    }

    @Override
    public DiscordDisplayable<TXT, MSG> output(Object display) {
        return adapter.output(display);
    }

    @Override
    public <R> InputSequence<R, USR, MSG> input(HeldType<R> resultType) {
        return adapter.input(resultType);
    }

    @Override
    public <R extends Enum<R> & Named> InputSequence<R, USR, MSG> enumInput(Class<R> ofEnum) {
        return adapter.enumInput(ofEnum);
    }

    public DiscordForm<SRV, TXT, USR, MSG> createFormBuilder(TXT inChannel) {
        return new DiscordForm<>(adapter, convertTXT(inChannel));
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

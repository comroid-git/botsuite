package org.comroid.dux;

import org.comroid.api.HeldType;
import org.comroid.api.Named;
import org.comroid.dux.adapter.*;
import org.comroid.dux.form.DiscordForm;
import org.comroid.dux.model.AdapterHolder;
import org.comroid.dux.ui.input.EnumInputSequence;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.input.StandardInputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.uniform.ValueType;

import static org.comroid.api.Polyfill.uncheckedCast;

public final class DiscordUX<SRV, TXT, USR, MSG> implements AdapterHolder<SRV, TXT, USR, MSG> {
    private final LibraryAdapter<Object, SRV, TXT, USR, MSG> adapter;

    @Override
    public LibraryAdapter<Object, SRV, TXT, USR, MSG> getAdapter() {
        return adapter;
    }

    private DiscordUX(LibraryAdapter<?, ?, ?, ?, ?> adapter) {
        this.adapter = uncheckedCast(adapter);
    }

    public static <BASE, SRV extends BASE, TXT extends BASE, USR extends BASE, MSG extends BASE> DiscordUX<SRV, TXT, USR, MSG> create(
            LibraryAdapter<BASE, SRV, TXT, USR, MSG> adapter
    ) {
        return new DiscordUX<>(adapter);
    }

    public DiscordDisplayable<TXT, MSG> output(Object display) {
        return adapter.wrapIntoDisplayable(display);
    }

    public <R> InputSequence<R, USR, MSG> input(HeldType<R> resultType) {
        if (resultType.equals(ValueType.STRING))
            return uncheckedCast(new StandardInputSequence.OfString<>(this));
        if (resultType.equals(ValueType.BOOLEAN))
            return uncheckedCast(new StandardInputSequence.OfBoolean<>(this));
        throw new UnsupportedOperationException(String.format("Unrecognized result type: %s", resultType));
    }

    public <R extends Enum<R> & Named> InputSequence<R, USR, MSG> enumInput(Class<R> ofEnum) {
        return new EnumInputSequence.SingleYield<>(ofEnum);
    }

    public DiscordForm<SRV, TXT, USR, MSG> createForm() {
        return new DiscordForm<>(this);
    }

    public DiscordServer<SRV> convertSRV(SRV srv) {
        return adapter.getServerByID(adapter.getID(srv));
    }

    public DiscordTextChannel<TXT> convertTXT(TXT txt) {
        return adapter.getTextChannelByID(adapter.getID(txt));
    }

    public DiscordUser<USR> convertUSR(USR usr) {
        return adapter.getUserByID(adapter.getID(usr));
    }

    public DiscordMessage<MSG> convertMSG(MSG msg) {
        return adapter.getMessageByID(adapter.getChannelOfMessage(msg).getID(), adapter.getID(msg));
    }

    public DiscordDisplayable<TXT, MSG> wrapIntoDisplayable(Object label) {
        if (label instanceof DiscordDisplayable)
            //noinspection unchecked
            return (DiscordDisplayable<TXT, MSG>) label;
        return getAdapter().wrapIntoDisplayable(label);
    }
}

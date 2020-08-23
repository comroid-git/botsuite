package org.comroid.dux.model;

import org.comroid.common.ref.Named;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.uniform.HeldType;

public interface ActionGenerator<TXT, USR, MSG> {
    DiscordDisplayable<TXT, MSG> output(Object display);

    <R> InputSequence<R, USR, MSG> input(HeldType<R> resultType);

    default<R extends Enum<R> & Named & EmojiHolder> InputSequence<R, USR, MSG> enumInput(Class<R> ofEnum) {
        return enumInput(ofEnum, "✅");
    }

    <R extends Enum<R> & Named & EmojiHolder> InputSequence<R, USR, MSG> enumInput(Class<R> ofEnum, String confirmationEmoji);
}

package org.comroid.dux.model;

import org.comroid.dux.ui.input.DiscordInputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.uniform.HeldType;

public interface ActionGenerator<TXT, USR, MSG> {
    DiscordDisplayable<TXT, MSG> output(Object display);

    <R> DiscordInputSequence<R, TXT, USR, MSG> input(HeldType<R> resultType);
}

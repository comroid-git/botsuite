package org.comroid.dux.model;

import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.uniform.HeldType;

public interface ActionGenerator<TXT, USR, MSG> {
    DiscordDisplayable<TXT, MSG> output(Object display);

    <R> InputSequence<R, USR> input(HeldType<R> resultType);
}

package org.comroid.dux.form;

import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class FormStage<R, TXT, USR, MSG> {
    protected final String key;
    protected final DiscordDisplayable<TXT, MSG> displayable;
    protected final InputSequence<R, USR, MSG> inputSequence;
    protected final Function<R, @Nullable String> nextKeyResolver;

    public FormStage(
            String key,
            DiscordDisplayable<TXT, MSG> displayable,
            InputSequence<R, USR, MSG> inputSequence,
            Function<R, @Nullable String> nextKeyResolver
    ) {
        this.key = key;
        this.displayable = displayable;
        this.inputSequence = inputSequence;
        this.nextKeyResolver = nextKeyResolver;
    }
}

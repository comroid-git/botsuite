package org.comroid.dux.form;

import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.io.CombinedAction;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.api.HeldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class FormStage<R, TXT, USR, MSG> extends CombinedAction<R, TXT, USR, MSG> {
    protected final String key;
    protected final InputSequence<R, USR, MSG> inputSequence;
    protected final Function<R, @Nullable String> nextKeyResolver;

    @Override
    public String getName() {
        return key;
    }

    @Override
    public HeldType<R> getResultType() {
        return inputSequence.getResultType();
    }

    public FormStage(
            String key,
            DiscordDisplayable<TXT, MSG> displayable,
            InputSequence<R, USR, MSG> inputSequence,
            Function<R, @Nullable String> nextKeyResolver
    ) {
        super(displayable);

        this.key = key;
        this.inputSequence = inputSequence;
        this.nextKeyResolver = nextKeyResolver;
    }

    @Override
    public CompletableFuture<R> listen(@NotNull CompletableFuture<?> abortionFuture, @Nullable DiscordUser<USR> targetUser, DiscordMessage<MSG> displayMessage) {
        return inputSequence.listen(abortionFuture, targetUser, displayMessage);
    }

    @Override
    public @Nullable String findFollowupKey(R forReponse) {
        return nextKeyResolver.apply(forReponse);
    }
}

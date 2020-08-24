package org.comroid.dux.form;

import org.comroid.dux.adapter.DiscordTextChannel;
import org.comroid.dux.adapter.LibraryAdapter;
import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.io.CombinedAction;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.uniform.HeldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class FormStage<R, TXT, USR, MSG> extends CombinedAction<R, TXT, USR, MSG> {
    protected final String key;
    protected final DiscordDisplayable<TXT, MSG> displayable;
    protected final InputSequence<R, USR, MSG> inputSequence;
    protected final Function<R, @Nullable String> nextKeyResolver;

    @Override
    public HeldType<R> getResultType() {
        return inputSequence.getResultType();
    }

    @Override
    public LibraryAdapter<Object, Object, TXT, Object, MSG> getAdapter() {
        return displayable.getAdapter();
    }

    @Override
    public CompletableFuture<R> listen(@NotNull CompletableFuture<?> abortionFuture, DiscordUser<USR> targetUser, DiscordMessage<MSG> displayMessage) {
        return inputSequence.listen(abortionFuture, targetUser, displayMessage);
    }

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

    @Override
    public CompletableFuture<MSG> sendInto(DiscordTextChannel<TXT> channel) {
        return displayable.sendInto(channel);
    }

    @Override
    public CompletableFuture<MSG> updateContent(DiscordMessage<MSG> oldMessage) {
        return displayable.updateContent(oldMessage);
    }
}

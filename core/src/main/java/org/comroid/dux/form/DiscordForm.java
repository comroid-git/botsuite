package org.comroid.dux.form;

import org.comroid.dux.DiscordUX;
import org.comroid.dux.adapter.DiscordTextChannel;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.io.CombinedAction;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.mutatio.span.Span;
import org.comroid.uniform.node.UniObjectNode;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class DiscordForm<SRV, TXT, USR, MSG> {
    public final DiscordUX<SRV, TXT, USR, MSG> dux;
    private final DiscordTextChannel<TXT> inChannel;
    final Span<CombinedAction<?, TXT, USR, MSG>> stages;

    public DiscordForm(DiscordUX<SRV, TXT, USR, MSG> dux,
                       DiscordTextChannel<TXT> inChannel) {
        this.dux = dux;
        this.inChannel = inChannel;
        this.stages = new Span<>();
    }

    public final <R> DiscordForm<SRV, TXT, USR, MSG> addStage(
            String key,
            DiscordDisplayable<TXT, MSG> displayable,
            InputSequence<R, USR, MSG> inputSequence
    ) {
        return addStage(key, displayable, inputSequence, any -> null);
    }

    public final <R> DiscordForm<SRV, TXT, USR, MSG> addStage(
            String key,
            DiscordDisplayable<TXT, MSG> displayable,
            InputSequence<R, USR, MSG> inputSequence,
            Function<R, @Nullable String> nextKeyResolver
    ) {
        return addStage(new FormStage<>(key, displayable, inputSequence, nextKeyResolver));
    }

    public final <R> DiscordForm<SRV, TXT, USR, MSG> addStage(FormStage<R, TXT, USR, MSG> stage) {
        stages.add(stage);
        return this;
    }

    public final CompletableFuture<UniObjectNode> execute(TXT inChannel, USR targetUser) {
        return new FormExecutor<>(this, inChannel, targetUser);
    }

    public int stageCount() {
        return stages.size();
    }
}

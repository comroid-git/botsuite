package org.comroid.dux.form;

import org.comroid.common.ref.Named;
import org.comroid.dux.DiscordUX;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.io.CombinedAction;
import org.comroid.dux.ui.io.EnumSelection;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.mutatio.span.Span;
import org.comroid.uniform.HeldType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class DiscordForm<SRV, TXT, USR, MSG> {
    public final DiscordUX<SRV, TXT, USR, MSG> dux;
    final Span<CombinedAction<?, TXT, USR, MSG>> stages;

    public DiscordForm(DiscordUX<SRV, TXT, USR, MSG> dux) {
        this.dux = dux;
        this.stages = new Span<>();
    }

    public final <R> DiscordForm<SRV, TXT, USR, MSG> addStage(
            String key,
            Object label,
            HeldType<R> resultType
    ) {
        return addStage(key, dux.wrapIntoDisplayable(label), resultType, any -> null);
    }

    public final <R> DiscordForm<SRV, TXT, USR, MSG> addStage(
            String key,
            Object label,
            HeldType<R> resultType,
            Function<R, @Nullable String> nextKeyResolver
    ) {
        return addStage(key, dux.wrapIntoDisplayable(label), dux.input(resultType), nextKeyResolver);
    }

    public final <R> DiscordForm<SRV, TXT, USR, MSG> addStage(
            String key,
            DiscordDisplayable<TXT, MSG> displayable,
            InputSequence<R, USR, MSG> inputSequence,
            Function<R, @Nullable String> nextKeyResolver
    ) {
        return addStage(new FormStage<>(key, displayable, inputSequence, nextKeyResolver));
    }

    public final <R> DiscordForm<SRV, TXT, USR, MSG> addStage(CombinedAction<R, TXT, USR, MSG> stage) {
        stages.add(stage);
        return this;
    }

    public <R extends Enum<R> & Named> DiscordForm<SRV, TXT, USR, MSG> addEnumSelection(
            String key,
            Class<R> ofEnum,
            Object label
    ) {
        return addEnumSelection(key, ofEnum, label, any -> null);
    }

    public <R extends Enum<R> & Named> DiscordForm<SRV, TXT, USR, MSG> addEnumSelection(
            String key,
            Class<R> ofEnum,
            Object label,
            Function<R, @Nullable String> nextKeyResolver
    ) {
        return addStage(new EnumSelection<>(dux, key, label, ofEnum, nextKeyResolver));
    }

    public final FormExecutor<TXT, USR, MSG> execute(TXT inChannel, USR targetUser) {
        return new FormExecutor<>(this, inChannel, targetUser);
    }

    public int stageCount() {
        return stages.size();
    }
}

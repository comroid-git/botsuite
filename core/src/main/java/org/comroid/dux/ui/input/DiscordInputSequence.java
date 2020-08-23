package org.comroid.dux.ui.input;

import org.comroid.dux.model.AdapterHolder;
import org.comroid.uniform.HeldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public abstract class DiscordInputSequence<R, TXT, USR, MSG> implements AdapterHolder<Object, TXT, USR, MSG> {
    public final HeldType<R> outputType;

    protected DiscordInputSequence(HeldType<R> outputType) {
        this.outputType = outputType;
    }

    public CompletableFuture<R> listen(USR targetUser) {
        return listen(new CompletableFuture<>(), targetUser);
    }

    public abstract CompletableFuture<R> listen(@NotNull CompletableFuture<?> abortionFuture, @Nullable USR targetUser);
}

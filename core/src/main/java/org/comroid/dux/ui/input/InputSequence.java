package org.comroid.dux.ui.input;

import org.comroid.uniform.HeldType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface InputSequence<R, USR> {
    HeldType<R> getResultType();

    default CompletableFuture<R> listen(USR targetUser) {
        return listen(new CompletableFuture<>(), targetUser);
    }

    CompletableFuture<R> listen(@NotNull CompletableFuture<?> abortionFuture, USR targetUser);
}

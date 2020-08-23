package org.comroid.dux.ui.input;

import org.comroid.uniform.HeldType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface InputSequence<R, USR, MSG> {
    HeldType<R> getResultType();

    default CompletableFuture<R> listen(USR targetUser, MSG displayMessage) {
        return listen(new CompletableFuture<>(), targetUser, displayMessage);
    }

    CompletableFuture<R> listen(@NotNull CompletableFuture<?> abortionFuture, USR targetUser, MSG displayMessage);
}

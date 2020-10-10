package org.comroid.dux.ui.input;

import org.comroid.api.HeldType;
import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface InputSequence<R, USR, MSG> {
    HeldType<R> getResultType();

    default CompletableFuture<R> listen(DiscordUser<USR> targetUser, DiscordMessage<MSG> displayMessage) {
        return listen(new CompletableFuture<>(), targetUser, displayMessage);
    }

    CompletableFuture<R> listen(@NotNull CompletableFuture<?> abortionFuture, @Nullable DiscordUser<USR> targetUser, DiscordMessage<MSG> displayMessage);
}

package org.comroid.dux.javacord;

import org.comroid.dux.ui.input.InputSequence;
import org.comroid.uniform.HeldType;
import org.comroid.uniform.ValueType;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public final class JavacordInputSequence {
    public static final class OfString implements InputSequence<String, User, MSG> {
        private final CompletableFuture<String> future = new CompletableFuture<>();
        private final JavacordDUX adapter;

        @Override
        public HeldType<String> getResultType() {
            return ValueType.STRING;
        }

        public OfString(JavacordDUX javacordDUX) {
            this.adapter = javacordDUX;
        }

        @Override
        public CompletableFuture<String> listen(@NotNull CompletableFuture<?> abortionFuture, @Nullable User targetUser, MSG displayMessage) {
            class OnceListener implements MessageCreateListener, Closeable {
                public final CompletableFuture<String> future = new CompletableFuture<>();
                private final long targetUserId;

                OnceListener(DiscordApi api, long targetUserId) {
                    this.targetUserId = targetUserId;
                    future.thenRun(api.addMessageCreateListener(this)::remove);
                    abortionFuture.thenRun(this::close);
                }

                @Override
                public void close() {
                    if (!future.isDone())
                        future.completeExceptionally(new RuntimeException("Input Aborted"));
                }

                @Override
                public void onMessageCreate(MessageCreateEvent event) {
                    if (event.getMessageAuthor().getId() == targetUserId)
                        future.complete(event.getReadableMessageContent());
                }
            }

            return new OnceListener(adapter.api, targetUser == null ? -1 : targetUser.getId()).future;
        }
    }
}

package org.comroid.dux.javacord;

import org.comroid.api.Junction;
import org.comroid.common.ref.Named;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.mutatio.span.Span;
import org.comroid.uniform.HeldType;
import org.comroid.uniform.ValueType;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveAllEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public final class JavacordInputSequence {
    public static final class OfString implements InputSequence<String, User, Message> {
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
        public CompletableFuture<String> listen(@NotNull CompletableFuture<?> abortionFuture, @Nullable User targetUser, Message displayMessage) {
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
                    if (targetUserId == -1 || event.getMessageAuthor().getId() == targetUserId)
                        future.complete(event.getReadableMessageContent());
                }
            }

            return new OnceListener(adapter.api, targetUser == null ? -1 : targetUser.getId()).future;
        }
    }
}

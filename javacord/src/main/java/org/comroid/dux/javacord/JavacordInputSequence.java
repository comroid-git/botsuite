package org.comroid.dux.javacord;

import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.uniform.HeldType;
import org.comroid.uniform.ValueType;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public final class JavacordInputSequence {
    public static final class OfString implements InputSequence<String, User, Message> {
        private final JavacordDUX adapter;

        @Override
        public HeldType<String> getResultType() {
            return ValueType.STRING;
        }

        public OfString(JavacordDUX javacordDUX) {
            this.adapter = javacordDUX;
        }

        @Override
        public CompletableFuture<String> listen(@NotNull CompletableFuture<?> abortionFuture, DiscordUser<User> targetUser, DiscordMessage<Message> displayMessage) {
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
                    if (event.getMessageAuthor().isYourself()) return;
                    if (targetUserId == -1 || event.getMessageAuthor().getId() == targetUserId)
                        future.complete(event.getReadableMessageContent());
                }
            }

            return new OnceListener(adapter.api, targetUser == null ? -1 : targetUser.getId()).future;
        }
    }

    public static final class OfBoolean implements InputSequence<Boolean, User, Message> {
        public static final String YES_EMOJI = "✅";
        public static final String NO_EMOJI = "❌";
        private final JavacordDUX adapter;

        @Override
        public HeldType<Boolean> getResultType() {
            return ValueType.BOOLEAN;
        }

        public OfBoolean(JavacordDUX adapter) {
            this.adapter = adapter;
        }

        @Override
        public CompletableFuture<Boolean> listen(@NotNull CompletableFuture<?> abortionFuture, DiscordUser<User> targetUser, DiscordMessage<Message> displayMessage) {
            class YesNoListener implements ReactionAddListener, Closeable {
                private final CompletableFuture<Boolean> future = new CompletableFuture<>();
                private final long targetUserId;

                public YesNoListener(Message displayMessage, long targetUserId) {
                    this.targetUserId = targetUserId;
                    future.thenRun(displayMessage.addReactionAddListener(this)::remove);
                    abortionFuture.thenRun(this::close);

                    displayMessage.addReactions(YES_EMOJI, NO_EMOJI);
                }

                @Override
                public void close() {
                    if (!future.isDone())
                        future.completeExceptionally(new RuntimeException("Input Aborted"));
                }

                @Override
                public void onReactionAdd(ReactionAddEvent event) {
                    if (event.getUser().isYourself()) return;
                    if (targetUserId == -1 || event.getUser().getId() == targetUserId)
                        event.getEmoji()
                                .asUnicodeEmoji()
                                .map(x -> {
                                    if (x.equals(YES_EMOJI))
                                        return true;
                                    if (x.equals(NO_EMOJI))
                                        return false;
                                    return null;
                                }).ifPresent(future::complete);
                }
            }

            return new YesNoListener(displayMessage, targetUser.getId()).future;
        }
    }
}

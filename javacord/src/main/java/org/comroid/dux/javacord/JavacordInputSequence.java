package org.comroid.dux.javacord;

import org.comroid.api.Polyfill;
import org.comroid.dux.abstr.LibraryAdapter;
import org.comroid.dux.ui.input.DiscordInputSequence;
import org.comroid.uniform.ValueType;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public final class JavacordInputSequence {
    public static final class OfString extends DiscordInputSequence<String, TextChannel, User, Message> {
        private final CompletableFuture<String> future = new CompletableFuture<>();
        private final JavacordAdapter adapter;

        @Override
        public LibraryAdapter<Object, Object, TextChannel, User, Message> getAdapter() {
            return Polyfill.uncheckedCast(adapter);
        }

        public OfString(JavacordAdapter javacordAdapter) {
            super(ValueType.STRING);

            this.adapter = javacordAdapter;
        }

        @Override
        public CompletableFuture<String> listen(@NotNull CompletableFuture<?> abortionFuture, @Nullable User targetUser) {
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

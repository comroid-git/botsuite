package org.comroid.dux.javacord;

import org.comroid.api.Junction;
import org.comroid.common.ref.Named;
import org.comroid.dux.ui.input.InputSequence;
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

    public static final class OfEnum<R extends Enum<R> & Named> implements InputSequence<R, User, Message> {
        private final Class<R> enumClass;
        private final R[] values;
        private final Junction<String, R> converter = Junction.ofString(name -> {
            for (R value : getValues()) {
                if (value.getName().equalsIgnoreCase(name))
                    return value;
            }
            return null;
        });
        private final HeldType<R> heldType = new HeldType<R>() {

            @Override
            public Junction<String, R> getConverter() {
                return converter;
            }

            @Override
            public String getName() {
                return enumClass.getSimpleName();
            }

            @Override
            public <T> T convert(R value, HeldType<T> toType) {
                if (toType.equals(ValueType.STRING))
                    //noinspection unchecked
                    return (T) value.getName();
                throw new UnsupportedOperationException();
            }
        };
        private final String confirmationEmoji;

        public R[] getValues() {
            return values;
        }

        @Override
        public HeldType<R> getResultType() {
            return heldType;
        }

        public OfEnum(Class<R> ofEnum, String confirmationEmoji) {
            this.enumClass = ofEnum;
            this.values = enumClass.getEnumConstants();
            this.confirmationEmoji = confirmationEmoji;
        }

        @Override
        public CompletableFuture<R> listen(@NotNull CompletableFuture<?> abortionFuture, User targetUser, Message displayMessage) {
            class EnumConstantsListener implements ReactionAddListener, ReactionRemoveListener, ReactionRemoveAllListener, Closeable {
                public final CompletableFuture<R> future = new CompletableFuture<>();
                private final Message displayMessage;
                private final long targetUserId;

                public EnumConstantsListener(Message displayMessage, long targetUserId) {
                    this.displayMessage = displayMessage;
                    this.targetUserId = targetUserId;
                }

                @Override
                public void onReactionAdd(ReactionAddEvent event) {
                    if (targetUserId == -1 || event.getUser().getId() == targetUserId)

                }

                @Override
                public void onReactionRemoveAll(ReactionRemoveAllEvent event) {
                }

                @Override
                public void onReactionRemove(ReactionRemoveEvent event) {
                    if (targetUserId == -1 || event.getUser().getId() == targetUserId)
                }

                @Override
                public void close() {
                    if (!future.isDone())
                        future.completeExceptionally(new RuntimeException("Input Aborted"));
                }
            }

            return new EnumConstantsListener(displayMessage, targetUser == null ? -1 : targetUser.getId()).future;
        }
    }
}

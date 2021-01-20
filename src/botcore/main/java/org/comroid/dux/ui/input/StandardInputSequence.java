package org.comroid.dux.ui.input;

import org.comroid.api.ValueType;
import org.comroid.dux.DiscordUX;
import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.ui.AbstractAction;
import org.comroid.util.StandardValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public final class StandardInputSequence {
    public static final class OfString<USR, MSG> implements InputSequence<String, USR, MSG> {
        private final DiscordUX<?, ?, USR, MSG> dux;

        @Override
        public ValueType<String> getResultType() {
            return StandardValueType.STRING;
        }

        public OfString(DiscordUX<?, ?, USR, MSG> dux) {
            this.dux = dux;
        }

        @Override
        public CompletableFuture<String> listen(@NotNull CompletableFuture<?> abortionFuture, @Nullable DiscordUser<USR> targetUser, DiscordMessage<MSG> displayMessage) {
            class OnceListener extends AbstractAction<String> {
                public final CompletableFuture<String> future = new CompletableFuture<>();

                OnceListener(DiscordMessage<MSG> msg, long targetUserId) {
                    super(targetUserId);
                    future.thenRun(displayMessage.getChannel().listenForMessages(this::handleMessageCreate));
                    abortionFuture.thenRun(this::close);
                }

                private void handleMessageCreate(long userId, String messageContent) {
                    if (isUserTargeted(userId))
                        future.complete(messageContent);
                }
            }

            return new OnceListener(displayMessage, targetUser == null ? -1 : targetUser.getID()).future;
        }
    }

    public static final class OfBoolean<USR, MSG> implements InputSequence<Boolean, USR, MSG> {
        public static final String YES_EMOJI = "✅";
        public static final String NO_EMOJI = "❌";
        private final DiscordUX<?, ?, USR, MSG> dux;

        @Override
        public ValueType<Boolean> getResultType() {
            return StandardValueType.BOOLEAN;
        }

        public OfBoolean(DiscordUX<?, ?, USR, MSG> dux) {
            this.dux = dux;
        }

        @Override
        public CompletableFuture<Boolean> listen(@NotNull CompletableFuture<?> abortionFuture, @Nullable DiscordUser<USR> targetUser, DiscordMessage<MSG> displayMessage) {
            class YesNoListener extends AbstractAction<Boolean> {
                public YesNoListener(DiscordMessage<MSG> displayMessage, long targetUserId) {
                    super(targetUserId);
                    future.thenRun(displayMessage.listenForReactions(this::handleReactionAdd));
                    abortionFuture.thenRun(this::close);

                    displayMessage.addReactions(YES_EMOJI, NO_EMOJI);
                }

                public void handleReactionAdd(long userId, String emoji) {
                    if (isUserTargeted(userId))
                        if (emoji.equals(YES_EMOJI))
                            future.complete(true);
                        else if (emoji.equals(NO_EMOJI))
                            future.complete(false);
                }
            }

            return new YesNoListener(displayMessage, targetUser.getID()).future;
        }
    }
}

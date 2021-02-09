package org.comroid.dux.ui.input;

import org.comroid.api.ValueType;
import org.comroid.api.Named;
import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.type.EnumValueType;
import org.comroid.dux.ui.AbstractAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class EnumInputSequence {
    public static final class SingleYield<R extends Enum<R> & Named, USR, MSG> implements InputSequence<R, USR, MSG> {
        private final EnumValueType<R, USR, MSG> enumType;
        private final R[] values;

        public R[] getValues() {
            return values;
        }

        @Override
        public ValueType<R> getResultType() {
            return enumType;
        }

        public SingleYield(Class<R> ofEnum) {
            this.enumType = new EnumValueType<>(ofEnum, name -> {
                for (R value : getValues()) {
                    if (value.getName().equalsIgnoreCase(name))
                        return value;
                }
                return null;
            });
            this.values = ofEnum.getEnumConstants();
        }

        private Processor<R> findValueByEmoji(String emoji) {
            return Stream.of(values)
                    .filter(r -> r.getAlternateFormattedName().equals(emoji))
                    .findAny()
                    .map(Processor::ofConstant)
                    .orElseGet(Processor::empty);
        }

        @Override
        public CompletableFuture<R> listen(@NotNull CompletableFuture<?> abortionFuture, @Nullable DiscordUser<USR> targetUser, DiscordMessage<MSG> displayMessage) {

            return new EnumConstantsListener(displayMessage, targetUser == null ? -1 : targetUser.getID()).future;
        }

        public class EnumConstantsListener extends AbstractAction<R> {
            public EnumConstantsListener(DiscordMessage<MSG> displayMessage, long targetUserId) {
                super(targetUserId);

                for (R value : values)
                    displayMessage.addReactions(value.getAlternateFormattedName());
                future.thenRun(displayMessage.listenForReactions(this::handleReactionAdd));
            }

            private void handleReactionAdd(long userId, String emoji) {
                if (!isUserTargeted(userId)) return;
                findValueByEmoji(emoji).ifPresent(future::complete);
            }
        }
    }
}

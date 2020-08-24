package org.comroid.dux.ui.input;

import org.comroid.api.Junction;
import org.comroid.api.UUIDContainer;
import org.comroid.common.Disposable;
import org.comroid.common.ref.Named;
import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.ui.AbstractAction;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.span.Span;
import org.comroid.uniform.HeldType;
import org.comroid.uniform.ValueType;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class EnumInputSequence {
    public static abstract class SingleYield<R extends Enum<R> & Named, USR, MSG> implements InputSequence<R, USR, MSG> {
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

        public R[] getValues() {
            return values;
        }

        @Override
        public HeldType<R> getResultType() {
            return heldType;
        }

        public SingleYield(Class<R> ofEnum) {
            this.enumClass = ofEnum;
            this.values = enumClass.getEnumConstants();
        }

        private Processor<R> findValueByEmoji(String emoji) {
            return Stream.of(values)
                    .filter(r -> r.getAlternateFormattedName().equals(emoji))
                    .findAny()
                    .map(Processor::ofConstant)
                    .orElseGet(Processor::empty);
        }

        @Override
        public CompletableFuture<R> listen(@NotNull CompletableFuture<?> abortionFuture, DiscordUser<USR> targetUser, DiscordMessage<MSG> displayMessage) {

            return new EnumConstantsListener(displayMessage, targetUser == null ? -1 : targetUser.getID()).future;
        }

        public class EnumConstantsListener extends AbstractAction {
            public final CompletableFuture<R> future = new CompletableFuture<>();
            private final long targetUserId;

            public EnumConstantsListener(DiscordMessage<MSG> displayMessage, long targetUserId) {
                this.targetUserId = targetUserId;

                for (R value : values)
                    displayMessage.addReaction(value.getAlternateFormattedName());
                future.thenRun(displayMessage.listenFor(this)::close);
            }

            protected void handleReactionAdd(long userId, String emoji) {
                if (targetUserId != -1 && userId != targetUserId)
                    return;
                findValueByEmoji(emoji).ifPresent(future::complete);
            }

            @Override
            public void close() {
                if (!future.isDone())
                    future.completeExceptionally(new RuntimeException("Input Aborted"));
            }
        }
    }
}

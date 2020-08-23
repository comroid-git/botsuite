package org.comroid.dux.javacord;

import org.comroid.api.Junction;
import org.comroid.common.ref.Named;
import org.comroid.dux.model.EmojiHolder;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.span.Span;
import org.comroid.uniform.HeldType;
import org.comroid.uniform.ValueType;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveAllEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.event.ListenerManager;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class JavacordEnumInputSequence {
    public static final class SingleYield<R extends Enum<R> & Named & EmojiHolder> implements InputSequence<R, User, Message> {
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
                .filter(r -> r.getPrintableEmoji().equals(emoji))
                .findAny()
                .map(Processor::ofConstant)
                .orElseGet(Processor::empty);
    }

    @Override
    public CompletableFuture<R> listen(@NotNull CompletableFuture<?> abortionFuture, User targetUser, Message displayMessage) {
        class EnumConstantsListener implements ReactionAddListener, Closeable {
            public final CompletableFuture<R> future = new CompletableFuture<>();
            private final long targetUserId;
            private final Span<ListenerManager<EnumConstantsListener>> managers = new Span<>();

            public EnumConstantsListener(Message displayMessage, long targetUserId) {
                this.targetUserId = targetUserId;

                for (R value : values)
                    displayMessage.addReaction(value.getPrintableEmoji());
                future.thenRun(displayMessage.addReactionAddListener(this)::remove);
            }

            @Override
            public void onReactionAdd(ReactionAddEvent event) {
                if (targetUserId != -1 && event.getUser().getId() != targetUserId)
                    return;
                
                findValueByEmoji(event.getEmoji().getMentionTag())
                        .ifPresent(future::complete);
            }

            @Override
            public void close() {
                if (!future.isDone())
                    future.completeExceptionally(new RuntimeException("Input Aborted"));
            }
        }

        return new EnumConstantsListener(displayMessage, targetUser == null ? -1 : targetUser.getId()).future;
    }
}}

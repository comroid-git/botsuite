package org.comroid.dux.ui.io;

import org.comroid.api.ValueType;
import org.comroid.api.Named;
import org.comroid.api.Polyfill;
import org.comroid.api.WrappedFormattable;
import org.comroid.dux.DiscordUX;
import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.adapter.LibraryAdapter;
import org.comroid.dux.type.EnumValueType;
import org.comroid.dux.ui.AbstractAction;
import org.comroid.mutatio.ref.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

public final class EnumSelection<R extends Enum<R> & Named, TXT, USR, MSG> extends CombinedAction<R, TXT, USR, MSG> {
    private final String key;
    private final R[] values;
    private final EnumValueType<R, TXT, USR> resultType;
    private final DiscordUX<?, TXT, USR, MSG> dux;
    private final Function<R, @Nullable String> followupResolver;

    @Override
    public String getName() {
        return key;
    }

    @Override
    public ValueType<R> getResultType() {
        return resultType;
    }

    @Override
    public LibraryAdapter<Object, Object, TXT, Object, MSG> getAdapter() {
        return Polyfill.uncheckedCast(dux.getAdapter());
    }

    public EnumSelection(
            DiscordUX<?, TXT, USR, MSG> dux,
            String key,
            Object label,
            Class<R> enumType,
            @Nullable Function<R, @Nullable String> followupResolver
    ) {
        super(dux.getAdapter().wrapIntoDisplayable(label));

        this.key = key;
        this.values = enumType.getEnumConstants();
        this.resultType = new EnumValueType<>(enumType, name -> Arrays.stream(values)
                .filter(v -> v.getName().equalsIgnoreCase(name))
                .findAny()
                .orElse(null));
        this.dux = dux;
        this.followupResolver = followupResolver;
    }

    @Override
    public @Nullable String findFollowupKey(R forReponse) {
        return followupResolver != null
                ? followupResolver.apply(forReponse)
                : null;
    }

    @Override
    public CompletableFuture<R> listen(@NotNull CompletableFuture<?> abortionFuture, @Nullable DiscordUser<USR> targetUser, DiscordMessage<MSG> displayMessage) {
        final Listener listener = new Listener(displayMessage, targetUser != null ? targetUser.getID() : -1);
        abortionFuture.thenRun(listener::close);
        return listener.future;
    }

    private Processor<R> findValueByEmoji(String emoji) {
        return Stream.of(values)
                .filter(r -> r.getAlternateFormattedName().equals(emoji))
                .findAny()
                .map(Processor::ofConstant)
                .orElseGet(Processor::empty);
    }

    private final class Listener extends AbstractAction<R> {
        protected Listener(DiscordMessage<MSG> display, long targetUserId) {
            super(targetUserId);

            Runnable detacher = display.listenForReactions(this::handleReactions);
            addChildren(detacher::run);
            future.thenRun(detacher);

            display.addReactions(Arrays.stream(values)
                    .map(WrappedFormattable::getAlternateFormattedName)
                    .toArray(String[]::new));
        }

        private void handleReactions(long userId, String emoji) {
            if (isUserTargeted(userId))
                findValueByEmoji(emoji).ifPresent(future::complete);
        }
    }
}

package org.comroid.dux.ui.io;

import org.comroid.api.Polyfill;
import org.comroid.common.ref.Named;
import org.comroid.dux.DiscordUX;
import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.adapter.LibraryAdapter;
import org.comroid.dux.type.EnumHeldType;
import org.comroid.uniform.HeldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class EnumSelection<R extends Enum<R> & Named, TXT, USR, MSG> extends CombinedAction<R, TXT, USR, MSG> {
    private final String key;
    private final R[] values;
    private final EnumHeldType<R, TXT, USR> resultType;
    private final DiscordUX<?, TXT, USR, MSG> dux;
    private final Function<R, @Nullable String> followupResolver;

    @Override
    public String getName() {
        return key;
    }

    @Override
    public HeldType<R> getResultType() {
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
        this.resultType = new EnumHeldType<>(enumType, name -> Arrays.stream(values)
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
        return null;
    }
}

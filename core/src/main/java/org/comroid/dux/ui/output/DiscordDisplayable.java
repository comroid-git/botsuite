package org.comroid.dux.ui.output;

import org.comroid.dux.model.AdapterHolder;
import org.comroid.mutatio.ref.FutureReference;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DiscordDisplayable<TXT, MSG> implements AdapterHolder<Object, TXT, Object, MSG>, Displayable<TXT, MSG> {
    private final Map<Long, FutureReference<MSG>> alreadySent = new ConcurrentHashMap<>();

    protected abstract CompletableFuture<MSG> sendInto(TXT channel);

    protected abstract CompletableFuture<MSG> updateContent(MSG oldMessage);

    @Override
    public final FutureReference<MSG> displayIn(final TXT channel) {
        return alreadySent.compute(getAdapter().getID(channel), (k, v) -> {
            boolean isOld = false;

            if (v == null || (isOld = !v.test(getAdapter()::isMostRecentMessage))) {
                if (isOld) v.ifPresent(getAdapter()::deleteMessage);
                return new FutureReference<>(sendInto(channel));
            } else return new FutureReference<>(v.future.thenComposeAsync(this::updateContent));
        });
    }
}

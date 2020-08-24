package org.comroid.dux.form;

import org.comroid.annotations.Blocking;
import org.comroid.api.Polyfill;
import org.comroid.dux.DiscordUX;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.adapter.DiscordTextChannel;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.span.Span;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniObjectNode;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.comroid.api.Polyfill.uncheckedCast;

public class DiscordForm<SRV, TXT, USR, MSG> {
    private final DiscordUX<SRV, TXT, USR, MSG> dux;
    private final DiscordTextChannel<TXT> inChannel;
    private final Span<FormStage<?, TXT, USR, MSG>> stages;

    public DiscordForm(DiscordUX<SRV, TXT, USR, MSG> dux,
                       DiscordTextChannel<TXT> inChannel) {
        this.dux = dux;
        this.inChannel = inChannel;
        this.stages = new Span<>();
    }

    public final <R> DiscordForm<SRV, TXT, USR, MSG> addStage(
            String key,
            DiscordDisplayable<TXT, MSG> displayable,
            InputSequence<R, USR, MSG> inputSequence
    ) {
        return addStage(key, displayable, inputSequence, any -> null);
    }

    public final <R> DiscordForm<SRV, TXT, USR, MSG> addStage(
            String key,
            DiscordDisplayable<TXT, MSG> displayable,
            InputSequence<R, USR, MSG> inputSequence,
            Function<R, @Nullable String> nextKeyResolver
    ) {
        return addStage(new FormStage<>(key, displayable, inputSequence, nextKeyResolver));
    }

    public final <R> DiscordForm<SRV, TXT, USR, MSG> addStage(FormStage<R, TXT, USR, MSG> stage) {
        stages.add(stage);
        return this;
    }

    public final CompletableFuture<UniObjectNode> execute(TXT inChannel, USR targetUser) {
        return new FormExecutor(inChannel, targetUser).future;
    }

    @Internal
    private final class FormExecutor {
        protected final CompletableFuture<UniObjectNode> future;
        protected final DiscordTextChannel<TXT> inChannel;
        protected final DiscordUser<USR> targetUser;

        @Internal
        private FormExecutor(TXT inChannel, USR targetUser) {
            this.inChannel = dux.convertTXT(inChannel);
            this.targetUser = dux.convertUSR(targetUser);
            this.future = CompletableFuture.supplyAsync(this::execute);
        }

        @Blocking
        @Internal
        private UniObjectNode execute() {
            return executeAndStoreRecursive(0, dux.getAdapter()
                    .getSerializationAdapter()
                    .createUniObjectNode());
        }

        @Blocking
        @Internal
        private UniObjectNode executeAndStoreRecursive(final Object thisKey, final UniObjectNode data) {
            return findStage(thisKey).ifPresentMapOrElseGet(
                    stage -> stage.displayable.displayIn(inChannel)
                            .map(dux::convertMSG)
                            .ifPresentMapOrElseGet(
                                    msg -> stage.inputSequence.listen(targetUser, msg),
                                    () -> Polyfill.failedFuture(new RuntimeException("Could not show displayable")))
                            .thenApplyAsync(result -> {
                                if (result instanceof Iterable) {
                                    final UniArrayNode arr = data.putArray(stage.key);
                                    ((Iterable<?>) result).forEach(it -> arr
                                            .add(stage.inputSequence.getResultType(), uncheckedCast(it)));
                                } else if (result instanceof Map) {
                                    final UniObjectNode obj = data.putObject(stage.key);
                                    ((Map<?, ?>) result).forEach((k, v) -> obj
                                            .put(String.valueOf(k), stage.inputSequence.getResultType(), uncheckedCast(v)));
                                } else
                                    data.put(stage.key, stage.inputSequence.getResultType(), uncheckedCast(result));

                                String nextKey = stage.nextKeyResolver.apply(uncheckedCast(result));

                                if (nextKey != null)
                                    return executeAndStoreRecursive(nextKey, data);
                                else if (thisKey instanceof Integer)
                                    return executeAndStoreRecursive(((int) thisKey) + 1, data);
                                else return data;
                            }).join(),
                    () -> data);
        }

        private Processor<? extends FormStage<?, TXT, USR, MSG>> findStage(Object key) {
            if (key instanceof Integer) {
                if (((int) key) >= stages.size())
                    return Processor.empty();

                return stages.getReference((int) key).process();
            } else {
                String strKey = String.valueOf(key);

                return stages.stream()
                        .filter(stage -> stage.key.equals(strKey))
                        .findFirst()
                        .map(Processor::ofConstant)
                        .orElseGet(Processor::empty);
            }
        }
    }
}

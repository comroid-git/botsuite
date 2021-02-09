package org.comroid.dux.form;

import org.comroid.annotations.Blocking;
import org.comroid.api.Polyfill;
import org.comroid.dux.adapter.DiscordTextChannel;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.ui.io.CombinedAction;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniObjectNode;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static org.comroid.api.Polyfill.uncheckedCast;

@Internal
public final class FormExecutor<TXT, USR, MSG> extends CompletableFuture<UniObjectNode> {
    protected final DiscordTextChannel<TXT> inChannel;
    protected final DiscordUser<USR> targetUser;
    private final DiscordForm<?, TXT, USR, MSG> form;

    @Internal
    FormExecutor(DiscordForm<?, TXT, USR, MSG> form, TXT inChannel, USR targetUser) {
        this(ForkJoinPool.commonPool(), form, inChannel, targetUser);
    }

    @Internal
    FormExecutor(Executor executor, DiscordForm<?, TXT, USR, MSG> form, TXT inChannel, USR targetUser) {
        this.form = form;
        this.inChannel = form.dux.convertTXT(inChannel);
        this.targetUser = form.dux.convertUSR(targetUser);

        executor.execute(() -> complete(execute()));
    }

    @Blocking
    @Internal
    private UniObjectNode execute() {
        return executeAndStoreRecursive(0, form.dux.getAdapter()
                .getSerializationAdapter()
                .createUniObjectNode());
    }

    @Blocking
    @Internal
    private UniObjectNode executeAndStoreRecursive(final Object thisKey, final UniObjectNode data) {
        return findStage(thisKey).ifPresentMapOrElseGet(
                stage -> stage.displayIn(inChannel)
                        .map(form.dux::convertMSG)
                        .ifPresentMapOrElseGet(
                                msg -> stage.listen(targetUser, msg),
                                () -> Polyfill.failedFuture(new RuntimeException("Could not show displayable")))
                        .thenApplyAsync(result -> {
                            if (result instanceof Iterable) {
                                final UniArrayNode arr = data.putArray(stage.getName());
                                ((Iterable<?>) result).forEach(it -> arr
                                        .add(stage.getResultType(), uncheckedCast(it)));
                            } else if (result instanceof Map) {
                                final UniObjectNode obj = data.putObject(stage.getName());
                                ((Map<?, ?>) result).forEach((k, v) -> obj
                                        .put(String.valueOf(k), stage.getResultType(), uncheckedCast(v)));
                            } else data.put(stage.getName(), stage.getResultType(), uncheckedCast(result));

                            String nextKey = stage.findFollowupKey(uncheckedCast(result));

                            if (nextKey != null)
                                return executeAndStoreRecursive(nextKey, data);
                            else if (thisKey instanceof Integer)
                                return executeAndStoreRecursive(((int) thisKey) + 1, data);
                            else return data;
                        }).join(),
                () -> data);
    }

    private Processor<? extends CombinedAction<?, TXT, USR, MSG>> findStage(Object key) {
        if (key instanceof Integer) {
            if (((int) key) >= form.stageCount())
                return Processor.empty();

            return form.stages.getReference((int) key).process();
        } else {
            String strKey = String.valueOf(key);

            return form.stages.stream()
                    .filter(stage -> stage.getName().equals(strKey))
                    .findFirst()
                    .map(Processor::ofConstant)
                    .orElseGet(Processor::empty);
        }
    }
}

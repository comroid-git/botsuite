package org.comroid.dux.ui;

import org.comroid.api.UUIDContainer;
import org.comroid.common.Disposable;
import org.comroid.dux.model.TargetUserBound;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractAction<R> extends UUIDContainer.Base implements Disposable, TargetUserBound {
    public final CompletableFuture<R> future = new CompletableFuture<>();
    private final long targetUserId;

    @Override
    public long getTargetUserID() {
        return targetUserId;
    }

    protected AbstractAction(long targetUserId) {
        this.targetUserId = targetUserId;
    }

    @Override
    public void closeSelf() {
        if (!future.isDone())
            future.completeExceptionally(new RuntimeException("Input Aborted"));
    }
}

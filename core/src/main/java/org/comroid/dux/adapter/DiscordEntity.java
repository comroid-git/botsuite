package org.comroid.dux.adapter;

import org.comroid.mutatio.ref.Reference;

public interface DiscordEntity<S> {
    long getID();

    Reference<S> getParentReference();
}

package org.comroid.dux.ui.output;

import org.comroid.dux.adapter.DiscordTextChannel;
import org.comroid.mutatio.ref.FutureReference;

public interface Displayable<TXT, MSG> {
    FutureReference<MSG> displayIn(DiscordTextChannel<TXT> channel);
}

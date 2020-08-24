package org.comroid.dux.ui.io;

import org.comroid.common.ref.Named;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.jetbrains.annotations.Nullable;

public abstract class CombinedAction<R, TXT, USR, MSG> extends DiscordDisplayable<TXT, MSG>
        implements InputSequence<R, USR, MSG>, Named {
    public abstract @Nullable String findFollowupKey(R forReponse);
}

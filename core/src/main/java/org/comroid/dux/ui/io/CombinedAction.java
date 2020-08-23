package org.comroid.dux.ui.io;

import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;

public abstract class CombinedAction<R, TXT, USR, MSG> extends DiscordDisplayable<TXT, MSG> implements InputSequence<R, USR, MSG> {
}

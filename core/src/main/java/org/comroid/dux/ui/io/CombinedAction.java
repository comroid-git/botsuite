package org.comroid.dux.ui.io;

import org.comroid.dux.abstr.LibraryAdapter;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.uniform.HeldType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class CombinedAction<R, TXT, USR, MSG> extends DiscordDisplayable<TXT, MSG> implements InputSequence<R, MSG> {
}

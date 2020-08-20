package org.comroid.dux.builder;

import org.comroid.dux.abstr.LibraryAdapter;
import org.comroid.dux.model.DiscordTextChannel;
import org.comroid.uniform.HeldType;

public class FormBuilder<SRV, TXT, USR, MSG, R> {
    private final LibraryAdapter<Object, SRV, TXT, USR, MSG> adapter;
    private final DiscordTextChannel<TXT> inChannel;
    private final HeldType<R> resultType;

    public FormBuilder(LibraryAdapter<Object, SRV, TXT, USR, MSG> adapter,
                       DiscordTextChannel<TXT> inChannel,
                       HeldType<R> resultType) {
        this.adapter = adapter;
        this.inChannel = inChannel;
        this.resultType = resultType;
    }
}

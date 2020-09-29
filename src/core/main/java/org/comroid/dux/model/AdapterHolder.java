package org.comroid.dux.model;

import org.comroid.dux.adapter.LibraryAdapter;

public interface AdapterHolder<SRV, TXT, USR, MSG> {
    LibraryAdapter<Object, SRV, TXT, USR, MSG> getAdapter();
}

package org.comroid.dux.ui.output;

import org.comroid.mutatio.ref.FutureReference;

public interface Displayable<TXT, MSG> {
    FutureReference<MSG> displayIn(TXT channel);
}

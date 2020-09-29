package org.comroid.dux.model;

public interface TargetUserBound {
    long getTargetUserID();

    default boolean isUserTargeted(long id) {
        final long target = getTargetUserID();
        return target == -1 || target == id;
    }
}

package net.grandcentrix.thirtyinch.distinctuntilchanged;

import java.util.Arrays;

/**
 * {@link DistinctComparator} implementation which uses the {@link Object#hashCode()} of the
 * parameters to detect changes. This is the default implementation used by the
 * {@link DistinctUntilChanged} annotation.
 */
public class HashComparator implements DistinctComparator {

    private int mLastParametersHash = 0;

    @Override
    public boolean isEqual(final Object[] newParameters) {
        final int hash = Arrays.hashCode(newParameters);
        if (hash == mLastParametersHash) {
            return true;
        }
        mLastParametersHash = hash;
        return false;
    }
}

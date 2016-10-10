package net.grandcentrix.thirtyinch.distinctuntilchanged;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * {@link DistinctComparator} implementation which uses the {@link Object#equals(Object)} Method
 * of the parameters to detect changes.
 */
public class EqualsComparator implements DistinctComparator {

    private WeakReference<Object[]> mLastParameters;

    @Override
    public boolean isEqual(final Object[] newParameters) {
        if (mLastParameters == null || !Arrays.equals(newParameters, mLastParameters.get())) {
            mLastParameters = new WeakReference<>(newParameters);
            return false;
        }
        return true;
    }
}

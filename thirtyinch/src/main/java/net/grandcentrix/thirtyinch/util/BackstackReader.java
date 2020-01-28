package net.grandcentrix.thirtyinch.util;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

/**
 * Reads package private information about the {@link FragmentManager} backstack
 *
 * Thanks Hannes ;)
 */
public class BackstackReader {

    /**
     * Checks whether or not a given fragment is on the backstack of the fragment manager (could
     * also be on top of the backstack and hence visible)
     *
     * @param fragment The fragment you want to check if its on the back stack
     * @return true, if the given Fragment is on the back stack, otherwise false (not on the back
     * stack)
     *
     * Hacky workaround because Fragment#isInBackStack is inaccessible with AndroidX
     */
    public static boolean isInBackStack(final Fragment fragment) {
        return isInBackStackAndroidX120(fragment);
    }

    /**
     * Implementation which worked with AndroidX Fragment 1.1.0 and should be working again from 1.2.1:
     * https://issuetracker.google.com/issues/148189412
     */
    private static boolean isInBackStackAndroidXOld(final Fragment fragment) {
        final StringWriter writer = new StringWriter();
        fragment.dump("", null, new PrintWriter(writer), null);
        final String dump = writer.toString();
        return !dump.contains("mBackStackNesting=0");
    }

    /**
     * Temporary hack for the hack ;) because in AndroidX Fragment 1.2.0 the original `fragment.dump` hack stopped working:
     * https://github.com/sockeqwe/mosby/issues/318#issuecomment-577660091
     *
     * Uses reflection, so should be removed once AndroidX Fragment 1.2.1 is released.
     */
    private static boolean isInBackStackAndroidX120(final Fragment fragment) {
        try {
            final Field backStackNestingField = Fragment.class.getDeclaredField("mBackStackNesting");
            backStackNestingField.setAccessible(true);
            final int backStackNesting = backStackNestingField.getInt(fragment);
            return backStackNesting > 0;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

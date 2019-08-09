package net.grandcentrix.thirtyinch.util;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.io.PrintWriter;
import java.io.StringWriter;

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
        final StringWriter writer = new StringWriter();
        fragment.dump("", null, new PrintWriter(writer), null);
        final String dump = writer.toString();
        return !dump.contains("mBackStackNesting=0");
    }
}

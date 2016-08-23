package net.grandcentrix.thirtyinch.util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class AndroidDeveloperOptions {

    /**
     * Returns the state of the "Don't keep activities - Destroy every activity as soon as the user
     * leaves it" developer option
     */
    public static boolean isDontKeepActivitiesEnabled(final Context context) {
        int alwaysFinishActivitiesInt;
        if (Build.VERSION.SDK_INT >= 17) {
            alwaysFinishActivitiesInt = Settings.System
                    .getInt(context.getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES,
                            0);
        } else {
            alwaysFinishActivitiesInt = Settings.System
                    .getInt(context.getContentResolver(), Settings.System.ALWAYS_FINISH_ACTIVITIES,
                            0);
        }

        return alwaysFinishActivitiesInt == 1;
    }

}

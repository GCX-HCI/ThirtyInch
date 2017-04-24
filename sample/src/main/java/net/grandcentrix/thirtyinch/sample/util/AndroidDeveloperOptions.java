/*
 * Copyright (C) 2017 grandcentrix GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.grandcentrix.thirtyinch.sample.util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class AndroidDeveloperOptions {

    /**
     * Returns the state of the "Don't keep activities - Destroy every activity as soon as the user
     * leaves it" developer option
     */
    @SuppressWarnings("deprecation")
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

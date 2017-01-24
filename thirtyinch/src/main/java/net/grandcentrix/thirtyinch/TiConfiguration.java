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

package net.grandcentrix.thirtyinch;

import net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThread;
import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;
import net.grandcentrix.thirtyinch.internal.TiPresenterProvider;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Configure how ThirtyInch should handle the {@link TiPresenter}.
 * Can be applied to the constructor of a presenter {@link TiPresenter#TiPresenter(TiConfiguration)}
 * or global (in {@link Application#onCreate()}) with {@link TiPresenter#setDefaultConfig(TiConfiguration)}.
 * <p>
 * Use the {@link Builder} to change the default configuration.
 * <code>
 * <pre>
 * public HelloWorldPresenter() {
 *     super(new TiConfiguration.Builder()
 *     .setCallOnMainThreadInterceptorEnabled(true)
 *     .setDistinctUntilChangedInterceptorEnabled(true)
 *     .setUseStaticSaviorToRetain(true)
 *     .setRetainPresenterEnabled(true)
 *     .build());
 * }
 * </pre>
 * </code>
 */
public class TiConfiguration {

    public static class Builder {

        private final TiConfiguration mConfig;

        /**
         * Initializes a builder for {@link TiConfiguration}
         */
        public Builder() {
            mConfig = new TiConfiguration();
        }

        /**
         * Constructs the {@link TiConfiguration}
         *
         * @return a {@link TiConfiguration}
         */
        public TiConfiguration build() {
            return mConfig;
        }

        /**
         * When enabled you can add the {@link CallOnMainThread} annotation to <code>void</code>
         * methods of your {@link TiView} interface.
         * The {@link net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThreadInvocationHandler}
         * then automatically calls the method on the android main thread.
         * This allows to run code off the main thread but send events to the UI without dealing
         * with {@link android.os.Handler} and {@link android.os.Looper}.
         * <p>
         * You'll never see "CalledFromWrongThreadException: Only the original thread that created
         * a view hierarchy can touch its views." again.
         * <p>
         * Disable this option and the {@link CallOnMainThread} annotation will be ignored
         * <p>
         * default <code>true</code>
         * <p>
         * Example {@link TiView} interface
         * <code>
         * <pre>
         * public interface HelloWorldView extends TiView {
         *
         *     &#64;CallOnMainThread
         *     void showPresenterUpTime(Long uptime);
         *
         *     &#64;CallOnMainThread
         *     void showText(final String text);
         *
         *     // not allowed here because it's not a void method
         *     int doSomethingAndReturnResult();
         * }
         * </pre>
         * </code>
         */
        public Builder setCallOnMainThreadInterceptorEnabled(final boolean enabled) {
            mConfig.mCallOnMainThreadInterceptorEnabled = enabled;
            return this;
        }

        /**
         * When enabled you can add the {@link DistinctUntilChanged} annotation to
         * <code>void</code> methods of your {@link TiView} with at least one argument.
         * When calling such an annotated method the {@link TiView} receives no duplicated (equal)
         * calls.
         * {@link #hashCode()} is used to check for changes.
         * <p>
         * Calling {@link TiView} methods often with the same arguments can cause performance
         * problems, especially when those calls cause unnecessary layout changes.
         * This often happened when an Activity goes to background and to foreground again.
         * The same Activity will be attached to the {@link TiPresenter} and methods to fill the UI
         * will be called.
         * Because the same {@link TiView} (Activity) was already attached it most likely already
         * knows the correct data to show.
         * <p>
         * Disable this option and the {@link DistinctUntilChanged} annotation will be ignored
         * <p>
         * default <code>true</code>
         * <p>
         * <code>
         * <pre>
         * public interface UserListView extends TiView {
         *
         *     // the RecyclerView should not render the same data twice
         *     &#64;DistinctUntilChanged
         *     void showUsers(List<User> users);
         *
         *     // can be called twice with the same text simply showing two Toasts
         *     // don't add &#64;DistinctUntilChanged here
         *     void showToast(final String text);
         * }
         * </pre>
         * </code>
         * <p>
         * Inspired by the <code>.distinctUntilChanged()</code> operator from RxJava
         */
        public Builder setDistinctUntilChangedInterceptorEnabled(final boolean enabled) {
            mConfig.mDistinctUntilChangedInterceptorEnabled = enabled;
            return this;
        }

        //TODO documentation
        public Builder setPresenterSerializer(TiPresenterSerializer serializer) {
            mConfig.mPresenterSerializer = serializer;
            return this;
        }

        /**
         * When set to <code>true</code> the {@link TiPresenter} will be restored when the {@link
         * Activity} recreates due to a configuration changes such as the orientation change.
         * The same {@link TiPresenter} instance will be attached to the new {@link Activity}
         * instance.
         * {@link TiPresenterProvider#providePresenter()} will be called only once within the
         * {@link Activity} lifecycle when {@link Activity#onCreate(Bundle)} gets called with
         * {@code savedInstanceState == null}.
         * <p>
         * When set to <code>false</code> a new {@link TiPresenter} instance will be created every
         * time {@link Activity#onCreate(Bundle)} gets called.
         * {@link TiPresenterProvider#providePresenter()} will be called for the new instance.
         * <p>
         * It's recommended to retain the {@link TiPresenter} and keep your state in it.
         * You basically never have to implement {@link Activity#onSaveInstanceState(Bundle)} again
         * because your state lives in memory in your presenter.
         * But Google and many others are using presenters which do not survive configuration
         * changes.
         * If this is your preferred way of doing MVP, here is your option to disable the
         * restoration.
         * <p>
         * default <code>true</code>
         */
        public Builder setRetainPresenterEnabled(final boolean enabled) {
            mConfig.mRetainPresenter = enabled;
            return this;
        }

        /**
         * Sets whether the {@link PresenterSavior} singleton should be used to restore the {@link
         * TiPresenter}. This was a workaround targeting the "Don't keep
         * activities" option in the Android Developer options destroying every activity as soon as
         * the user leaves it.
         * The "good" android way of saving the {@link TiPresenter} with {@link
         * AppCompatActivity#onRetainCustomNonConfigurationInstance()} does not work when the
         * option is enabled.
         * The {@link PresenterSavior} works even if the option is enabled.
         * <p>
         * Some people argue that singletons are bad and want to be a good android citizen. This
         * method is for you.
         * Set it to <code>false</code> and the singleton will not be used. You
         * are responsible when you lose data because a new {@link TiPresenter} instance will be
         * created.
         * <p>
         * This option will not be used when {@link #setRetainPresenterEnabled(boolean)} is set to
         * <code>false</code>.
         * <p>
         * default <code>true</code>
         */
        public Builder setUseStaticSaviorToRetain(final boolean enabled) {
            mConfig.mUseStaticSaviorToRetain = enabled;
            return this;
        }
    }

    public static final TiConfiguration DEFAULT = new Builder().build();

    private boolean mCallOnMainThreadInterceptorEnabled = true;

    private boolean mDistinctUntilChangedInterceptorEnabled = true;

    private TiPresenterSerializer mPresenterSerializer;

    private boolean mRetainPresenter = true;

    private boolean mUseStaticSaviorToRetain = true;

    /**
     * use {@link Builder} to construct a configuration.
     */
    private TiConfiguration() {
    }

    @Nullable
    public TiPresenterSerializer getPresenterSerializer() {
        return mPresenterSerializer;
    }

    public boolean isCallOnMainThreadInterceptorEnabled() {
        return mCallOnMainThreadInterceptorEnabled;
    }

    public boolean isDistinctUntilChangedInterceptorEnabled() {
        return mDistinctUntilChangedInterceptorEnabled;
    }

    public boolean shouldRetainPresenter() {
        return mRetainPresenter;
    }

    public boolean useStaticSaviorToRetain() {
        return mUseStaticSaviorToRetain;
    }
}

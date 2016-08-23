package net.grandcentrix.thirtyinch;

//TODO add documentation. this will be one of the most read documentations people will read
public class TiPresenterConfiguration {

    public static class Builder {

        private final TiPresenterConfiguration mConfig;

        public Builder() {
            mConfig = new TiPresenterConfiguration();
        }

        public TiPresenterConfiguration build() {
            return mConfig;
        }

        public Builder setCallOnMainThreadInterceptorEnabled(final boolean enabled) {
            mConfig.mCallOnMainThreadInterceptorEnabled = enabled;
            return this;
        }

        public Builder setDistinctUntilChangedInterceptorEnabled(final boolean enabled) {
            mConfig.mDistinctUntilChangedInterceptorEnabled = enabled;
            return this;
        }

        public Builder setRetainPresenterEnabled(final boolean enabled) {
            mConfig.mRetainPresenter = enabled;
            return this;
        }

        public Builder setUseStaticSaviorToRetain(final boolean enabled) {
            mConfig.mUseStaticSaviorToRetain = enabled;
            return this;
        }

    }

    public static final TiPresenterConfiguration DEFAULT = new Builder().build();

    private boolean mCallOnMainThreadInterceptorEnabled = true;

    private boolean mDistinctUntilChangedInterceptorEnabled = true;

    private boolean mRetainPresenter = true;

    private boolean mUseStaticSaviorToRetain = true;

    private TiPresenterConfiguration() {
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

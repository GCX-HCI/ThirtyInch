package net.grandcentrix.thirtyinch.android.internal;

import android.os.Bundle;
import android.support.annotation.NonNull;

public interface FragmentPresenterProvider<P> {

    @NonNull
    P providePresenter(final Bundle activityIntentBundle, final Bundle fragmentArguments);
}

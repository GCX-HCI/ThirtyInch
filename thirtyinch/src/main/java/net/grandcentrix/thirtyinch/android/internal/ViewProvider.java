package net.grandcentrix.thirtyinch.android.internal;

import android.support.annotation.NonNull;

public interface ViewProvider<V> {

    @NonNull
    V provideView();
}

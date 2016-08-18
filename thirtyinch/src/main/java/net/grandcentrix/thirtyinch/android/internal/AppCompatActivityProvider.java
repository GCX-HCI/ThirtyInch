package net.grandcentrix.thirtyinch.android.internal;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

public interface AppCompatActivityProvider {

    @NonNull
    AppCompatActivity getAppCompatActivity();
}

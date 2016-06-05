package net.grandcentix.thirtyinch.sample;

import net.grandcentrix.thirtyinch.DistinctUntilChanged;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.android.CallOnMainThread;

import rx.Observable;

public interface HelloWorldView extends TiView {

    Observable<Void> onButtonClicked();

    @CallOnMainThread
    void showPresenterUpTime(Long uptime);

    @CallOnMainThread
    @DistinctUntilChanged
    void showText(final String text);
}

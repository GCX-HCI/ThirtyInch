package net.grandcentix.thirtyinch.sample;

import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.android.callonmainthread.CallOnMainThread;

public interface SampleView extends TiView {

    @CallOnMainThread
    void showText(final String s);

}

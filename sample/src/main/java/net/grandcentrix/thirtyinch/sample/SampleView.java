package net.grandcentrix.thirtyinch.sample;

import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThread;


public interface SampleView extends TiView {

    @CallOnMainThread
    void showText(final String s);

}

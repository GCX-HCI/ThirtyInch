package net.grandcentrix.thirtyinch.sample;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiPresenterConfiguration;
import net.grandcentrix.thirtyinch.rx.RxTiPresenterSubscriptionHandler;
import net.grandcentrix.thirtyinch.rx.RxTiPresenterUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;


public class SamplePresenter extends TiPresenter<SampleView> {


    private RxTiPresenterSubscriptionHandler mSubscriptionHandler
            = new RxTiPresenterSubscriptionHandler(this);

    public SamplePresenter() {
        super(new TiPresenterConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .setUseStaticSaviorToRetain(true)
                .build());
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        mSubscriptionHandler.manageSubscription(Observable.interval(0, 37, TimeUnit.MILLISECONDS)
                .compose(RxTiPresenterUtils.<Long>deliverLatestToView(this))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(final Long alive) {
                        getView().showText("I'm a fragment and alive for " + (alive * 37) + "ms");
                    }
                }));
    }
}

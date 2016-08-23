package net.grandcentrix.thirtyinch.sample;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.rx.RxTiPresenterSubscriptionHandler;
import net.grandcentrix.thirtyinch.rx.RxTiPresenterUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class HelloWorldPresenter extends TiPresenter<HelloWorldView> {

    private static final String TAG = HelloWorldPresenter.class.getSimpleName();

    private int mCounter = 0;

    private BehaviorSubject<String> mText = BehaviorSubject.create();

    private PublishSubject<Void> triggerHeavyCalculation = PublishSubject.create();

    private RxTiPresenterSubscriptionHandler rxSubscriptionHelper = new RxTiPresenterSubscriptionHandler(this);


    public HelloWorldPresenter() {
        super(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(true)
                .build());
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        mText.onNext("Hello World!");

        rxSubscriptionHelper.manageSubscription(Observable.interval(0, 1, TimeUnit.SECONDS)
                .compose(RxTiPresenterUtils.<Long>deliverLatestToView(this))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(final Long uptime) {
                        getView().showPresenterUpTime(uptime);
                    }
                }));

        rxSubscriptionHelper.manageSubscription(triggerHeavyCalculation
                .doOnNext(new Action1<Void>() {
                    @Override
                    public void call(final Void aVoid) {
                        mText.onNext("calculating next number...");
                    }
                })
                .onBackpressureDrop(new Action1<Void>() {
                    @Override
                    public void call(final Void aVoid) {
                        mText.onNext("Don't hurry me!");
                    }
                })
                .flatMap(new Func1<Void, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(final Void aVoid) {
                        return increaseCounter();
                    }
                }, 1)
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(final Integer integer) {
                        mText.onNext("Count: " + mCounter);
                    }
                })
                .subscribe());
    }

    @Override
    protected void onWakeUp() {
        super.onWakeUp();

        rxSubscriptionHelper.manageViewSubscription(mText.asObservable()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(final String text) {
                        getView().showText(text);
                    }
                }));

        rxSubscriptionHelper.manageViewSubscription(getView().onButtonClicked()
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(final Void aVoid) {
                        triggerHeavyCalculation.onNext(null);
                    }
                }));
    }

    /**
     * fake a heavy calculation
     */
    private Observable<Integer> increaseCounter() {
        return Observable.just(mCounter)
                .subscribeOn(Schedulers.computation())
                // fake heavy calculation
                .delay(2, TimeUnit.SECONDS)
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(final Integer integer) {
                        mCounter++;
                        mText.onNext("value: " + mCounter);
                    }
                });
    }
}

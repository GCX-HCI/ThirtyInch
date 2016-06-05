package net.grandcentix.thirtyinch.sample;

import net.grandcentrix.thirtyinch.TiPresenter;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class HelloWorldPresenter extends TiPresenter<HelloWorldView> {

    private static final String TAG = HelloWorldPresenter.class.getSimpleName();

    private int mCounter = 0;

    private PublishSubject<String> mText = PublishSubject.create();

    @Override
    protected void onCreate() {
        super.onCreate();

        mText.onNext("Hello World!");

        manageSubscription(Observable.interval(1, TimeUnit.SECONDS)
                .compose(this.<Long>deliverLatestToView())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(final Long uptime) {
                        getView().showPresenterUpTime(uptime);
                    }
                }));
    }

    @Override
    protected void onWakeUp() {
        super.onWakeUp();

        manageViewSubscription(mText.asObservable()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(final String text) {
                        getView().showText(text);
                    }
                }));

        manageViewSubscription(getView().onButtonClicked()
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
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(final Integer integer) {
                        mText.onNext("value: " + mCounter);
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
                    }
                });
    }
}

# DEPRECATED - no longer actively maintained

[![Build Status](https://travis-ci.org/grandcentrix/ThirtyInch.svg?branch=master)](https://travis-ci.org/grandcentrix/ThirtyInch)
![License](https://img.shields.io/badge/license-Apache%202-green.svg?style=flat) 
[![Gitter](https://badges.gitter.im/ThirtyInch/gitter.svg)](https://gitter.im/ThirtyInch/Lobby)
# ThirtyInch - a MVP library for Android


This library adds Presenters to Activities and Fragments. It favors the stateful Presenter pattern, where the Presenter survives configuration changes and dumb View pattern, where the View only sends user events and receives information from the Presenter but never actively asks for data. This makes testing very easy because no logic lives in the View (Activity, Fragment) except for fancy animations which anyways aren't testable.

#### The name

> Keep Android At Arm’s Length
>
&mdash; Kevin Schultz, Droidcon NYC '14

The perfect distance to the Android Framework is approximately **thirty inches**, the average length of the human arm, shoulder to fingertips.

## Story

Read the introduction article on [Medium](https://medium.com/@passsy/thirtyinch-a-new-mvp-library-for-android-bd1a27262fd6#.xihev9jxq)

See the slides of the latest talk on [Speakerdeck](https://speakerdeck.com/passsy/thirtyinch-living-next-to-the-activity)


## Get it

### GitHub Packages

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/GCX-HCI/ThirtyInch")
    }
}

dependencies {
    implementation "net.grandcentrix.thirtyinch:thirtyinch:$thirtyinchVersion"
    implementation "net.grandcentrix.thirtyinch:thirtyinch-rx2:$thirtyinchVersion"
    implementation "net.grandcentrix.thirtyinch:thirtyinch-logginginterceptor:$thirtyinchVersion"
    implementation "net.grandcentrix.thirtyinch:thirtyinch-kotlin:$thirtyinchVersion"
    implementation "net.grandcentrix.thirtyinch:thirtyinch-kotlin-coroutines:$thirtyinchVersion"
    
    // Legacy dependencies
    implementation "net.grandcentrix.thirtyinch:thirtyinch-rx:$thirtyinchVersion"
}
```

### JCenter (deprecated)

```gradle
repositories {
    jcenter()
}

dependencies {
    implementation "net.grandcentrix.thirtyinch:thirtyinch:$thirtyinchVersion"
    implementation "net.grandcentrix.thirtyinch:thirtyinch-rx2:$thirtyinchVersion"
    implementation "net.grandcentrix.thirtyinch:thirtyinch-logginginterceptor:$thirtyinchVersion"
    implementation "net.grandcentrix.thirtyinch:thirtyinch-kotlin:$thirtyinchVersion"
    implementation "net.grandcentrix.thirtyinch:thirtyinch-kotlin-coroutines:$thirtyinchVersion"
    
    // Lagacy dependencies
    implementation "net.grandcentrix.thirtyinch:thirtyinch-rx:$thirtyinchVersion"
}
```

## Hello World MVP example with ThirtyInch

`HelloWorldActivity.java`
```java
public class HelloWorldActivity 
        extends TiActivity<HelloWorldPresenter, HelloWorldView> 
        implements HelloWorldView {

    private TextView mOutput;

    @NonNull
    @Override
    public HelloWorldPresenter providePresenter() {
        return new HelloWorldPresenter();
    }

    @Override
    public void showText(final String text) {
        mOutput.setText(text);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        mOutput = (TextView) findViewById(R.id.output);
    }
}
```

`HelloWorldView.java`
```java
public interface HelloWorldView extends TiView {

    @CallOnMainThread
    void showText(final String text);
}

```

`HelloWorldPresenter.java`
```java
public class HelloWorldPresenter extends TiPresenter<HelloWorldView> {

    @Override    
    protected void onAttachView(@NonNull final HelloWorldView view) {
        super.onAttachView(view);
        view.showText("Hello World!");
    }
}

```
## ThirtyInch features

### Presenter

- survives configuration changes
- survives when the `Activity` got killed in background
- is not a singleton
- dies when the `Activity` gets finished

##### Lifecycle

The `TiPresenter` lifecycle is very easy.

It can be `CREATED` and `DESTROYED`. 
The corresponding callbacks `onCreate()` and `onDestroy()` will be only called once!

The `TiView` can either be `ATTACHED` or `DETACHED`.
The corresponding callbacks are `onAttachView(TiView)` and `onDetachView()` which maps to `onStart()` and `onStop()`.


```java
public class MyPresenter extends TiPresenter<MyView> {

    @Override
    protected void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onAttachView(@NonNull final HelloWorldView view) {
        super.onAttachView(view);
    }

    @Override
    protected void onDetachView() {
        super.onDetachView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

```

The lifecycle can be observed using `TiLifecycleObserver`

There is no callback for `onResume()` and `onPause()` in the `TiPresenter`. 
This is something the view layer should handle.
Read more about this here [Hannes Dorfmann - Presenters don't need lifecycle events](http://hannesdorfmann.com/android/presenters-dont-need-lifecycle)

##### Configuration

The default behaviour might not fit your needs. 
You can disable unwanted features by providing a configuration in the `TiPresenter` constructor.

```java
public class HelloWorldPresenter extends TiPresenter<HelloWorldView> {

    public static final TiConfiguration PRESENTER_CONFIG = 
            new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true) 
                .setCallOnMainThreadInterceptorEnabled(true)
                .setDistinctUntilChangedInterceptorEnabled(true)
                .build();
            
    public HelloWorldPresenter() {
        super(PRESENTER_CONFIG);
    }
}
```

Or globally for all `TiPresenters`
```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TiPresenter.setDefaultConfig(MY_DEFAULT_CONFIG);
    }
}
```

### TiView Annotations

Two awesome annotations for the `TiView` interface made it already into `Ti` saving you a lot of time.

```java
public interface HelloWorldView extends TiView {

    @CallOnMainThread
    @DistinctUntilChanged
    void showText(final String text);
}
```

##### @CallOnMainThread

Whenever you call this method it will be called on the Android main thread.
This allows to run code off the main thread but send events to the UI without dealing with Handlers and Loopers.

Requires to be a `void` method. Works only for `TiView` interfaces implemented by "Android Views" (`TiActivity`, `TiFragment`).

Enabled by default, can be disabled with the `TiConfiguration`

##### @DistinctUntilChanged

When calling this method the `View` receives no duplicated calls.
The View swallows the second call when a method gets called with the same (hashcode) parameters twice.

Usecase:
The Presenter binds a huge list to the `View`. The app loses focus (`onDetachView()`) and the exact same Activity instance gains focus again (`onAttachView(view)`).
The `Activity` still shows the huge list.
The `Presenter` binds the huge list again to the `View`.
When the data has changed the list will be updated.
When the data hasn't changed the call gets swallowed and prevents flickering.

Requires to be a `void` method and has at least one parameter.

Enabled by default, can be disabled with the `TiConfiguration`


### View binding interceptors

*View Annotations* only work because ThirtyInch supports interceptors. 
Add interceptors (`BindViewInterceptor`) to `TiActivity` or `TiFragment` to intercept the binding process from `TiView` to `TiPresenter`.
Interceptors are public API waiting for other great ideas.


```java
public class HelloWorldActivity extends TiActivity<HelloWorldPresenter, HelloWorldView>
        implements HelloWorldView {

    public HelloWorldActivity() {
        addBindViewInterceptor(new LoggingInterceptor());
    }
}
```

`LoggingInterceptor` is available as module and logs all calls to the view.

### Kotlin

Using Kotlin these days is a no-brainer.
`ThirtyInch` provides some extension methods to improve itself even further!  

#### SendToView

When using `sendToView`, repeating `it.*` inside the lambda is quite annoying.
It's clear that the methods are called *on* the view.
With the kotlin extension `deliverToView` the `TiView` will be give over to the lambda as `this`.
```kotlin
class HelloWorldPresenter : TiPresenter<HelloWorldView> {

  override fun onCreate() {
      // normal java API
      sendToView {
          it.showText("Hello World")
      }
      
      // kotlin extension
      deliverToView {
          showText("Hello World")
      }
  }
}

interface HelloWorldView : TiView {
    fun showText(text: String)
}
``` 
Back in the Java days we had to use `it` inside the `sendToView`-lambda.

#### Coroutines
If you're using Kotlin's Coroutines we offer a `CoroutineScope` that scopes to a presenter's lifecycle.

```kotlin
class HelloWorldPresenter : TiPresenter<HelloWorldView> {

  private val scope = TiCoroutineScope(this, Dispatchers.Default)

  override fun onCreate() {
      scope.launch { ... }
  }
}
```
The created `Job` will automatically be cancelled when the presenter is destroyed.

Alternatively, you can launch jobs that get cancelled when a `TiView` detaches:
```kotlin
class HelloWorldPresenter : TiPresenter<HelloWorldView> {

  private val scope = TiCoroutineScope(this, Dispatchers.Default)

  override fun onAttachView(view: HelloWorldView) {
      scope.launchUntilViewDetaches { ... }
  }
}
```
However, be careful that `launchUntilViewDetaches` can only be called when there is a view attached!

### [RxJava](https://github.com/ReactiveX/RxJava)

Using RxJava for networking is very often used.
Observing a `Model` is another good usecase where Rx can be used inside of a `TiPresenter`.
The Rx package provides helper classes to deal with `Subscription` or wait for an attached `TiView`.

```java
public class HelloWorldPresenter extends TiPresenter<HelloWorldView> {

    // add the subscription helper to your presenter
    private RxTiPresenterSubscriptionHandler rxHelper = new RxTiPresenterSubscriptionHandler(this);

    @Override
    protected void onCreate() {
        super.onCreate();
        
        // automatically unsubscribe in onDestroy()
        rxHelper.manageSubscription(
                Observable.interval(0, 1, TimeUnit.SECONDS)
                    // cache the latest value when no view is attached
                    // emits when the view got attached
                    .compose(RxTiPresenterUtils.<Long>deliverLatestToView(this))
                    .subscribe(uptime -> getView().showPresenterUpTime(uptime))
        );
    }

    @Override
    protected void onAttachView(@NonNull final HelloWorldView view) {
        super.onAttachView(view);
        
        // automatically unsubscribe in onDetachView(view)
        rxHelper.manageViewSubscription(anotherObservable.subscribe());
    }
}
```

You can make `Disposable` handling even less intrusive in Kotlin. Just create the following interface and make your presenters implement it:

```kotlin
interface DisposableHandler {

    // Initialize with reference to your TiPresenter instance
    val disposableHandler: RxTiPresenterDisposableHandler

    // Dispose of Disposables dependent on the TiPresenter lifecycle
    fun Disposable.disposeWhenDestroyed(): Disposable = disposableHandler.manageDisposable(this)

    // Dispose of Disposables dependent on the TiView attached/detached state
    fun Disposable.disposeWhenViewDetached(): Disposable = disposableHandler.manageViewDisposable(this)
} 
```

Then just implement the interface in your presenter and you can use created extension functions to manage `Disposable`s:

```kotlin
class MyPresenter : TiPresenter<MyView>(), DisposableHandler {

    override val disposableHandler = RxTiPresenterDisposableHandler(this)

    override fun onCreate() {
        super.onCreate()

        // Presenter lifecycle dependent Disposable
        myObservable
            .subscribe()
            .disposeWhenDestroyed()
    }

    override fun onAttachView(view: MyView) {
        super.onAttachView(view)

        // View attached/detached dependent Disposable
        myViewObservable
            .subscribe()
            .disposeWhenViewDetached()
    }
}
```

# License

```
Copyright 2016 grandcentrix GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

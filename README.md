# ThirtyInch - a MVP library for Android

> Keep Android At Arm’s Length
>
&mdash; Kevin Schultz, Droidcon NYC '14

According to dangerous experiments, heavy calculations and long running tests the perfect distance to the Android Framework is approximately *thirty inches*.

## Introduction

Androids biggest hurdle is the *Activity Lifecycle*.
Besides of that and some systems APIs it's plain Java.
Handling the resumed and paused state is easy.
With `onPause` ond `onResume` it is possible to start/stop unnecessary work (such as GPS) when the user doesn't use the app.

But it gets a lot harder when your Activity gets completely destroyed because of an orientation change.
Or the `Activity` was in background and got garbage collected.
The Activity has the same API for both cases.
The state can be saved in `onSaveInstanceState(Bundle)` and recovered when the Activity gets recreated.
But it's only possible to save serialized data. Strings, Integers, Booleans, arrays.
The idea to use the same mechanism to restore the state after a (possible) UI change and returning to an previously opened app was a reasonable decision.
But is it good practice to serialize all data and deserialize everything a few milliseconds later with the new created Activity object?
It would be easier without the serialization.

- What about long running operations like unzipping a file?
Should the zipping stop and restart because the user rotated the phone?
- Should a network request to order a pizza be cancelled because a user taps a notification and leaves the app?

*No*. This is not the expected behaviour.
But it happens all the time on Android devices with gigabytes of ram.
Old laptops with 1GB ram are able to do all those things (and more) in parallel without a problem.
Changing the screen size of a window, unzipping a file while waiting for the response when booking a flight.
Everything simultaneously. Nobody fears that the flight will not be booked because he started chatting after submitting the form.

Android is capable to do those things in parallel as well.
There is the `IntentService`.
Holding a reference to an `AsyncTask` from a singleton could be another option. And many more.

But here is the thing: All those APIs are hard and often limited.
Because of that, the decision to lock the screen rotation and stop *all* work when the app goes to background is often the cheapest and quickest solution.
The bosses are happy if their app doesn't crash.

## Step backwards

What is an `Activity`?
An `Activity` is the Object that gets started by the system when user taps a launcher icon.
The Activity allows us to add Views to a `Window` which will be visible to the user.
There is no other API that can be used to show something to the user.
The Activity also provides a lot of callbacks when and how the UI is visible.
There are other methods to connect to system services but they aren't exclusively available using an Activity Object.

*tl;dr* An `Activity` is the UI of an app and we can't get rid of it.

## Living next to the Activity

The idea behind this project is that the `Activity` is *only the UI* (`View`) and displays data but doesn't store them.
The data/state will be stored in an Object that lives longer than the `Activity`.

This is the `Presenter`.

- The `Presenter` isn't a singleton
- When the `Activity` gets finished the `Presenter` dies, too
- The `Presenter` survives orientation changes
- The `Presenter` survives when the `Activity` got killed in background

The only information the `Presenter` knows about the `View` is if the `View` is in a state to display information or not.
This results in a very easy Lifecycle:

    The `Activity` gets started
    The `Presenter` will be created (`onCreate()`)

        The `Activity` is visible
        The `Activity` will be attached to the `Presenter` (`onWakeUp()`)

        The `Activity` my be invisible
        The `Activity` will be detached form the `Presenter` (`onSleep()`)

        The `Activity` is visible again
        The `Activity` will be attached to the `Presenter` (`onWakeUp()`)

        The `Activity` my be destroyed.
        The `Activity` will be detached form the `Presenter` (`onSleep()`)

        Another `Activity` my be attached to the `Presenter` (`onWakeUp()`)

    The currently attached `Activity` gets finished
    The Presenter gets destroyed (`onSleep()` followed by `onDestroy()`)


*Four* simple lifecycle methods and an `Object` which is able to hold references to long running operations even when the system destroyed the `Activity`.

## The Presenter

The `Presenter` is written in pure Java and so abstract that it has no idea if the `View` is an AndroidView or a TerminalApplicationView.
The communication between `View` and `Presenter` is handled with an interface which should be so generalized that it could be implemented for both.
It's not forbidden but the `Presenter` should *NOT* have a reference to a `Context`. This would break the concept of _"Keep Android At Arm’s Length"_.

## Where is the Model

MVP has a `Model` a `View` and a `Presenter`.
This project doesn't provide a implementation of the `Model`.
The heart of this project is the `Presenter`.
You are free to use whatever you like. A database, a pojo, a service; connecting them to the Presenter is no magic and doesn't require an API.
Using injection is recommended.


## Hello World Example

First create an interface for your `View` extending the empty ThirtyInch `TiView` interface.

```java
public interface HelloWorldView extends TiView {

    void showText(final String text);

}
```

Add a `Presenter` to your `Activity` by extending from `TiActivity<TiPresenter, TiView>`.
Another, advanced option is to use the [plugin](plugin/).
Also implement the `TiView` interface.
The Activity is the view implementation.

Two methods have to be implemented:

`providePresenter()` has to return an instance of the `TiPresenter`.
This method will be called only once in `onCreate(Bundle)`, the first time the Activity gets launched (`savedInstanceState == null`).
Sadly there is no other way to create the `Presenter` than creating it after the `Activity` was launched.
Remember, the `Activity` is the entry point.

The `Activity` (`this`) itself is the `View`.
It will be bound to the presenter whenever the `Activity` is visible to the User (`onStart()`).
In case the `TiActivity` doesn't implement the `TiView` interface check `TiActivity#provideView()`.


```java
public class HelloWorldActivity extends TiActivity<HelloWorldPresenter, HelloWorldView>
        implements HelloWorldView {

    private TextView mOutput;

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

    @NonNull
    @Override
    public HelloWorldPresenter providePresenter(final Bundle activityIntentBundle) {
        return new HelloWorldPresenter();
    }
}
```


Creating the `Presenter` and add your logic

```java

public class HelloWorldPresenter extends TiPresenter<HelloWorldView> {

    private String mText = "Hello World";

    @Override
    protected void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onWakeUp() {
        super.onWakeUp();

        getView().showText(mText);
    }

    @Override
    protected void onSleep() {
        super.onSleep();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

```

Notice: `mText` works here as "Model".

## View interface Annotations

There are two very helpful annotations you can add to *void* methods of your `TiView` interface.

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

##### @DistinctUntilChanged

When calling this method the `View` receives no duplicated (equal) calls.
When the View received a parameter and gets called again with the same parameter (equals) the call gets swallowed.

Usecase:
The Presenter binds a huge list to the `View`. The app loses focus (`onSleep()`) and the exact same Activity instance gains focus again (`onWakeUp()`).
The `Activity` still shows the huge list.
The `Presenter` binds the huge list again to the `View`.
When the data has changed the list will be updated.
When the data hasn't changed the call gets swallowed and prevents flickering.

Requires to be a `void` method and has at least one parameter.


## Build and integrate

This workaround will be removed once public released.

Clone this project and generate the `aar` package. Add the artifact to your local maven repository
```bash
# generate aars and push them to maven local
./gradlew clean bundleRelease bintrayUpload -PbintrayUser=wrongName -PbintrayKey=invalidKey -PdryRun=true`
```

in your app `build.gradle`
```gradle
buildscript {
    repositories {
        mavenLocal()
    }
}

android {
    // your configuration
}

dependencies {
    // "changing true" always loads the latest version from maven (here: mavenLocal)

    // the normal version
    compile('net.grandcentrix.thirtyinch:thirtyinch:0.7-SNAPSHOT') { changing true }

    // the plugin version for CompositeAndroid
    compile('net.grandcentrix.thirtyinch:thirtyinch-plugin:0.7-SNAPSHOT') { changing true }
}

configurations.all {
    // Check for updates every build, default is every 24h
    resolutionStrategy.cacheChangingModulesFor 0, 'seconds'
}
```

Or add the aars manually and put them in the libs folder. Download the [ThrityInch 0.6 aars here](https://github.gcxi.de/grandcentrix/ThirtyInch/releases/tag/v0.6)

For each AAR `File > New > New Module > Import .JAR/.AAR`

```gradlew

dependencies {
    compile project(':thirtyinch')
    compile project(':thirtyinch-plugin')
}
```

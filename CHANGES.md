# ThirtyInch Releases


## Version 0.8.5 `12.10.17`

### Summary

Mostly a maintenance release improving documentation, adding tests and cleaning up the repository.

Highlight: The `test` package is now deprecated and all functionality was moved into the `thirtyinch` artifact.

Thanks for external contributors:
@baltuky @vpondala 

### Public changes

- [Pull 115](https://github.com/grandcentrix/ThirtyInch/pull/115) New: Update dependencies (support lib 26.0.0, target- and compileSdkVersion 26 and other minor updates)
- [Pull 120](https://github.com/grandcentrix/ThirtyInch/pull/120) New: `TiPresenter#test()` returns a `TiTestPresenter` with helper methods for unit testing. Replaces `TiPresenterInstructor` and deprecates the `test` artifact.
- [Pull 126](https://github.com/grandcentrix/ThirtyInch/pull/126) Fix: `TiPresenter#test()` documentation, thx @baltuky

### Internal changes

- [Pull 99](https://github.com/grandcentrix/ThirtyInch/pull/99) Fix: remove javadoc warnings
- [Pull 103](https://github.com/grandcentrix/ThirtyInch/pull/103) New: Improved `RELEASE.md` instructions
- [Pull 105](https://github.com/grandcentrix/ThirtyInch/pull/105) New: Deprecate internal `DelegatedTiActivity#isActivityChangingConfigurations` which turned out as not required to handle config changes correctly. Simplifies internal logic
- [Pull 112](https://github.com/grandcentrix/ThirtyInch/pull/112) New: Update to gradlew 4.1 allowing development with Android Studio 3.0
- [Pull 113](https://github.com/grandcentrix/ThirtyInch/pull/113) New: Test for LifecycleObserver cleanup
- [Pull 114](https://github.com/grandcentrix/ThirtyInch/pull/114) New: Tests for internal `OneTimeRemovable`
- [Pull 117](https://github.com/grandcentrix/ThirtyInch/pull/117) New: internal `PresenterScope` tests
- [Pull 121](https://github.com/grandcentrix/ThirtyInch/pull/121) New: Replace all `Hamcrest` assertions with `AssertJ`, thx @vpondala 
- [Pull 123](https://github.com/grandcentrix/ThirtyInch/pull/123) New: Add codestyle to project and reformat all sources according to this. Reduces diffs for external contributors
- [Pull 127](https://github.com/grandcentrix/ThirtyInch/pull/127) New: Additional tests for 
`TiTestPresenter`

## Version 0.8.0 `04.05.17`

### Summary

Version 0.8.0 is a big step forward towards 1.0.0. The biggest problem, a memory leak when using Fragments could be resolved. This bugfix was only possible after introducing tons of tests.
API enhancements of `TiPresenter` make MVP even easier and removes common pitfalls

Thanks for external contributors:
@remcomokveld, @vRallev, @jonathan-caryl, @k0shk0sh

### thirtyinch

**TiPresenter**

- [Pull 26](https://github.com/grandcentrix/ThirtyInch/pull/26) New: `onAttachView(TiView)` replaces `onWakeUp()` which is now deprecated
- [Pull 26](https://github.com/grandcentrix/ThirtyInch/pull/26) New: `onDetachView()` replaces `onSleep()` which is now deprecated
- [Pull 26](https://github.com/grandcentrix/ThirtyInch/pull/26) New: `getView()` is now annotated with `@Nullable`
- [Pull 87](https://github.com/grandcentrix/ThirtyInch/pull/87) New: `getViewOrThrow()` for cases where the view logically can't be `null`
- [Pull 36](https://github.com/grandcentrix/ThirtyInch/pull/36) New: `sendToView(ViewAction<TiView>)` to postpone code execution until the view is attached
- [Pull 65](https://github.com/grandcentrix/ThirtyInch/pull/65) New: `runOnUiThread(Runnable)` executes code on the view Ui Thread.
- [Pull 65](https://github.com/grandcentrix/ThirtyInch/pull/65) New: `sendToView(view -> { })` automatically executes the action on the Ui Thread
- [Pull 94](https://github.com/grandcentrix/ThirtyInch/pull/94) Always call observer for events which happend when they were registered

**TiFragment**

- [Pull 78](https://github.com/grandcentrix/ThirtyInch/pull/78) Presenter gets correctly destroyed when Fragment is not managed by the `FragmentManager` anymore
- [Pull 78](https://github.com/grandcentrix/ThirtyInch/pull/78), [Pull 67](https://github.com/grandcentrix/ThirtyInch/pull/67) uses now the default: `setRetainInstanceState(false)`. Setting `TiFragment#setRetainInstanceState(true)` will throw
- [Pull 78](https://github.com/grandcentrix/ThirtyInch/pull/78) Support for backstack

**TiActivity**

- [Pull 78](https://github.com/grandcentrix/ThirtyInch/pull/78) Always use `PresenterSavior` singleton, drop support for `NonConfigurationInstance`

**Other**

- [Pull 14](https://github.com/grandcentrix/ThirtyInch/pull/14), [Pull 15](https://github.com/grandcentrix/ThirtyInch/pull/15) `TiLog` is used for logging. Listener to see a log output
- [Pull 19](https://github.com/grandcentrix/ThirtyInch/pull/19), [Pull 24](https://github.com/grandcentrix/ThirtyInch/pull/24) `@DistinctUntilChanged` supports multiple comparators. (`EqualsComparator`, `WeakEqualsComparator`, `HashComparator`(default))
- [Pull 23](https://github.com/grandcentrix/ThirtyInch/pull/23) AppCompat is now included with `provided` dependency instead of `compile`
- [Pull 42](https://github.com/grandcentrix/ThirtyInch/pull/42) New: `TiDialogFragment`
- [Pull 79](https://github.com/grandcentrix/ThirtyInch/pull/79) New: `@CallSuper` where a super call is required.
- [Pull 79](https://github.com/grandcentrix/ThirtyInch/pull/79) New: restrict `TiActivity`, `TiFragment`...  API for subclasses. 
- [Pull 78](https://github.com/grandcentrix/ThirtyInch/pull/78), [Pull 33](https://github.com/grandcentrix/ThirtyInch/pull/33), [Pull 83](https://github.com/grandcentrix/ThirtyInch/pull/83), [Pull 68](https://github.com/grandcentrix/ThirtyInch/pull/68) Fix: `TiPresenter` gets destroyed when `TiFragment` gets removed from the FragmentManager
- [Pull 81](https://github.com/grandcentrix/ThirtyInch/pull/81) New: `Proguard` rules included in the library
- [Pull 78](https://github.com/grandcentrix/ThirtyInch/pull/78) `TiConfiguration#setUseStaticSaviorToRetain(Boolean)` is now deprecated. The `PresenterSavior` singleton is always used to retain presenters when `TiConfiguration#setRetainPresenterEnabled(true)`. 

### rx

- [Pull 27](https://github.com/grandcentrix/ThirtyInch/pull/27) Fix: view can be `null` before unsubscribing from Subscriptions
- [Pull 43](https://github.com/grandcentrix/ThirtyInch/pull/43) New: `manage\[View\]Subscriptions(Subscription...)`
- [Pull 58](https://github.com/grandcentrix/ThirtyInch/pull/), [Pull 61](https://github.com/grandcentrix/ThirtyInch/pull/61) `manageViewSubscription(Subscription)` will now throw when the view isn't attached
- [Pull 61](https://github.com/grandcentrix/ThirtyInch/pull/61) `RxTiPresenterUtils#isViewReady()` now emits the ready event after `onAttachView(TiView)` was called.
- [Pull 73](https://github.com/grandcentrix/ThirtyInch/pull/73) New: `manage\[View\]Subscription` will now return `Subscription`

### rx2

New module for RxJava2 analog to rx module

- [Pull 54](https://github.com/grandcentrix/ThirtyInch/pull/54), [Pull 64](https://github.com/grandcentrix/ThirtyInch/pull/64) New: `RxTiPresenterDisposableHandler#manageDisposable` and `RxTiPresenterDisposableHandler#manageViewDisposable`

### plugin

- [Pull 49](https://github.com/grandcentrix/ThirtyInch/pull/49) New: `TiFragmentPlugin`

### test

- [Pull 65](https://github.com/grandcentrix/ThirtyInch/pull/65) `TiPresenterInstructor` automatically sets an `Executor` for `runOnUiThread` and `sendToView` actions

### logginginterceptor

 - [Pull 85](https://github.com/grandcentrix/ThirtyInch/pull/85) New: Add logging interceptor module including `LoggingInterceptor`

## Version 0.7.1 `07.09.16`
- add missing `TiActivity#getPresenter()`

## Version 0.7.0 `04.09.16`
- `TiConfiguration`
- Presenter LifecycleObservers
- ViewBindingInterceptors
- `TiActivityDelegate` for code sharing
- separate Rx module
- separate Test module
- public release, projects using `Ti`: ∞

## Version 0.6 `11.06.16`
- Tests
- Smaller bugfixes and minor breaking changes

## Version 0.5 `03.05.16`
- plugin for CompositeAndroid
- Clean usage syntax by automatically using the `TiActivity` as the `TiView`
- Projects using `Ti`: 6

## Version 0.4 `12.05.16`
- Extracted into standalone library
- Rebranded to ThirtyInch
- Projects using `Ti`: 3

## Version 0.3 `19.02.16`
- CallOnMainThread annotation
- fix "Don't keep activities" with `PresenterSavior`

## Version 0.2 `02.09.15`
- stabilize Activity and Fragment support

## Version 0.1 `10.04.15`
- first configuration change surviving Presenter
- heavy usage or RxJava

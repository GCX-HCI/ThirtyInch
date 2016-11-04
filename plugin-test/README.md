## Test module for `plugin` module

This module contains the `androidTests` for the `plugin` module.
The `plugin` module itself cannot run tests because it uses `provided` instead of `compile`. 
This reduces problems for users of the library but makes testing harder.

Also, Activities cannot be defined in the `androidTest` package.
A solution would be to put the `TestActivity` into the `debug` buildType.
But shipping an Activity in the `debug` buildType is far from ideal.
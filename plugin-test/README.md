## Test module for `plugin` module

This module contains the tests androidTests for the `plugin` module.
The `plugin` module itself cannot run tests because it uses `provided` instead of `compile`. 
This reduces problems for users of the library but makes testing harder.

Also, Activities cannot be defined in the `androidTest` package.
Moving them into the `debug` buildType doesn't feel right.
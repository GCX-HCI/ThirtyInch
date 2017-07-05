## Release ThirtyInch

To release a new version of ThirtyInch to [jcenter](https://jcenter.bintray.com) we have to run the following command:
```
./gradlew clean bundleRelease bintrayUpload -PbintrayUser=BINTRAY_USERNAME -PbintrayKey=BINTRAY_KEY -PdryRun=false
```
As we use [bintray-release](https://github.com/novoda/bintray-release) you can check out the documentation there if you have any question or issues.

Before we release a new version we have to make sure that we increase the `VERSION_NAME` in the root `build.gradle`.

> **Note:** `-SNAPSHOT` as a version name is not allowed in jcenter.

## Snapshots
At this time we don't provide public `SNAPSHOT` releases.
Anyway. If you want to use the latest and greatest features of Ti (or just want to build it by yourself because it is fun) you can "publish" Ti directly into your `mavenLocal()`. To do it you can run either:
```
./gradlew clean bundleRelease bintrayUpload -PbintrayUser=MockedUserName -PbintrayKey=MockedKey -PdryRun=true
```
(MockedUserName and MockedKey can be any value 😉)

Or you can use directly the `publishToMavenLocal` task:
```
./gradlew clean bundleRelease publishToMavenLocal
```

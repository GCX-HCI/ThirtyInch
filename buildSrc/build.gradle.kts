plugins {
    kotlin("jvm") version "1.3.61"
    `java-gradle-plugin`
}

repositories {
    google()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("guru.stefma.bintrayrelease:bintrayrelease:1.1.2")
    compileOnly("guru.stefma.androidartifacts:androidartifacts:1.4.0") {
        because("BintrayRelease uses this dep as a transitive dependency. " +
                "Unfortunately its not available in the compile classpath. " +
                "But we want to use a class in our PublishPlugin. " +
                "Therefore we have to explicite add this to our compile classpath.")
    }
    // If you update this.
    // Also update the lintVersion inside the ../build.gradle
    implementation("com.android.tools.build:gradle:3.4.2") {
        because("The AndroidArtifacts plugin uses some classes of this plugin." +
                "So we have to move this to this buildSrc classpath." +
                "Otherwise it would lead to compile errors.")
    }
}

gradlePlugin {
    plugins {
        create("bintrayPublish") {
            id = "net.grandcentrix.gradle.publish"
            implementationClass = "net.grandcentrix.gradle.publish.PublishPlugin"
        }
    }
}
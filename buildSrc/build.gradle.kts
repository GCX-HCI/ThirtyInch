plugins {
    kotlin("jvm") version "1.3.21"
    `java-gradle-plugin`
}

repositories {
    google()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("guru.stefma.bintrayrelease:bintrayrelease:1.1.1")
}

gradlePlugin {
    plugins {
        create("bintrayPublish") {
            id = "net.grandcentrix.gradle.publish"
            implementationClass = "net.grandcentrix.gradle.publish.PublishPlugin"
        }
    }
}
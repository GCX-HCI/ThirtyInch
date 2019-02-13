package net.grandcentrix.gradle.publish

import guru.stefma.androidartifacts.ArtifactsExtension
import guru.stefma.bintrayrelease.PublishExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class PublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project.pluginManager) {
            withPlugin("com.android.library") {
                apply("guru.stefma.bintrayrelease")
                (project.extensions.getByName("publish") as PublishExtension).apply {
                    userOrg = "grandcentrix"
                    uploadName = "ThirtyInch"
                    website = "https://github.com/grandcentrix/ThirtyInch"
                    desc = "a Model View Presenter library for Android"
                }
                (project.extensions.getByName("androidArtifact") as ArtifactsExtension).apply {
                    artifactId = project.name
                }
            }
        }
    }
}
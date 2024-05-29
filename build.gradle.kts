// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    kotlin("kapt") version "1.9.23"
    kotlin("jvm") version "1.9.23" apply false
}
buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.9.23"))
    }
}
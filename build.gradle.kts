// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.clistery.gradle")
}

buildscript {
    repositories {
        google()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0")
        classpath("io.realm:realm-gradle-plugin:10.5.0")
        
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4.23.4") //artifactory
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.32") //dokka
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts.kts.kts.kts.kts.kts.kts.kts files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

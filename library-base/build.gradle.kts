import com.clistery.gradle.AppConfig
import com.clistery.gradle.AppDependencies
import com.clistery.gradle.implementation

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("realm-android")
    id("org.jetbrains.dokka")
    id("kre-publish")
}

android {
    compileSdkVersion(AppConfig.compileSdk)
    buildToolsVersion(AppConfig.buildToolsVersion)

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    defaultConfig {
        minSdkVersion(AppConfig.minSdk)
        targetSdkVersion(AppConfig.targetSdk)
        versionCode(AppConfig.versionCode)
        versionName(AppConfig.versionName)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true
    }
    lintOptions {
        isAbortOnError = false
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    packagingOptions {
        exclude("META-INF/rxjava.properties")
        exclude("META-INF/library-base_debug.kotlin_module")
    }
}

dependencies {
    implementation(AppDependencies.baseLibs)
    compileOnly("io.reactivex.rxjava2:rxjava:2.2.19")
    compileOnly("io.reactivex.rxjava2:rxandroid:2.1.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.multidex:multidex:2.0.1")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test:rules:1.0.2")
    androidTestImplementation("com.google.truth:truth:0.31")
    androidTestImplementation("io.reactivex.rxjava2:rxjava:2.2.19")
    androidTestImplementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    kaptAndroidTest(AppDependencies.realm.annotations)
    kaptAndroidTest(AppDependencies.realm.processor)
}

val androidJavadocs by tasks.register<Javadoc>("androidJavadocs") {
    options {
        encoding = Charsets.UTF_8.displayName()
        source = android.sourceSets.flatMap { it.java.srcDirs }.first().absolutePath
        classpath =
            classpath.plus(project.files(android.bootClasspath.joinToString(File.pathSeparator)))
        exclude(listOf("**/*.kt", "**/BuildConfig.java", "**/R.java"))
        isFailOnError = true
        if (this is StandardJavadocDocletOptions) {
            links("http://docs.oracle.com/javase/8/docs/api/")
            linksOffline("http://d.android.com/reference", "${android.sdkDirectory}/docs/reference")
        }
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask> {
    // dependsOn(androidJavadocs)
    dokkaSourceSets {
        named("main") {
            noStdlibLink.set(true)
            noAndroidSdkLink.set(true)
            noJdkLink.set(true)
            includeNonPublic.set(true)
            skipEmptyPackages.set(true)
        }
    }
    offlineMode.set(true)
    // outputDirectory.set(androidJavadocs.destinationDir)
}
val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}
val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-doc")
}
val androidSourcesJar by tasks.register<Jar>("androidSourcesJar") {
    from(android.sourceSets.flatMap { it.java.srcDirs })
    archiveClassifier.set("sources")
}

publishing{
    publications {
        maybeCreate<MavenPublication>("-Release").apply {
            groupId = AppConfig.GROUP_ID
            artifactId = AppConfig.ARTIFACT_ID
            version = AppConfig.versionName

            suppressAllPomMetadataWarnings()

            artifact(dokkaJavadocJar)
            artifact(androidSourcesJar)
            afterEvaluate { artifact(tasks.getByName("bundleReleaseAar")) }
        }
    }
}

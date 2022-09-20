import com.clistery.gradle.AppDependencies

plugins {
    id("kre-publish")
    id("realm-android")
}

android {
    buildTypes.configureEach {
        isMinifyEnabled = false
    }
    packagingOptions {
        exclude("META-INF/rxjava.properties")
        exclude("META-INF/library-base_debug.kotlin_module")
    }
}

dependencies {
    AppDependencies.baseLibs.forEach { implementation(it) }
    compileOnly("io.reactivex.rxjava2:rxjava:2.2.19")
    compileOnly("io.reactivex.rxjava2:rxandroid:2.1.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.multidex:multidex:2.0.1")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test:rules:1.0.2")
    androidTestImplementation("com.google.truth:truth:0.31")
    androidTestImplementation("io.reactivex.rxjava2:rxjava:2.2.19")
    androidTestImplementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    kaptAndroidTest(AppDependencies.realm.annotations)
    kaptAndroidTest(AppDependencies.realm.processor)
    implementation(AppDependencies.clistery.appbasic)
    implementation(AppDependencies.clistery.appinject)
    implementation(AppDependencies.kotlin.reflect)
}

import com.clistery.gradle.AppDependencies

plugins {
    id("app")
    id("realm-android")
}

android {
    defaultConfig {
        applicationId = "com.yh.kre"
        
        buildConfigField("String", "CALL_RECORD_DB", "\"CallRecord\"")
        buildConfigField("long", "RECORD_DB_VERSION", "1")
        buildConfigField("int", "MAX_RETRY_SYNC_RECORD_COUNT", "5")
    }
    buildTypes.configureEach {
        isMinifyEnabled = false
    }
    packagingOptions {
        exclude("META-INF/rxjava.properties")
        exclude("META-INF/sample_debug.kotlin_module")
    }
}

dependencies {
    AppDependencies.baseLibs.forEach { implementation(it) }
    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation(project(mapOf("path" to ":library-base")))
    testImplementation("junit:junit:4.13.2")
    implementation(AppDependencies.clistery.appbasic)
    implementation(AppDependencies.clistery.appinject)
    implementation(AppDependencies.kotlin.reflect)
}

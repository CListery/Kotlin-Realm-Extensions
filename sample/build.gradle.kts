import com.clistery.gradle.AppConfig
import com.clistery.gradle.AppDependencies
import com.clistery.gradle.implementation

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("realm-android")
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
        applicationId = "com.yh.kre"
        minSdkVersion(AppConfig.minSdk)
        targetSdkVersion(AppConfig.targetSdk)
        versionCode(AppConfig.versionCode)
        versionName(AppConfig.versionName)
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        multiDexEnabled = true
        
        buildConfigField("String", "CALL_RECORD_DB", "\"CallRecord\"")
        buildConfigField("long", "RECORD_DB_VERSION", "1")
        buildConfigField("int", "MAX_RETRY_SYNC_RECORD_COUNT", "5")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            // debuggable true
        }
    }
    packagingOptions {
        exclude("META-INF/rxjava.properties")
        exclude("META-INF/sample_debug.kotlin_module")
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(AppDependencies.baseLibs)
    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation(project(mapOf("path" to ":library-base")))
    testImplementation("junit:junit:4.13.2")
}

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation("com.android.tools.build:gradle:4.2.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    implementation("org.jetbrains.kotlin.android:org.jetbrains.kotlin.android.gradle.plugin:1.7.10")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.23.4")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.32")
    implementation("io.realm:realm-gradle-plugin:10.5.0")
}

gradlePlugin {
    plugins {
        create("clistery_plugin") {
            id = "com.clistery.gradle"
            implementationClass = "com.clistery.gradle.CPlugin"
        }
    }
}

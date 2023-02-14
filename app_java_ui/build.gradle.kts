plugins {
    id("com.android.application")
}

android {
    compileSdkVersion(32)

    defaultConfig {
        applicationId = "net.payrdr.mobile.payment.sample.java"
        minSdkVersion(21)
        targetSdkVersion(32)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    packagingOptions {
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/LICENSE-notice.md")
        exclude("META-INF/AL2.0")
        exclude("META-INF/LGPL2.1")
        exclude("META-INF/main.kotlin_module")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/plexus/components.xml")
        exclude("META-INF/sdk_debug.kotlin_module")
        exclude("META-INF/sdk_release.kotlin_module")
        exclude("**/attach_hotspot_windows.dll")
        exclude("META-INF/licenses/**")
        exclude("META-INF/AL2.0")
        exclude("META-INF/LGPL2.1")
    }
}

dependencies {
    implementation(group = "", name = "sdk_forms-release", ext = "aar")
    implementation(group = "", name = "sdk_core-release", ext = "aar")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.devnied.emvnfccard:library:3.0.1")
    implementation("com.google.android.gms:play-services-wallet:18.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.7")
    implementation("com.caverock:androidsvg-aar:1.4")
    implementation("com.google.android.material:material:1.2.0-beta01")
    implementation("io.card:android-sdk:5.5.1")
}
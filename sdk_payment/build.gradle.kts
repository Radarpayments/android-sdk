plugins {
    id("com.android.library")
    id("io.qameta.allure")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    compileSdkVersion(BuildVersionsAndroid.compileSdkVersion)

    defaultConfig {
        minSdkVersion(BuildVersionsAndroid.minSdkVersion)
        targetSdkVersion(BuildVersionsAndroid.targetSdkVersion)
        multiDexEnabled = true
        versionCode = BuildVersionsAndroid.versionCode
        versionName = SDKBuildVersions.sdkPaymentVersion
        testInstrumentationRunner("io.qameta.allure.android.runners.AllureAndroidJUnitRunner")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "SDK_PAYMENT_VERSION_NUMBER",
            "\"${SDKBuildVersions.sdkPaymentVersion}\""
        )
    }

    buildTypes {
        getByName("release") {
            consumerProguardFiles("proguard-rules.pro")
            isTestCoverageEnabled = false
        }
        getByName("debug") {
            isTestCoverageEnabled = false
        }
    }

    sourceSets {
        map { it.java.srcDir("src/${it.name}/kotlin") }
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

    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

allure {
    autoconfigure = true
    version = "2.20.1" // Latest Allure Version

    useJUnit4 {
        version = "2.20.1" // Latest Allure Version
    }
}

dependencies {
    implementation(project(":sdk_forms"))
    implementation(project(":sdk_threeds"))
    implementation(project(":sdk_core"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    //for test
    implementation(Libs.jaxb_api)
    testImplementation(TestLibs.allure_kotlin_model)
    testImplementation(TestLibs.allure_kotlin_commons)
    testImplementation(TestLibs.allure_kotlin_junit4)
    androidTestImplementation(TestLibs.kaspresso)
    androidTestImplementation(TestLibs.kaspresso_allure_support)
    androidTestImplementation(TestLibs.allure_kotlin_android)

    implementation(Libs.kotlin_stdlib)
    implementation(Libs.kotlinx_coroutines_core)
    implementation(Libs.kotlinx_coroutines_android)
    implementation(Libs.appcompat)
    implementation(Libs.androidx_constraintlayout)
    implementation(Libs.android_play_services_wallet)

    testImplementation(TestLibs.junit)
    testImplementation(TestLibs.io_kotest_runner_junit)
    testImplementation(TestLibs.io_kotest_assertion_core)
    testImplementation(TestLibs.androidx_test_ext_junit)
    testImplementation(TestLibs.androidx_test_core)
    testImplementation(TestLibs.io_mockk)
    testImplementation(TestLibs.kotlinx_coroutines_test)
    testImplementation(TestLibs.test_runner)

    androidTestImplementation(TestLibs.io_kotest_assertion_core)
    androidTestImplementation(TestLibs.kotlinx_coroutines_test)
    androidTestImplementation(TestLibs.androidx_test_ext_junit)
    androidTestImplementation(TestLibs.androidx_test_rules)
    androidTestImplementation(TestLibs.androidx_test_espresso_core)
    androidTestImplementation(TestLibs.espresso_contrib)
    androidTestImplementation(TestLibs.io_mockk_android)
    androidTestImplementation(TestLibs.com_squareup_spoon)
    androidTestImplementation(TestLibs.com_squareup_mockwebserver)
}

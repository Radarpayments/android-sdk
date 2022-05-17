plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
//    id("com.jaredsburrows.spoon")
//    id("jacoco")
//    id("plugins.jacoco-report")
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    compileSdkVersion(BuildVersionsAndroid.compileSdkVersion)

    defaultConfig {
        minSdkVersion(BuildVersionsAndroid.minSdkVersion)
        targetSdkVersion(BuildVersionsAndroid.targetSdkVersion)
        multiDexEnabled = true
        versionCode = BuildVersionsAndroid.versionCode
        versionName = SDKBuildVersions.sdkPaymentVersion

        buildConfigField(
            "String",
            "SDK_PAYMENT_VERSION_NUMBER",
            "\"${SDKBuildVersions.sdkPaymentVersion}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
//
//spoon {
//    val isTagBuild = System.getenv().containsKey("CI_COMMIT_TAG")
//
//    title = "RBS Payment SDK"
//    grantAll = true
//    debug = true
//    clearAppDataBeforeEachTest = true
//    noAnimations = true
//    codeCoverage = true
//    shard = !isTagBuild
//
//    if (project.hasProperty("testSize")) {
//        testSize = project.property("testSize") as String
//    }
//}
//
//jacoco {
//    toolVersion = "0.8.4"
//}
//
//tasks.withType<Test> {
//    extensions.configure(JacocoTaskExtension::class) {
//        isIncludeNoLocationClasses = true
//        excludes = listOf("jdk.internal.*")
//    }
//}

dependencies {
    implementation(project(":sdk_forms"))
    implementation(project(":sdk_threeds"))
    implementation(project(":sdk_core"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

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

    androidTestImplementation(TestLibs.kotlinx_coroutines_test)
    androidTestImplementation(TestLibs.androidx_test_ext_junit)
    androidTestImplementation(TestLibs.androidx_test_rules)
    androidTestImplementation(TestLibs.androidx_test_espresso_core)
    androidTestImplementation(TestLibs.espresso_contrib)
    androidTestImplementation(TestLibs.io_mockk_android)
    androidTestImplementation(TestLibs.com_squareup_spoon)
    androidTestImplementation(TestLibs.com_squareup_mockwebserver)
}
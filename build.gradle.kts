// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlin_version by extra("1.7.10")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        jcenter()
        maven(uri("https://oss.jfrog.org/artifactory/oss-snapshot-local/"))
        maven(uri("https://oss.sonatype.org/content/repositories/snapshots"))
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath("io.qameta.allure:allure-gradle:2.8.1")
        classpath(kotlin("gradle-plugin", version = kotlin_version))
        classpath(kotlin("serialization", version = kotlin_version))

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    configurations.all {
        resolutionStrategy {
            force("org.objenesis:objenesis:2.6")
        }
    }
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(uri("https://oss.sonatype.org/content/repositories/snapshots"))
        flatDir {
            dirs("libs")
        }
    }
}

tasks {
    val clean = register("clean", Delete::class) {
        delete(rootProject.buildDir)
    }

    val copyGitHooks = register<Task>("copyGitHooks") {
        copy {
            from(file("config/git/hooks")) {
                rename {
                    if (!it.startsWith("script")) {
                        it.removeSuffix(".sh")
                    } else it
                }
            }
            into(file(".git/hooks"))
        }
    }

    clean {
        dependsOn(copyGitHooks)
    }
}
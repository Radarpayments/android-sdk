// Maven Central publication. A module sets pomArtifactId, pomName, pomDescription (pomAarFile and pomVersion for a
// prebuilt AAR) in extra before applying this script. Version comes from -PsdkVersion, signing key from SIGNING_KEY.

apply(plugin = "maven-publish")
apply(plugin = "signing")

val publishGroup = "io.github.radarpayments"
val publishVersion = if (extra.has("pomVersion")) extra["pomVersion"] as String else findProperty("sdkVersion")?.toString() ?: "local-dev"
val prebuiltAar = if (extra.has("pomAarFile")) file(extra["pomAarFile"] as String) else null

// Signatures are written next to the signed file, so a prebuilt AAR is signed as a copy in build/
val copyPrebuiltAar = prebuiltAar?.let { aar ->
    tasks.register<Copy>("copyPrebuiltAar") {
        from(aar)
        into(buildDir.resolve("prebuilt"))
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(rootProject.file("gradle/publishing/README.md"))
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    if (prebuiltAar == null) {
        from("src/main/kotlin", "src/main/java")
    } else {
        from(rootProject.file("gradle/publishing/card-io-sources/README.md"))
    }
}

afterEvaluate {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("release") {
                groupId = publishGroup
                artifactId = extra["pomArtifactId"] as String
                version = publishVersion

                if (prebuiltAar == null) {
                    from(components["release"])
                } else {
                    artifact(buildDir.resolve("prebuilt/${prebuiltAar.name}")) {
                        extension = "aar"
                        builtBy(copyPrebuiltAar)
                    }
                }
                artifact(sourcesJar)
                artifact(javadocJar)

                pom {
                    name.set(extra["pomName"] as String)
                    description.set(extra["pomDescription"] as String)
                    url.set("https://github.com/Radarpayments/android-sdk")
                    if (prebuiltAar != null) packaging = "aar"
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("radarpayments")
                            name.set("BPC")
                            url.set("https://github.com/Radarpayments")
                        }
                    }
                    scm {
                        url.set("https://github.com/Radarpayments/android-sdk")
                        connection.set("scm:git:https://github.com/Radarpayments/android-sdk.git")
                        developerConnection.set("scm:git:ssh://git@github.com/Radarpayments/android-sdk.git")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "staging"
                url = uri(rootProject.buildDir.resolve("staging-deploy"))
            }
        }
    }

    val signingKey = System.getenv("SIGNING_KEY")
    if (!signingKey.isNullOrBlank()) {
        configure<SigningExtension> {
            useInMemoryPgpKeys(signingKey, System.getenv("SIGNING_PASSWORD"))
            sign(the<PublishingExtension>().publications["release"])
        }
    }
}

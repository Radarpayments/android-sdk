plugins {
    base
}

extra["pomArtifactId"] = "card-io"
extra["pomVersion"] = "5.5.1-OS1"
extra["pomName"] = "card.io for PAYRDR SDK"
extra["pomDescription"] = "card.io 5.5.1, OutSystems fork with 16 KB page size support, used by the PAYRDR SDK for card scanning"
extra["pomAarFile"] = "$rootDir/repo/io/github/radarpayments/card-io/5.5.1-OS1/card-io-5.5.1-OS1.aar"
apply(from = "$rootDir/gradle/publishing.gradle.kts")

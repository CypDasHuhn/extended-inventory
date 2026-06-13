plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "ExtendedInventory"

includeBuild("../rooster-core")
includeBuild("../rooster-localization")
includeBuild("../rooster-sql")
includeBuild("../rooster-ui")

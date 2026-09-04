import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.0.2"
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "8.3.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "dev.CypDasHuhn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.112-stable")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    bukkitLibrary("com.google.code.gson:gson:2.10.1")
    testImplementation("com.google.code.gson:gson:2.10.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.2")
    implementation("com.google.code.gson:gson:2.11.0")

    bukkitLibrary("io.github.classgraph:classgraph:4.8.170")
    testImplementation("io.github.classgraph:classgraph:4.8.170")

    implementation("net.kyori:adventure-api:4.24.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.2:4.116.1")
    testImplementation("io.papermc.paper:paper-api:26.2.build.111-stable")

    implementation("org.jetbrains.exposed:exposed-core:0.49.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.49.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.49.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.49.0")

    implementation("org.xerial:sqlite-jdbc:3.45.2.0")

    bukkitLibrary("org.jetbrains.kotlin:kotlin-stdlib:2.4.10")

    implementation("dev.jorel:commandapi-paper-shade:11.2.0")

    implementation("dev.rooster.db:RoosterDb:1.0-SNAPSHOT")
    implementation("dev.rooster.core:rooster-core:1.0-SNAPSHOT")
    implementation("dev.rooster.localization:rooster-localization:1.0-SNAPSHOT")
    implementation("dev.rooster.ui:RoosterUI:1.0-SNAPSHOT")
    implementation("dev.rooster.ui.sql:RoosterUISql:1.0-SNAPSHOT")
    implementation("dev.rooster:rooster-commands:1.0.0")
    implementation("dev.rooster:command-api:1.0.0")
}

val targetJavaVersion = 25
kotlin {
    jvmToolchain(targetJavaVersion)
}

bukkit {
    name = "ExtendedInventory"
    main = "dev.cypdashuhn.extendedinventory.ExtendedInventoryPlugin"
    apiVersion = "26.2"

    commands {
    }

    permissions {
    }
}

tasks {
    runServer {
        minecraftVersion("26.2")
        jvmArgs("-Dkotlinx.coroutines.debug=off")
    }
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.test {
    useJUnitPlatform()
}

plugins {
    // Kotlin/JVM 2.4.0, current stable (June 2026). Pure-JVM on purpose:
    // Lab 1 is a Kotlin clinic, so there is NO Android/emulator here. Fast feedback,
    // Koans-style. The Android + Compose toolchain arrives in Lab 2.
    kotlin("jvm") version "2.4.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // Coroutines: a FIRST TASTE in this lab (structured concurrency as a systems tool).
    // The heavy coroutines/Flow work lands in Labs 6–7, per the course arc.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    // One task: every exercise runs here. Aim to finish the systems-critical ones live and
    // complete the rest at home before Lab 2.
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
    }
}

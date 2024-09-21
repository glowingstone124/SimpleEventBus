plugins {
    kotlin("jvm") version "2.0.20"
}

group = "ind.glowingstone.eventbus"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.0.20")
}

tasks.test {
    useJUnitPlatform()
}
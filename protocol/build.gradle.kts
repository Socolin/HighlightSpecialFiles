plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("name:rd-gen")
    implementation("name:rider-model")
}

repositories {
    mavenCentral()
}
plugins {
    kotlin("jvm") version "1.5.0-M2"
}

group = "club.eridani"
version = "1.0.0"


val download: Configuration by configurations.creating

tasks.create<Copy>("downloadLibs") {
    from(download)
    into("libs")
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    download(kotlin("script-runtime"))
    download("org.ow2.asm:asm:9.1")
    download("org.ow2.asm:asm-tree:9.1")
    implementation(download)
}


plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "it.unisa.gaia.tdd"
version = "0.3.3"

repositories {
    mavenCentral()
}

dependencies {
    // Http Client for API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
}

// Configure Gradle IntelliJ Plugin
intellij {
    // Target PyCharm Community 2023.2.5
    version.set("2023.2.5")
    type.set("PC")

    // NOTE: We do NOT add "com.intellij.modules.python" here for PyCharm (PC)
    // because it is already built-in.
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        // Setting this to null removes the upper limit (<idea-version until-build="...">)
        untilBuild.set(null as String?)
    }
}
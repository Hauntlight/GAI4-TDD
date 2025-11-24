plugins {
    id("java")
    // This version requires Gradle 8.13+
    id("org.jetbrains.intellij.platform") version "2.10.4"
}

group = "it.unisa.gaia.tdd"
version = "0.3.3"

repositories {
    mavenCentral()
    // Adds the necessary JetBrains repositories for PyCharm and Plugins
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // 1. Use the 'pycharm' helper.
        // In Plugin 2.x, this correctly resolves PyCharm Professional.
        // (PyCharm Community is no longer a build target, Pro contains all features)
        pycharm("2025.2.4")

        // 2. Use 'bundledPlugin' with ID "Pythonid".
        // Because we are targeting PyCharm Pro, the Python plugin is Bundled.
        bundledPlugin("Pythonid")

        // 3. Recommended: Add instrumentation tools
        instrumentationTools()
    }

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
}

intellijPlatform {
    pluginConfiguration {
        version.set(project.version.toString())
        ideaVersion {
            sinceBuild.set("232")
            untilBuild.set(provider { null })
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}
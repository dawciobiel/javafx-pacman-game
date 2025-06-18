plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.1.1"
    kotlin("jvm") version "2.1.0"
}

group = "com.dawciobiel"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Updated dependency versions
val junitVersion = "5.11.4"
val ikonliVersion = "12.3.1"
val controlsfxVersion = "11.2.1"
val jacksonVersion = "2.18.2"
val javafxVersion = "23.0.2"  // Latest stable version
val formsfxVersion = "11.6.0"
val tilesfxVersion = "21.0.9"
val fxglVersion = "17.3"

// Java version
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("com.dawciobiel.pacman")
    mainClass.set("com.dawciobiel.pacman.GameEngine")
}

// JavaFX version
javafx {
    version = javafxVersion
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.web", "javafx.swing", "javafx.media")
}

dependencies {
    // UI Controls
    implementation("org.controlsfx:controlsfx:$controlsfxVersion")
    implementation("com.dlsc.formsfx:formsfx-core:$formsfxVersion") {
        exclude(group = "org.openjfx")
    }

    // Icons
    implementation("org.kordamp.ikonli:ikonli-javafx:$ikonliVersion")
    implementation("org.kordamp.ikonli:ikonli-fontawesome5-pack:$ikonliVersion")

    // Tiles and Charts
    implementation("eu.hansolo:tilesfx:$tilesfxVersion") {
        exclude(group = "org.openjfx")
    }

    // Game Engine
    implementation("com.github.almasb:fxgl:$fxglVersion") {
        exclude(group = "org.openjfx")
        exclude(group = "org.jetbrains.kotlin")
    }

    // JSON Processing
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    // JavaFX Runtime - should match the version used in javafx {} block
    runtimeOnly("org.openjfx:javafx-controls:$javafxVersion:${javafx.platform.classifier}")
    runtimeOnly("org.openjfx:javafx-fxml:$javafxVersion:${javafx.platform.classifier}")
    runtimeOnly("org.openjfx:javafx-media:$javafxVersion:${javafx.platform.classifier}")
    runtimeOnly("org.openjfx:javafx-web:$javafxVersion:${javafx.platform.classifier}")
    runtimeOnly("org.openjfx:javafx-swing:$javafxVersion:${javafx.platform.classifier}")
}

// Conflict resolution configuration
configurations.all {
    resolutionStrategy {
        force("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
        force("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Add more memory for tests
    maxHeapSize = "1G"
}

// JLink configuration with fixes
val osName = System.getProperty("os.name").lowercase()

jlink {
    options.set(
        listOf(
            "--strip-debug",       // Removes debug symbols to reduce size
            "--compress=zip-9",    // Highest compression of modules
            "--no-header-files",   // Exclude header files
            "--no-man-pages"       // Exclude man pages
        )
    )

    jpackage {
        imageName = "Pacman"
        appVersion = "1.0"
        vendor = "Dawciobiel"

        // Use base icon path and append extension based on OS
        val iconBase = "src/main/resources/icons/pacman-icon"
        icon = when {
            osName.contains("linux") -> "$iconBase.png"
            osName.contains("windows") -> "$iconBase.ico"
            osName.contains("mac") || osName.contains("darwin") -> "$iconBase.icns"
            else -> error("Unsupported OS: $osName")
        }

        installerOptions.clear()  // Clear existing options

        when {
            osName.contains("linux") -> {
                installerType = "app-image"  // Use app-image; change to "deb" or "rpm" if desired
                installerOptions.addAll(
                    listOf(
                        "--linux-shortcut",        // Create .desktop shortcut
                        "--linux-package-name", "pacman", "--linux-app-category", "Game"
                    )
                )
            }

            osName.contains("windows") -> {
                installerType = "msi"  // or "exe"
                installerOptions.addAll(
                    listOf(
                        "--win-shortcut",          // Create desktop shortcut
                        "--win-menu",              // Create Start Menu entry
                        "--win-menu-group", "Pacman", "--win-dir-chooser"        // Allow directory choice during installation
                    )
                )
            }

            osName.contains("mac") || osName.contains("darwin") -> {
                installerType = "pkg"  // or "dmg"
                installerOptions.addAll(
                    listOf(
                        "--mac-package-identifier", "com.dawciobiel.pacman",  // Bundle ID
                        "--mac-package-name", "Pacman", "--mac-app-category", "public.app-category.games"
                    )
                )
            }

            else -> error("Unsupported OS: $osName")
        }
    }
}

// Run configuration
tasks.named<JavaExec>("run") {
    jvmArgs = listOf(
        "--module-path", classpath.asPath,
        "--add-modules", "javafx.controls,javafx.fxml,javafx.media,javafx.web,javafx.swing",
    )
}

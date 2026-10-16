plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.allinonelighting"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.allinonelighting"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/LICENSE.md",
            "/META-INF/LICENSE-notice.md",
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// --- Licence coverage (ADR-023) ----------------------------------------------------
// Writes every external module in the release runtime classpath to a file. The
// licences screen is hand-curated; LicenceCoverageTest checks it against this list
// in both directions, so a dependency cannot ship unattributed and an entry cannot
// claim a component that does not ship.

abstract class WriteReleaseModulesTask : DefaultTask() {
    @get:Input
    abstract val rootComponent: Property<org.gradle.api.artifacts.result.ResolvedComponentResult>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun write() {
        val seen = mutableSetOf<org.gradle.api.artifacts.result.ResolvedComponentResult>()
        val modules = sortedSetOf<String>()
        val queue = ArrayDeque(listOf(rootComponent.get()))
        while (queue.isNotEmpty()) {
            val component = queue.removeFirst()
            if (!seen.add(component)) continue
            val id = component.id
            if (id is org.gradle.api.artifacts.component.ModuleComponentIdentifier) {
                modules += "${id.group}:${id.module}"
            }
            component.dependencies
                .filterIsInstance<org.gradle.api.artifacts.result.ResolvedDependencyResult>()
                .forEach { queue += it.selected }
        }
        output.get().asFile.writeText(modules.joinToString("\n", postfix = "\n"))
    }
}

val writeReleaseModules = tasks.register<WriteReleaseModulesTask>("writeReleaseModules") {
    description = "Lists external modules in the release runtime classpath (ADR-023)."
    rootComponent.set(
        configurations.named("releaseRuntimeClasspath")
            .flatMap { it.incoming.resolutionResult.rootComponent },
    )
    output.set(layout.buildDirectory.file("licences/release-runtime-modules.txt"))
}

tasks.withType<Test>().configureEach {
    dependsOn(writeReleaseModules)
    inputs.files(writeReleaseModules)
}

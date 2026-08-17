import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

// Generates ZentrumhubBuildConfig into commonMain from Gradle properties. Provide real values in
// ~/.gradle/gradle.properties or via -P/env (ORG_GRADLE_PROJECT_*); committed defaults are empty so
// no credentials live in VCS. Consumed by AppModule.
val generateZentrumhubConfig = tasks.register("generateZentrumhubConfig") {
    val accountId = providers.gradleProperty("zentrumhub.accountId").orElse("")
    val apiKey = providers.gradleProperty("zentrumhub.apiKey").orElse("")
    val channelId = providers.gradleProperty("zentrumhub.channelId").orElse("sandbox")
    inputs.property("accountId", accountId)
    inputs.property("apiKey", apiKey)
    inputs.property("channelId", channelId)
    val outDir = layout.buildDirectory.dir("generated/zentrumhub/kotlin")
    outputs.dir(outDir)
    doLast {
        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$")
        val pkgDir = outDir.get().asFile.resolve("com/resthalflab/resthalfapp/core/network")
        pkgDir.mkdirs()
        pkgDir.resolve("ZentrumhubBuildConfig.kt").writeText(
            """
            package com.resthalflab.resthalfapp.core.network

            // Generated from Gradle properties (zentrumhub.accountId / .apiKey / .channelId). Do NOT edit.
            internal object ZentrumhubBuildConfig {
                const val ACCOUNT_ID: String = "${esc(accountId.get())}"
                const val API_KEY: String = "${esc(apiKey.get())}"
                const val CHANNEL_ID: String = "${esc(channelId.get())}"
            }
            """.trimIndent() + "\n",
        )
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    androidLibrary {
       namespace = "com.resthalflab.resthalfapp.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        commonMain {
            kotlin.srcDir(generateZentrumhubConfig)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.android)
            implementation(libs.koin.android)
            // Google sign-in (Credential Manager) + Firebase Auth — used by the Android actual of
            // the KMP Google sign-in. The google-services plugin + google-services.json live in
            // :androidApp, which is enough for Firebase to auto-initialize at runtime.
            implementation(libs.firebase.auth)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services)
            implementation(libs.google.identity)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.material.icons.extended)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.uuid)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.noarg)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kamel.image)
            // Decompose Navigation & State Management
            implementation(libs.decompose)
            implementation(libs.decompose.compose)
            implementation(libs.essenty.lifecycle)
            implementation(libs.essenty.statekeeper)
            implementation(libs.essenty.instancekeeper)
            implementation(libs.essenty.backhandler)
            implementation(libs.essenty.lifecycle.coroutines)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.koin.test)
            implementation(libs.kotest.assertions)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
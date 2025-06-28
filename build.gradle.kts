// Top-level build.gradle.kts

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

// You cannot put `classpath` here; it's for `build.gradle` Groovy only

// If needed, use buildscript block (rare in Kotlin DSL)
buildscript {
    dependencies {
        // This is ONLY needed if some old plugin still requires it
        classpath("com.google.gms:google-services:4.4.2")
    }
}

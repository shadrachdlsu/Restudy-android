// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Reads app/google-services.json at build time (declared here, applied in app/build.gradle.kts)
    alias(libs.plugins.google.services) apply false
}
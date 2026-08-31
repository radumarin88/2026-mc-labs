// Top-level build file. Plugins are declared here (apply false) and applied in :app.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // Exercise 1 (Lab 6): declare the KSP plugin here with `apply false`.
}

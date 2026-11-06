plugins {
    alias(libs.plugins.android.application)
    // Turns app/google-services.json into resources Firebase reads on startup (project id, API key, ...)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.restudy"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.restudy"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Firebase: the BoM picks matching versions, so auth/firestore have no version of their own
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)      // anonymous sign-in (gives each install a user id)
    implementation(libs.firebase.firestore) // cloud database where check-ins are saved
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Google services plugin for Firebase
}

android {
    namespace = "com.raufir.bauethms"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.raufir.bauethms"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add packaging options to exclude duplicate files
        packaging {
            resources {
                excludes += "com/itextpdf/text/AGPL.txt"
                excludes += "com/itextpdf/text/LICENSE.txt"
                excludes += "META-INF/AGPL.txt" // Exclude AGPL.txt
                excludes += "META-INF/DEPENDENCIES"
                excludes += "META-INF/LICENSE"
                excludes += "META-INF/LICENSE.txt"
                excludes += "META-INF/license.txt"
                excludes += "META-INF/NOTICE"
                excludes += "META-INF/NOTICE.txt"
                excludes += "META-INF/notice.txt"
                excludes += "META-INF/ASL2.0"
                excludes += "META-INF/*.kotlin_module"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Firebase BoM (Bill of Materials) to manage Firebase version compatibility
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Firebase SDKs
    implementation(libs.firebase.auth.v2101)
    implementation(libs.firebase.database.v2030)
    implementation(libs.firebase.firestore.v2400)
    implementation(libs.play.services.auth) // Add this if using Google Sign-In
    implementation(libs.firebase.storage)
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.firebase:firebase-appcheck-debug") // Debug App Check
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    // iText libraries
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.itextpdf:itextpdf:5.5.13.3")
    implementation("com.itextpdf:itext-pdfa:5.5.13.3")
    implementation("com.itextpdf:itext-xtra:5.5.13.3")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // Add this to resolve commons-imaging issue
    implementation("org.apache.commons:commons-imaging:1.0-alpha2")

    // AndroidX and other libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.location)

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
}
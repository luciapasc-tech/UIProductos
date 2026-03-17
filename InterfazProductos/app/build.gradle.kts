plugins {
    // Usamos las versiones de los catálogos de librerías (libs)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Plugin de Google Services para Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.interfazproductos"
    // Actualizamos a 36 para cumplir con las nuevas librerías de 2026
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.interfazproductos"
        minSdk = 24
        targetSdk = 36 // También subimos el target para estar al día
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- LIBRERÍAS DEL SISTEMA (Vía catálogo libs) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Iconos extendidos (necesario para ShoppingCart, etc.)
    implementation("androidx.compose.material:material-icons-extended")

    // --- FIREBASE ---
    // Usamos el BOM para que todas las librerías de Firebase sean compatibles entre sí
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore-ktx") // Necesario para la base de datos

    // --- IMÁGENES (Coil) ---
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- TEST Y DEBUG ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
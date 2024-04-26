plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.onemb.onembwallpapers"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.onemb.onembwallpapers"
        minSdk = 29
        targetSdk = 34
        versionCode = 5
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    //noinspection UseTomlInstead
    implementation ("com.squareup.retrofit2:retrofit:2.10.0")
    //noinspection UseTomlInstead
    implementation ("com.squareup.retrofit2:converter-gson:2.10.0")
    //noinspection UseTomlInstead
    implementation ("androidx.compose.runtime:runtime-livedata:1.6.6")
    //noinspection UseTomlInstead
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    //noinspection UseTomlInstead
    implementation("io.coil-kt:coil-compose:2.6.0")
    //noinspection UseTomlInstead
    implementation("androidx.navigation:navigation-compose:2.7.7")
    //noinspection UseTomlInstead
    implementation("com.github.yalantis:ucrop:2.2.8-native")
    //noinspection UseTomlInstead
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.7.7")
    //noinspection UseTomlInstead
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    //noinspection UseTomlInstead
    implementation("androidx.core:core-splashscreen:1.0.1")
    //noinspection UseTomlInstead


}
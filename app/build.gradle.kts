plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // 1. 必须首先应用 Hilt 插件
    id("com.google.dagger.hilt.android")

    // 2. 然后应用 KSP 插件
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") // 【新增/检查】应用序列化插件
}

android {
    namespace = "com.example.dydemo"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.dydemo"
        minSdk = 29
        targetSdk = 36
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
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//    kotlin {
//        // 使用 JVM Toolchain 统一版本
//        jvmToolchain(11) // 推荐使用 JDK 17 (或 11)
//    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.play.services.cast.tv)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp(libs.hilt.compiler)

    val room_version = "2.8.3"
    implementation("androidx.room:room-runtime:$room_version")
    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp("androidx.room:room-compiler:$room_version")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")
    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$room_version")
    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$room_version")
    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$room_version")
    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$room_version")
    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")
    ksp("com.google.dagger:hilt-android-compiler:2.57.1")
    // 使用 Compose Navigation，方便注入 ViewModel
    implementation(libs.hilt.navigation.compose)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // 添加 Coil Compose 依赖
    implementation("io.coil-kt:coil-compose:2.6.0")
}
plugins {
  id("com.android.application")
  kotlin("plugin.compose")
  id("org.jetbrains.compose")
  kotlin("multiplatform")
}

kotlin {
  androidTarget()

  jvmToolchain(17)

  sourceSets {
    val commonMain by getting {
      dependencies {
        // Use an Amper Module as a dependency
        implementation(projects.shared)
      }
    }

    val androidMain by getting {
      dependencies {
        implementation(libs.compose.ui)
        implementation(libs.compose.foundation)
        implementation(libs.compose.ui.tooling.preview)
        implementation(libs.compose.material3)
        implementation(libs.androidx.activity.compose)
      }
    }
  }
}
android {
  namespace = "com.dweb.subsoil.android"
  compileSdk = 34
  defaultConfig {
    applicationId = "com.dweb.subsoil.android"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
  }
  buildFeatures {
    compose = true
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false // 开启代码混淆
      isShrinkResources = true // 移除无用资源
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}

plugins {

  id("com.android.application")
  kotlin("plugin.compose")
  id("org.jetbrains.compose")
  kotlin("multiplatform")
}

repositories {
  mavenCentral()
  google()
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
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

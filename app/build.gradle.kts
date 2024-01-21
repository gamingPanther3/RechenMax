plugins {
    id("com.android.application")
}

android {
    namespace = "com.mlprograms.rechenmax"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.mlprograms.rechenmax"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.6.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // JUnit is added by default in Android Studio projects.
    // It's used for pure unit tests and also Espresso UI tests
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.testng:testng:6.9.6")
    implementation("junit:junit:4.13.2")
    implementation("org.apache.commons:commons-math3:3.6.1")
    testImplementation("junit:junit:4.13.1")
}
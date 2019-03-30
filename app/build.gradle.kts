import de.dbaelz.demo.androidheadsvienna.build.BuildConfig
import de.dbaelz.demo.androidheadsvienna.build.generateVersionCode

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}


val demoApiToken: String by project

android {
    compileSdkVersion(BuildConfig.compileSdkVersion)
    defaultConfig {
        applicationId = "de.dbaelz.demo.androidheadsvienna"
        minSdkVersion(BuildConfig.minSdkVersion)
        targetSdkVersion(BuildConfig.targetSdkVersion)
        versionCode = generateVersionCode()
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DEMO_API_TOKEN", demoApiToken)
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${BuildConfig.kotlinVersion}")
    implementation("androidx.appcompat:appcompat:${BuildConfig.appCompatVersion}")
    implementation("androidx.core:core-ktx:1.1.0-alpha05")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    testImplementation("junit:junit:4.12")

    androidTestImplementation("androidx.test:runner:1.1.2-alpha02")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0-alpha02") {
        exclude(group = "androidx.test.espresso", module = "just-for-the-demo")
    }
}

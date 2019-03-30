package de.dbaelz.demo.androidheadsvienna.build

object BuildConfig {
    val minSdkVersion = 24
    val targetSdkVersion = 28
    val compileSdkVersion = targetSdkVersion

    val buildToolsVersion = "28.0.3"
    val kotlinVersion = "1.3.21"

    val appCompatVersion = "1.1.0-alpha03"
}

fun generateVersionCode(): Int {
    var result = Runtime.getRuntime().exec("git rev-list HEAD --count")
        .inputStream.bufferedReader().readText().trim()
    if (result.isEmpty()) result = "1"
    return result.toInt()
}
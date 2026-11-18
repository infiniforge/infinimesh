plugins { id("com.android.application") version "8.9.2" }
android {
    namespace = "com.infinimesh"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.infinimesh"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.1.1"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("io.github.gretchycat.libinfinemesh:mesh-android:0.1.1")
    implementation("io.github.gretchycat.libinfiniselect:select-android:0.1.0")
}

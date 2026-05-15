plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "co.ynd.interview.tomek.test.app"
    compileSdk = 36
    targetProjectPath = ":app"

    defaultConfig {
        minSdk = 26
        targetSdk = 36
        testInstrumentationRunner = "co.ynd.interview.tomek.core.testing.KoinTestRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core-domain"))
    implementation(project(":core-testing"))
    implementation(project(":feature-feed"))
    implementation(project(":feature-feed-navigation"))
    implementation(project(":feature-camera-navigation"))

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    implementation(libs.androidx.test.core)
    implementation(libs.koin.test)
    implementation(libs.koin.test.junit4)
    implementation(libs.androidx.compose.ui.test.junit4)
}

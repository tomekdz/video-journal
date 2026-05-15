plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "co.ynd.interview.tomek.core.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "co.ynd.interview.tomek.core.testing.KoinTestRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":core-database"))

    implementation(libs.koin.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.sqldelight.coroutines)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqldelight.sqlite.driver)
}

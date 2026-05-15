plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
}

android {
    namespace = "co.ynd.interview.tomek.core.database"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("VideoJournalDatabase") {
            packageName.set("co.ynd.interview.tomek.core.database")
        }
    }
}

dependencies {
    implementation(libs.sqldelight.android.driver)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.koin.android)
}

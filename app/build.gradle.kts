plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

repositories {
    maven { url = uri("https://dl.bintray.com/robertfmurdock/zegreatrob") }
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.pillar.gizmogrokker"
        minSdkVersion(27)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        val release by getting {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packagingOptions {
        exclude("META-INF/atomicfu.kotlin_module")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to arrayOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.21")
    implementation("androidx.appcompat:appcompat:1.1.0-alpha03")
    implementation("androidx.core:core-ktx:1.1.0-alpha05")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.0-alpha")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.2.0-alpha")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.0.0")

    debugImplementation("androidx.fragment:fragment-testing:1.1.0-alpha05")
    debugImplementation("androidx.test:runner:1.1.2-alpha02")

    testImplementation("org.mockito:mockito-core:2.24.5")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
    testImplementation("junit:junit:4.12")
    testImplementation("com.zegreatrob.testmints:standard:1.3.2")

    androidTestImplementation("com.zegreatrob.testmints:standard:1.3.2")

    androidTestImplementation("androidx.test:runner:1.1.2-alpha02")

    androidTestImplementation("androidx.test:rules:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0-alpha02")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.1.1")
    androidTestImplementation("com.schibsted.spain:barista:2.10.0") {
        exclude(group = "com.android.support")
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "androidx.test.espresso")
    }
}

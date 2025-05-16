plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

}

android {
    namespace = "com.luchodevs.tx"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.luchodevs.tx"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = "7.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Definir el signingConfig correctamente
    signingConfigs {
        create("release") { // Usar create() para definir el signingConfig "release"
            keyAlias = "key0"
            keyPassword = "y1053793374B"
            storeFile = file("C:/Users/usuario/AndroidStudioProjects/MyKeysAndroid.jks")
            storePassword = "y1053793374B"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release") // Ahora "release" está definido correctamente
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    //navComponent
    val navVersion = "2.8.9"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    // Room dependencies
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")


    implementation ("androidx.work:work-runtime:2.9.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

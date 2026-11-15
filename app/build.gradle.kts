import java.util.Properties

plugins {
    id("com.android.application")
}

android {
    namespace = "dev.plume"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.plume.advanced"
        minSdk = 26
        targetSdk = 36
        versionCode = 11
        versionName = "0.1.0"
        testInstrumentationRunner = "dev.plume.ActivityTest"
        val changelog = rootProject.file("CHANGELOG.md").readText().replace("\r", "")
        val section = { v: String -> changelog.substringAfter("\n## $v\n", "").substringBefore("\n## ") }
        val minor = versionName!!.split('.').take(2).joinToString(".")
        val notes = (section(versionName!!) + if (minor != versionName) "\n" + section(minor) else "")
            .lines().filter { it.startsWith("- ") || it.startsWith("### ") }
            .map { if (it.startsWith("### ")) "#" + it.removePrefix("### ") else it.removePrefix("- ") }
        require(notes.isNotEmpty()) { "CHANGELOG.md has no entries under \"## $versionName\"" }
        resValue("string", "whats_new", notes.joinToString("\\n") { it.replace("'", "\\'").replace("\"", "\\\"") })
    }

    buildFeatures {
        resValues = true
    }

    lint {
        baseline = file("lint-baseline.xml")
        disable += setOf("MissingTranslation", "ExtraTranslation")
    }

    // Release key lives outside the repo; keystore.properties (git-ignored) points at it. Without that file
    val keys = rootProject.file("keystore.properties").takeIf { it.exists() }
        ?.let { f -> Properties().apply { f.inputStream().use { load(it) } } }
    signingConfigs {
        if (keys != null) create("release") {
            storeFile = file(keys.getProperty("storeFile"))
            storePassword = keys.getProperty("storePassword")
            keyAlias = keys.getProperty("keyAlias")
            keyPassword = keys.getProperty("keyPassword")
        }
    }

    packaging {
        resources {
            excludes += setOf("kotlin/**", "**/*.kotlin_builtins", "META-INF/*.kotlin_module", "META-INF/*.version", "DebugProbesKt.bin")
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    buildTypes {
        release {
            vcsInfo.include = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation("androidx.activity:activity:1.10.1")
    implementation("androidx.core:core-ktx:1.16.0")
    testImplementation("junit:junit:4.13.2")
}

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val implementation by configurations
val testImplementation by configurations

apply(plugin = "org.jetbrains.kotlin.android")
apply(plugin = "org.jetbrains.kotlin.android.extensions")
apply(plugin = "org.jetbrains.kotlin.kapt")

buildscript {
    Dependencies.repoHandler(repositories)

    dependencies {
        classpath(kotlin(Constants.KOTLIN_GRADLE, Versions.KOTLIN))
    }
}

// Required dependencies
dependencies {
    implementation(kotlin(Constants.KOTLIN_JDK, Versions.KOTLIN))

    implementation(Dependencies.App.ANNOTATION)
    implementation(Dependencies.App.COROUTINES)
    implementation(Dependencies.App.KOIN)
}

// Test Dependencies
dependencies {
    testImplementation(Dependencies.Test.J_UNIT)
    testImplementation(Dependencies.Test.KOIN)
    testImplementation(Dependencies.Test.MOCKK)
    testImplementation(Dependencies.Test.COROUTINES)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs =
            freeCompilerArgs + listOf("-Xinline-classes", "-Xskip-metadata-version-check")
        jvmTarget = Config.JDK.toString()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events(PASSED, SKIPPED, FAILED, STANDARD_OUT, STANDARD_ERROR)
    }
}

apply(from = rootProject.file("./gradle/formatter.gradle.kts"))
apply(from = rootProject.file("./gradle/jacoco.gradle.kts"))

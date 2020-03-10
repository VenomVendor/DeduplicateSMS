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
    implementation(kotlin(Constants.KOTLIN_GRADLE, Versions.KOTLIN))

    implementation(Dependencies.App.COROUTINES)
    implementation(Dependencies.App.KOIN)
}

// Test Dependencies
dependencies {
    testImplementation(Dependencies.Test.J_UNIT)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xinline-classes"
    )
}

apply(from = rootProject.file("./gradle/formatter.gradle.kts"))

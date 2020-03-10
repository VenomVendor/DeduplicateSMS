// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    Dependencies.repoHandler(repositories)

    dependencies {
        // Android build tool for gradle
        classpath(Dependencies.Build.GRADLE_BUILD_TOOL)

        // Kotlin for gradle
        classpath(kotlin(Constants.KOTLIN_GRADLE, version = Versions.KOTLIN))
    }
}

allprojects {
    Dependencies.repoHandler(repositories)
    buildscript {
        Dependencies.repoHandler(repositories)
    }
}

// Clean up
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

import org.gradle.api.artifacts.dsl.RepositoryHandler

object Versions {
    // Build
    const val GRADLE_BUILD_TOOL = "4.0.0-alpha09"
    const val KOTLIN = "1.3.61"

    // Semantic Version
    const val MAJOR = 3
    const val MINOR = 0
    const val PATCH = 0

    // Phone number parser
    const val PHONE_NUMBER_PARSER = "8.10.10"
    const val KT_LINT = "0.36.0"
    const val J_UNIT = "4.12"
}

object Dependencies {

    fun repoHandler(repositories: RepositoryHandler) {
        repositories.jcenter()
        repositories.google()
    }

    object App {
        const val PHONE_NUMBER_PARSER =
            "com.googlecode.libphonenumber:libphonenumber:${Versions.PHONE_NUMBER_PARSER}"
    }

    object Build {
        const val GRADLE_BUILD_TOOL = "com.android.tools.build:gradle:${Versions.GRADLE_BUILD_TOOL}"
        const val KT_LINT = "com.pinterest:ktlint:${Versions.KT_LINT}"
    }

    object Test {
        const val J_UNIT = "junit:junit:${Versions.J_UNIT}"
    }
}

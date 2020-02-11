import java.io.ByteArrayOutputStream

val isCi = System.getenv("CI")?.toBoolean() ?: false

val ktlint: Configuration by configurations.creating

dependencies {
    ktlint(Dependencies.Build.KT_LINT)
}

// Task to check formatting of kotlin code.
task("ktLint", JavaExec::class) {
    group = "verification"
    description = "Check Kotlin code style."
    main = "com.pinterest.ktlint.Main"
    classpath = configurations.getByName("ktlint")

    args(
        "src/**/*.kt",
        rootProject.file({ "**/*.gradle.kts" }),
        rootProject.file({ "buildSrc/src/**/*.kt" })
    )
}

tasks.named("preBuild") {
    dependsOn("ktLint")
}

// Task to format kotlin code.
task("ktlintFormat", JavaExec::class) {
    group = "formatting"
    description = "Fix Kotlin code style deviations."
    main = "com.pinterest.ktlint.Main"
    classpath = configurations.getByName("ktlint")
    args(
        "-F",
        "src/**/*.kt",
        rootProject.file({ "**/*.gradle.kts" }),
        rootProject.file({ "buildSrc/src/**/*.kt" })
    )
    onlyIf { !isCi }
}

if (!isCi) {
    val ktxLint = rootProject.file(".idea/.ktlint")
    if (!ktxLint.exists()) {
        ByteArrayOutputStream().let { os ->
            println(
                """
*************************************************
*********** Ensure ktlint is installed **********
************** brew install ktlint **************
*************************************************"""
            )

            var result = exec {
                workingDir = rootProject.file("./")
                commandLine = listOf("ktlint", "installGitPreCommitHook")
                standardInput = System.`in`
                standardOutput = os
                errorOutput = os
                isIgnoreExitValue = true
            }

            if (result.exitValue != 0) {
                println(os.toString())
                throw GradleException("Script execution failed. See above log for more details.")
            }

            result = exec {
                workingDir = rootProject.file("./")
                commandLine = listOf("ktlint", "--android", "applyToIDEAProject", "-y")
                standardInput = System.`in`
                standardOutput = os
                errorOutput = os
                isIgnoreExitValue = true
            }
            if (result.exitValue != 0) {
                println(os.toString())
                throw GradleException("Script execution failed. See above log for more details.")
            }

            ktxLint.createNewFile()
        }
    }
}

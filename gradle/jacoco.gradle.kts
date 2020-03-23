apply(plugin = "jacoco")

tasks.register("jacocoReport", JacocoReport::class) {
    group = "Reporting"
    description = "Generate Jacoco coverage reports"

    val testVariant = "testDebugUnitTest"

    val srcTree = fileTree("$projectDir/src/main/kotlin")
    val classTree = fileTree(baseDir = "$buildDir/tmp/kotlin-classes/debug") {
        exclude(ignoreClassList())
    }
    val resultTree = fileTree(baseDir = buildDir) {
        include("**/*.exec", "**/*.ec")
    }

    sourceDirectories.from(srcTree)
    classDirectories.from(classTree)
    executionData.from(resultTree)

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        xml.destination = file("$buildDir/reports/jacoco/report.xml")
        html.destination = file("$buildDir/reports/jacoco/html")
    }

    dependsOn(testVariant)
}

fun ignoreClassList(): List<String> {
    return listOf(
        "**/AutoValue_*.*",
        "**/*JavascriptBridge.class",
        "*/R.class",
        "**/R$*.class",
        "**/*\$ViewInjector*.*",
        "*/*\$ViewBinder*.*",
        "**/Manifest*.*",
        "**/android/**/*.*",
        "**/BuildConfig.*",
        "**/model/**/*.*",
        "**/PlaceHolder.*",
        "**/Dummy.*"
    )
}

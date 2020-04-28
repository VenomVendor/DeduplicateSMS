rootProject.name = "DeduplicateSMS"

fun configureModule(name: String, dir: String? = null) {
    include(":$name")

    val moduleDir = dir ?: name
    project(":$name").apply {
        projectDir = File(settingsDir, moduleDir)
        buildFileName = "$name.gradle.kts"

        require(projectDir.isDirectory) { "Project '${this.path} must have a $projectDir directory" }
        require(buildFile.isFile) { "Project '${this.path} must have a $buildFile build script" }
    }
}

configureModule("app", "deduplicate-app")
configureModule("search", "deduplicate-search")
configureModule("core", "deduplicate-core")

fun properties(key: String) = project.findProperty(key).toString()

version = properties("PluginVersion")

plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.2" // https://github.com/JetBrains/gradle-intellij-plugin/releases
    id("com.jetbrains.rdgen") version "2024.1.1" // https://github.com/JetBrains/rd/releases
}

repositories {
    maven("https://cache-redirector.jetbrains.com/intellij-repository/snapshots")
    maven("https://cache-redirector.jetbrains.com/maven-central")
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

intellij {
    type.set("RD")
    version.set(properties("ProductVersion"))
    downloadSources.set(false)
    instrumentCode.set(false)
    // plugins = listOf("uml", "com.jetbrains.ChooseRuntime:1.0.9")
}

val rdLibDirectory: () -> File = { file("${tasks.setupDependencies.get().idea.get().classes}/lib/rd") }

tasks {
    wrapper {
        gradleVersion = "8.4"
        distributionType = Wrapper.DistributionType.ALL
        distributionUrl = "https://cache-redirector.jetbrains.com/services.gradle.org/distributions/gradle-${gradleVersion}-all.zip"
    }

    sourceSets {
        main {
            kotlin.srcDir("src/rider/main/kotlin")
            resources.srcDir("src/rider/main/resources")
        }
    }

    create("compileDotNet") {
        doLast {
            val arguments = listOf(
                "msbuild",
                "/t:Restore;Rebuild",
                properties("DotnetSolution"),
                "/p:Configuration=${properties("BuildConfiguration")}",
                "/p:HostFullIdentifier=",
                "/p:AssemblyVersion=${version}"
            )
            exec {
                executable = "dotnet"
                args = arguments
                workingDir = rootDir
            }
        }
    }

    prepareSandbox {
        dependsOn(":compileDotNet")

        val outputFolder = "${rootDir}/src/dotnet/${properties("DotnetPluginId")}/bin/${properties("DotnetPluginId")}/${properties("BuildConfiguration")}"
        val dllFiles = listOf(
            "$outputFolder/${properties("DotnetPluginId")}.dll",
            "$outputFolder/${properties("DotnetPluginId")}.pdb",
            // TODO: add additional assemblies
        )

        dllFiles.forEach { f ->
            val file = file(f)
            from(file) { into("${rootProject.name}/dotnet") }
        }

        doLast {
            dllFiles.forEach { f ->
                val file = file(f)
                if (!file.exists()) throw RuntimeException("File ${file} does not exist")
            }
        }
    }

    rdgen {
        val modelDir = File(rootDir, "protocol/src/main/kotlin/model")
        val csOutput = File(rootDir, "src/dotnet/${properties("DotnetPluginId")}/Rider")
        val ktOutput = File(rootDir, "src/rider/main/kotlin/${properties("DotnetPluginId").replace('.', '/').toLowerCase()}")

        verbose = true
        classpath("${rdLibDirectory()}/rider-model.jar")
        sources("${modelDir}/rider")
        hashFolder = buildDir.toString()
        packages = "model.rider"

        generator {
            language = "kotlin"
            transform = "asis"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            namespace = "com.jetbrains.rider.model"
            directory = ktOutput.toString()
        }

        generator {
            language = "csharp"
            transform = "reversed"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            namespace = "JetBrains.Rider.Model"
            directory = csOutput.toString()
        }
    }

}

/*


tasks.create("testDotNet") {
    doLast {
        exec {
            executable = "dotnet"
            args("test", DotnetSolution, "--logger", "GitHubActions")
            workingDir = rootDir
        }
    }
}


tasks.runIde {
    maxHeapSize = "1500m"
    autoReloadPlugins = false
    // jbrVersion = "jbr_jcef-11_0_6b765.40" // https://confluence.jetbrains.com/display/JBR/Release+notes
}

tasks.create("patchPluginXml") {
    val changelogText = file("${rootDir}/CHANGELOG.md").readText()
    val changelogMatches = Regex("(?s)(-.+?)(?=##|$)").findAll(changelogText)

    val changeNotes = changelogMatches.map {
        it.groupValues[1].replace(Regex("(?s)\r?\n"), "<br />\n")
    }.take(1).joinToString("")

    doLast {
        patchPluginXml {
            changeNotes = changeNotes
        }
    }
}

tasks.create("prepareSandbox") {
    dependsOn(tasks.getByName("compileDotNet"))

    val outputFolder = "${rootDir}/src/dotnet/${DotnetPluginId}/bin/${DotnetPluginId}/${BuildConfiguration}"
    val dllFiles = listOf(
        "$outputFolder/${DotnetPluginId}.dll",
        "$outputFolder/${DotnetPluginId}.pdb",
        // TODO: add additional assemblies
    )

    dllFiles.forEach { f ->
        val file = file(f)
        from(file) { into("${rootProject.name}/dotnet") }
    }

    doLast {
        dllFiles.forEach { f ->
            val file = file(f)
            if (!file.exists()) throw RuntimeException("File ${file} does not exist")
        }
    }
}


*/

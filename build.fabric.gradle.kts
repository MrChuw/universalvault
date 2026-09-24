@file:Suppress("UnstableApiUsage")

plugins {
    id("dev.kikugie.loom-back-compat")
    id("fabric-loom")
    id("dev.kikugie.postprocess.jsonlang")
    id("me.modmuss50.mod-publish-plugin")
}

version = "${property("mod.version")}+${property("deps.minecraft")}-fabric"
base.archivesName = property("mod.id") as String

jsonlang {
    languageDirectories = listOf("assets/${property("mod.id")}/lang")
    prettyPrint = true
}

val requiredJava = when {
    sc.current.parsed >= "26.1"   -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18"   -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17"   -> JavaVersion.VERSION_16
    else                          -> JavaVersion.VERSION_1_8
}

repositories {
    mavenLocal()
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    maven("https://maven.terraformersmc.com/") { name = "ModMenu" }
    maven("https://maven.nucleoid.xyz/") { name = "Placeholder API" }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric-loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric-api")}")

    modImplementation("com.terraformersmc:modmenu:${property("deps.mod_menu")}")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

val modId = property("mod.id") as String

val possibleAwLocations = listOf(
    rootProject.file("src/main/resources/$modId.accesswidener"),
    rootProject.file("common/src/main/resources/$modId.accesswidener")
)
val rawAwFile = possibleAwLocations.firstOrNull { it.exists() }
val generatedAwPath = layout.buildDirectory.file("generated/accesswidener/$modId.accesswidener")
val resolvedNamespace = if (sc.current.parsed >= "26.1") "official" else "named"

if (rawAwFile != null && rawAwFile.exists()) {
    val outFile = generatedAwPath.get().asFile
    val lines = rawAwFile.readLines()
    val updatedLines = lines.mapIndexed { index, line ->
        if (index == 0 && (line.startsWith("accessWidener") || line.startsWith("classTweaker"))) {
            "accessWidener v2 $resolvedNamespace"
        } else {
            line
        }
    }
    val content = updatedLines.joinToString("\n")
    if (!outFile.exists() || outFile.readText() != content) {
        outFile.parentFile.mkdirs()
        outFile.writeText(content)
    }
}

abstract class GenerateAccessWidenerTask : DefaultTask() {
    @get:InputFile
    @get:Optional
    abstract val templateFile: RegularFileProperty

    @get:Input
    abstract val targetNamespace: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val src = templateFile.orNull?.asFile ?: return
        if (!src.exists()) return

        val ns = targetNamespace.get()
        val lines = src.readLines()
        val updatedLines = lines.mapIndexed { index, line ->
            if (index == 0 && (line.startsWith("accessWidener") || line.startsWith("classTweaker"))) {
                "accessWidener v2 $ns"
            } else {
                line
            }
        }

        val out = outputFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(updatedLines.joinToString("\n"))
    }
}

val generateAccessWidener = tasks.register<GenerateAccessWidenerTask>("generateAccessWidener") {
    if (rawAwFile != null) {
        templateFile.set(rawAwFile)
    }
    targetNamespace.set(resolvedNamespace)
    outputFile.set(generatedAwPath)
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

loom {
    if (rawAwFile != null) {
        accessWidenerPath = generatedAwPath.get().asFile
    }

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

//    runConfigs.named("server") {
//        preferGradleTask = true
//        generateRunConfig = true
//        runDirectory = file("run/server")
//        jvmArguments.add("-Dminecraft.eula=true")
//    }

    runConfigs.named("client") {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = file("run")
        jvmArguments.add("-Dmixin.debug.export=true")
    }
}

tasks {
    processResources {
        dependsOn("stonecutterGenerate", generateAccessWidener)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        exclude("**/neoforge.mods.toml", "**/mods.toml", "**/accesstransformer.cfg")

        val props = mapOf(
            "mod_id" to project.property("mod.id") as String,
            "mod_name" to project.property("mod.name") as String,
            "mod_description" to project.property("mod.description") as String,
            "mod_version" to project.property("mod.version") as String,
            "mod_authors" to project.property("mod.authors") as String,
            "mod_repo_url" to project.property("mod.repo_url") as String,
            "mod_license" to project.property("mod.license") as String,
            "mod_logo" to project.property("mod.logo") as String,
            "fabric_version_range" to project.property("deps.fabric_version_range") as String
        )

        filesMatching(listOf("fabric.mod.json")) { expand(props) }
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        if (rawAwFile != null) {
            from(generatedAwPath) {
                into("")
            }
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

tasks.processResources {
    notCompatibleWithConfigurationCache("Uses Stonecutter's version properties")
}

//publishMods {
//    file = tasks.remapJar.map { it.archiveFile.get() }
//    additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile.get() })
//
//    val modVersion = property("mod.version") as String
//    type = if (modVersion.contains("alpha")) ALPHA else if (modVersion.contains("beta")) BETA else STABLE
//    displayName = "${property("mod.name")} $modVersion for ${stonecutter.current.version} Fabric"
//    version = "${modVersion}+${property("deps.minecraft")}-fabric"
//    changelog = provider { rootProject.file("CHANGELOG.md").readText() }
//    modLoaders.add("fabric")
//
//    modrinth {
//        projectId = property("publish.modrinth") as String
//        accessToken = env.MODRINTH_API_KEY.orNull()
//        minecraftVersions.add(stonecutter.current.version)
//        minecraftVersions.addAll(additionalVersions)
//        requires("fabric-api")
//    }
//    curseforge {
//        projectId = property("publish.curseforge") as String
//        accessToken = env.CURSEFORGE_API_KEY.orNull()
//        minecraftVersions.add(stonecutter.current.version)
//        minecraftVersions.addAll(additionalVersions)
//        requires("fabric-api")
//    }
//}

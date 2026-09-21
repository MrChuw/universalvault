plugins {
    id("net.neoforged.moddev")
    id("dev.kikugie.postprocess.jsonlang")
    id("me.modmuss50.mod-publish-plugin")
}

version = "${property("mod.version")}+${property("deps.minecraft")}-universal"
base.archivesName = property("mod.id") as String

jsonlang {
    languageDirectories = listOf("assets/${property("mod.id")}/lang")
    prettyPrint = true
}

repositories {
    mavenLocal()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
}

dependencies {
    compileOnly("net.fabricmc:fabric-loader:${property("deps.fabric-loader")}")
    compileOnly("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric-api")}")


    compileOnly("net.fabricmc.fabric-api:fabric-api-base:2.0.6+fcdff87fa5")
    compileOnly("net.fabricmc.fabric-api:fabric-networking-api-v1:6.3.8+fcdff87fa5")
    compileOnly("net.fabricmc.fabric-api:fabric-item-group-api-v1:4.2.9+9ec45cd8f1")
    compileOnly("net.fabricmc.fabric-api:fabric-screen-handler-api-v1:1.3.99+fd37071f40")
    compileOnly("net.fabricmc.fabric-api:fabric-command-api-v2:3.1.2+fcdff87fa5")
    compileOnly("net.fabricmc.fabric-api:fabric-lifecycle-events-v1:4.1.9+ffef5f67a5")
    compileOnly("net.fabricmc.fabric-api:fabric-keybindings-v0:0.2.9+b4f4f6cdd2")

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}


neoForge {
    version = property("deps.neoforge") as String
    validateAccessTransformers = true

    runs {
        register("client") { gameDirectory = file("run/"); client() }
        register("server") { gameDirectory = file("run/"); server() }
    }
    mods {
        register(property("mod.id") as String) { sourceSet(sourceSets["main"]) }
    }
    sourceSets["main"].resources.srcDir("src/main/generated")
}

tasks {
    processResources {
        // Não excluímos nenhum metadado: os dois vão pro jar (universal)
        dependsOn("stonecutterGenerate")
    }
    named("createMinecraftArtifacts") { dependsOn("stonecutterGenerate") }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

val additionalVersions: List<String> = (findProperty("publish.additionalVersions") as String?)
    ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

tasks.named<ProcessResources>("processResources") {
    val props = HashMap<String, String>().apply {
        this["mod_id"] = project.property("mod.id") as String
        this["mod_name"] = project.property("mod.name") as String
        this["mod_description"] = project.property("mod.description") as String
        this["mod_version"] = project.property("mod.version") as String
        this["mod_authors"] = project.property("mod.authors") as String
        this["mod_repo_url"] = project.property("mod.repo_url") as String
        this["mod_license"] = project.property("mod.license") as String
        this["mod_logo"] = project.property("mod.logo") as String
        this["minecraft_version_range_neoforge"] = project.property("deps.minecraft_version_range_neoforge") as String
        this["minecraft_version_range_fabric"] = project.property("deps.minecraft_version_range_fabric") as String
    }

    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) { expand(props) }
    filesMatching("*.mixins.json") { expand("java" to "JAVA_25") }
}

//publishMods {
//    file = tasks.jar.map { it.archiveFile.get() }
//    additionalFiles.from(tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar").map { it.archiveFile.get() })
//
//    val modVersion = property("mod.version") as String
//    type = if (modVersion.contains("alpha")) ALPHA else if (modVersion.contains("beta")) BETA else STABLE
//    displayName = "${property("mod.name")} $modVersion for ${stonecutter.current.version} Universal"
//    version = "${modVersion}+${property("deps.minecraft")}"
//    changelog = provider { rootProject.file("CHANGELOG.md").readText() }
//    modLoaders.addAll("neoforge", "fabric")
//
//    modrinth {
//        projectId = property("publish.modrinth") as String
//        accessToken = env.MODRINTH_API_KEY.orNull()
//        minecraftVersions.add(stonecutter.current.version)
//        minecraftVersions.addAll(additionalVersions)
//    }
//    curseforge {
//        projectId = property("publish.curseforge") as String
//        accessToken = env.CURSEFORGE_API_KEY.orNull()
//        minecraftVersions.add(stonecutter.current.version)
//        minecraftVersions.addAll(additionalVersions)
//    }
//}

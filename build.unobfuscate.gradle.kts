plugins {
    id("net.neoforged.moddev")
    id("dev.kikugie.postprocess.jsonlang")
    id("me.modmuss50.mod-publish-plugin")
}

version = "${property("mod.version")}-${property("deps.minecraft")}-universal"
base.archivesName = property("mod.id") as String

jsonlang {
    languageDirectories = listOf("assets/${property("mod.id")}/lang")
    prettyPrint = true
}

repositories {
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
    maven("https://maven.terraformersmc.com/") { name = "ModMenu" }
    maven("https://maven.nucleoid.xyz/") { name = "Placeholder API" }
}

dependencies {
    compileOnly("net.fabricmc:fabric-loader:${property("deps.fabric-loader")}")
    compileOnly("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric-api")}")
    compileOnly("com.terraformersmc:modmenu:${property("deps.mod_menu")}")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

neoForge {
    version = property("deps.neoforge") as String
    validateAccessTransformers = true

    runs {
        register("client") {
            gameDirectory = file("run/")
            client()
        }
        register("server") {
            gameDirectory = file("run/")
            server()
        }
    }

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
    sourceSets["main"].resources.srcDir("src/main/generated")
}

tasks {
    processResources {
        dependsOn("stonecutterGenerate")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

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
        this["neoforge_version_range"] = project.property("deps.neoforge_version_range") as String
        this["minecraft_version_range"] = project.property("deps.minecraft_version_range") as String
        this["fabric_version_range"] = project.property("deps.fabric_version_range") as String
    }

    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) { expand(props) }
    filesMatching("*.mixins.json") { expand("java" to "JAVA_25") }
}
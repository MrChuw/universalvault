plugins {
    id("dev.kikugie.stonecutter")
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    id("dev.kikugie.loom-back-compat") version "0.4.2" apply false
    id("fabric-loom") version "1.18-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.147" apply false
    id("dev.kikugie.postprocess.jsonlang") version "2.1-beta.4" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.8.+" apply false
}

stonecutter active "26.3-neoforge"

stonecutter parameters {
    val loader = node.metadata.project.substringAfterLast('-')

    // "unobfuscate" ativa os dois loaders: o jar é universal
    constants["fabric"]      = (loader == "fabric"      || loader == "unobfuscate")
    constants["neoforge"]    = (loader == "neoforge"    || loader == "unobfuscate")
    constants["unobfuscate"] = (loader == "unobfuscate")

     filters.include("**/*.fsh", "**/*.vsh")

    swaps["mod_version"] = "\"${properties.get<String>("mod.version")}\";"
    swaps["minecraft"]   = "\"${node.metadata.version}\";"

    replacements {
        string(current.version >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}

stonecutter tasks {
    order("publishCurseforge")
    order("publishModrinth")
}

for (version in stonecutter.versions.map { it.version }.distinct()) {
    tasks.register("publish$version") {
        group = "publishing"
        dependsOn(stonecutter.tasks.named("publishMods") { metadata.version == version })
    }
}
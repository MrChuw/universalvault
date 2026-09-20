plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev") version "2.0.147" apply false
}

stonecutter active "26.1.2"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${properties.get<String>("mod.version")}\";"
    swaps["minecraft"]   = "\"${node.metadata.version}\";"
    constants["release"] = properties.get<String>("mod.id") != "template"

    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}
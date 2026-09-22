# Universal Vault
A NeoForge mod for Minecraft 26.2, inspired by Terraria's [DragonVault](https://steamcommunity.com/sharedfiles/filedetails/?id=2989967984). It adds
a shared, unlimited-capacity item vault: a global vault everyone shares, plus
one personal vault per player.

## Features

- **Global & personal vaults** — open with `/vault`, `/vault global`,
  `/vault personal`, or `/vault player <name>` (admin only), the **Vault
  Remote** item (shift-click for global, normal click for personal), or the
  `V` / `B` key bindings.
- **Vault I/O block** — a block that links to a vault and can be filtered
  (whitelist/blacklist) to control which items pass through. Shift+right-click
  cycles its target between global and the placing player's vault. Exposes an
  item-transfer capability so hoppers, minecarts, and other automation can
  insert/extract, matching its filter.
- **Vault screen** — searchable (`@mod`, `#tag`, or plain name), sortable
  (count, name, mod), virtualized grid that scales with window size.
- **Configurable limits** — max distinct item types, max amount per item
  type, max total items, virtual slot count for automation, and more (see
  `config/universal_vault-server.toml` / `-client.toml` after first run).

## Requirements

Fabric Loader `0.19.5+` on every supported Minecraft version.

| Minecraft | Fabric API         | NeoForge          |
|-----------|--------------------|--------------------|
| 1.21.10   | 0.135.0+1.21.10    | 21.10.64+          |
| 1.21.11   | 0.141.0+1.21.11    | 21.11.45+          |
| 26.1      | 0.144.3+26.1       | 26.1.0.19-beta+    |
| 26.1.1    | 0.145.4+26.1.1     | 26.1.1.15-beta+    |
| 26.1.2    | 0.148.0+26.1.2     | 26.1.2.109+        |
| 26.2      | 0.161.0+26.2       | 26.2.0.88+         |
| 26.3      | 0.160.5+26.3       | 26.3.0.0-beta+     |

## Building

```
./gradlew build
```

### Usage
- Use `"Set active project to ..."` Gradle tasks to update the Minecraft version
  available in `src/` classes.
- Use `buildAndCollect` Gradle task to store mod releases in `build/libs/`.
- Enable `mod-publish-plugin` in `stonecutter.gradle.kts` and `build.gradle.kts`
  and the corresponding code blocks to publish releases to Modrinth and Curseforge.
- Enable `maven-publish` in `build.gradle.kts` and the corresponding code block
  to publish releases to a personal maven repository.

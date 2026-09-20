# Universal Vault
A NeoForge mod for Minecraft 26.2, inspired by Terraria's [DragonVault]([https://steamcommunity.com/sharedfiles/filedetails/?id=2989967984](https://steamcommunity.com/sharedfiles/filedetails/?id=2989967984)). It adds
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

- Minecraft 26.2
- NeoForge 26.2.0.88+

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



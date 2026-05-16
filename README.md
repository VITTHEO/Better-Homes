Better Homes

Better Homes is a complete, configurable and professional homes plugin for Bukkit, Spigot and Paper servers.

Created by Vit Theo, Better Homes gives players a clean home system with commands, GUI menus, teleport safety, cooldowns, permissions, public/shared homes and a solid YAML storage backend.

What Better Homes offers

Better Homes is designed for servers that want more than a simple /home system. It gives players a modern and easy-to-use way to create, manage, open, rename, move, share and delete homes through commands and a clean inventory GUI.

The plugin is built to stay flexible, making it useful for survival, economy, factions, prison, roleplay and any other type of Minecraft server that needs a reliable home system.

Highlights
Complete homes system with named homes
Modern inventory GUI with pagination
Safe teleport checks
Teleport delay and cooldown
Cancel teleport on movement, damage or combat
Configurable home limits
Per-world home support
Public and shared homes
Admin commands for player home management
Fully configurable messages
Configurable GUI items, titles, sounds and effects
YAML storage by default
Clean architecture ready for future SQLite/MySQL support
Built for broad Bukkit/Spigot/Paper compatibility

Screenshots / In-game images

- homes player list
<img width="355" height="270" alt="Capture d&#39;écran 2026-05-11 103436" src="https://github.com/user-attachments/assets/eac83bc5-7afb-4ca3-840a-976cbf3bc48d" />


- home editor
<img width="342" height="270" alt="Capture d&#39;écran 2026-05-11 103548" src="https://github.com/user-attachments/assets/3450b823-50d4-4b58-a57b-e63e0326b5e1" />


- chat exemple
<img width="640" height="313" alt="Capture d&#39;écran 2026-05-11 103833" src="https://github.com/user-attachments/assets/46311d88-b405-4929-a6c7-ea963cd9ceb2" />



Player Commands
Code:
/sethome [name]         Create a home at your current location
/home [name]            Teleport to one of your homes
/home <player>:<name>   Teleport to a public/shared home
/delhome [name]         Delete one of your homes
/homes [page]           List homes and open the GUI

Admin Commands
Code:
/betterhomes reload                         Reload configuration and messages
/betterhomes info                           Display plugin information
/betterhomes sethomeplayer <player> <name>   Create a home for another player
/betterhomes delhomeplayer <player> <name>   Delete a player's home
/betterhomes listhomes <player>              List a player's homes
/betterhomes clearhomes <player>             Clear all homes of a player
/betterhomes givehome <player> <name>        Create a home for a player from your position
/betterhomes rename <old> <new>              Rename one of your homes
/betterhomes move <name>                     Move a home to your current position
/betterhomes share <home> <player>           Share a home with a player
/betterhomes unshare <home> <player>         Remove shared access
/betterhomes public <home> <on/off>          Toggle public access

Permissions
Code:
betterhomes.use                 Allows basic plugin usage
betterhomes.sethome             Allows /sethome
betterhomes.home                Allows /home
betterhomes.delhome             Allows /delhome
betterhomes.gui                 Allows GUI usage
betterhomes.bypass.cooldown     Bypasses teleport cooldown
betterhomes.bypass.limit        Bypasses home limits
betterhomes.admin               Grants admin permissions
betterhomes.reload              Allows /betterhomes reload
betterhomes.others              Allows managing other players' homes
betterhomes.rename              Allows renaming homes
betterhomes.list                Allows listing homes
betterhomes.teleport.instant    Bypasses teleport delay
betterhomes.teleport.safely     Allows safe teleport adjustment
betterhomes.world.<worldname>   Allows homes in a specific world
betterhomes.limit.<number>      Gives a custom home limit

GUI Features
Main homes menu
Page selector menu
Home edit menu
Delete confirmation menu
Buttons for teleport, move, rename, public toggle and delete
Configurable icons and item lore
Configurable rows, titles, sounds and filler items
Pagination for players with many homes

Teleport Safety
Configurable warmup delay
Configurable cooldown
Movement cancellation
Damage cancellation
Combat cancellation
Safe destination search
Block danger checks for lava, fire, cactus and similar hazards
World whitelist/blacklist support
Optional per-world permission checks

Storage

Better Homes uses YAML storage by default. Player homes are saved automatically and loaded on startup.

The internal storage layer is interface-based, making it easy to add SQLite or MySQL support later without rewriting the home system.

Configuration

The plugin generates:
config.yml
messages.yml
homes.yml

You can configure:
Home limits
Teleport delay
Teleport cooldown
Safe teleport behavior
GUI layout
GUI icons
Sounds
Effects
Messages
Allowed or blocked worlds
Shared/public home behavior
Command behavior

Compatibility

Better Homes is compiled against the Bukkit 1.8.8 API and avoids direct usage of modern-only API calls.

Material, sound, title and effect handling uses compatibility helpers and fallback names, which makes the plugin suitable for a wide range of Bukkit, Spigot and Paper versions.

Installation
Download BetterHomes.jar
Put it in your server's plugins folder
Restart your server
Edit plugins/BetterHomes/config.yml and messages.yml
Run /betterhomes reload or restart the server again

Why Better Homes?

Better Homes is built for server owners who want more than a basic /home command. It provides a complete home management experience with a professional structure, strong configuration options and a user-friendly GUI.

It is lightweight, dependency-free and designed to stay stable on active servers.

Support

If you need help, find a bug or want to suggest a feature, contact the author or open a discussion on the resource page.

Author

Vit Theo

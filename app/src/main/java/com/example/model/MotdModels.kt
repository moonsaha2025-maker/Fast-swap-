package com.example.model

enum class McColor(
    val code: Char,
    val displayName: String,
    val miniName: String,
    val rgb: Int
) {
    BLACK('0', "Black", "black", 0x000000),
    DARK_BLUE('1', "Dark Blue", "dark_blue", 0x0000AA),
    DARK_GREEN('2', "Dark Green", "dark_green", 0x00AA00),
    DARK_AQUA('3', "Dark Aqua", "dark_aqua", 0x00AAAA),
    DARK_RED('4', "Dark Red", "dark_red", 0xAA0000),
    DARK_PURPLE('5', "Dark Purple", "dark_purple", 0xAA00AA),
    GOLD('6', "Gold", "gold", 0xFFAA00),
    GRAY('7', "Gray", "gray", 0xAAAAAA),
    DARK_GRAY('8', "Dark Gray", "dark_gray", 0x555555),
    BLUE('9', "Blue", "blue", 0x5555FF),
    GREEN('a', "Green", "green", 0x55FF55),
    AQUA('b', "Aqua", "aqua", 0x55FFFF),
    RED('c', "Red", "red", 0xFF5555),
    LIGHT_PURPLE('d', "Light Purple", "light_purple", 0xFF55FF),
    YELLOW('e', "Yellow", "yellow", 0xFFFF55),
    WHITE('f', "White", "white", 0xFFFFFF);

    companion object {
        fun fromCode(ch: Char): McColor? = entries.find { it.code == ch.lowercaseChar() }
        fun fromMiniName(name: String): McColor? = entries.find { it.miniName == name.lowercase() }
    }
}

enum class McFormat(
    val code: Char,
    val label: String,
    val miniTag: String,
    val shortBadge: String
) {
    BOLD('l', "Bold", "bold", "B"),
    ITALIC('o', "Italic", "italic", "I"),
    UNDERLINE('n', "Underline", "underlined", "U"),
    STRIKETHROUGH('m', "Strike", "strikethrough", "S"),
    OBFUSCATED('k', "Magic", "obfuscated", "?!"),
    RESET('r', "Reset", "reset", "↺")
}

enum class ExportFormat(val label: String, val subtitle: String) {
    SERVER_PROPERTIES("server.properties", "Vanilla / Paper (\\u00A7)"),
    MINIMESSAGE("MiniMessage", "Paper / Velocity / Adventure"),
    LEGACY_AMPERSAND("Legacy (&)", "Spigot / Bukkit / MOTD plugins"),
    LEGACY_SECTION("Section (§)", "Direct § / BungeeCord config"),
    VANILLA_JSON("JSON", "Raw Minecraft text component")
}

enum class GradientOutputStyle(val label: String) {
    MINIMESSAGE("<gradient>"),
    SPIGOT_HEX("&#RRGGBB"),
    SECTION_HEX("§x§R§R...")
}

enum class EmojiCategory(val label: String) {
    ALL("All"),
    ARROWS("Arrows & Dividers"),
    COMBAT("RPG & PvP"),
    WORLD("World & Survival"),
    STATUS("Status & Icons"),
    BRACKETS("Brackets & Frames")
}

data class MotdSymbol(
    val symbol: String,
    val label: String,
    val category: EmojiCategory,
    val isWrapper: Boolean = false
)

data class GradientPreset(
    val id: String,
    val name: String,
    val colors: List<Int>,
    val isRainbow: Boolean = false
)

data class MotdPreset(
    val id: String,
    val name: String,
    val category: String,
    val serverName: String,
    val line1: String,
    val line2: String,
    val isCustom: Boolean = false
)

data class StyledSegment(
    val text: String,
    val colorRgb: Int = 0xAAAAAA,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underlined: Boolean = false,
    val strikethrough: Boolean = false,
    val obfuscated: Boolean = false
)

enum class StudioTab(val title: String) {
    EDITOR("Editor"),
    RGB("RGB Studio"),
    EMOJIS("Emojis"),
    SWAP_EXPORT("Swap & Export")
}

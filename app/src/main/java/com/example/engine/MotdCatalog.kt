package com.example.engine

import com.example.model.EmojiCategory
import com.example.model.GradientPreset
import com.example.model.MotdPreset
import com.example.model.MotdSymbol

object MotdCatalog {

    val symbols: List<MotdSymbol> = listOf(
        // Arrows & Dividers
        MotdSymbol("»", "Double Arrow Right", EmojiCategory.ARROWS),
        MotdSymbol("«", "Double Arrow Left", EmojiCategory.ARROWS),
        MotdSymbol("➤", "Pointer Arrow", EmojiCategory.ARROWS),
        MotdSymbol("➜", "Bold Arrow", EmojiCategory.ARROWS),
        MotdSymbol("▶", "Play Triangle", EmojiCategory.ARROWS),
        MotdSymbol("◀", "Left Triangle", EmojiCategory.ARROWS),
        MotdSymbol("★", "Filled Star", EmojiCategory.ARROWS),
        MotdSymbol("☆", "Outline Star", EmojiCategory.ARROWS),
        MotdSymbol("✦", "Four-Point Spark", EmojiCategory.ARROWS),
        MotdSymbol("✧", "Hollow Spark", EmojiCategory.ARROWS),
        MotdSymbol("❖", "Diamond Cluster", EmojiCategory.ARROWS),
        MotdSymbol("◈", "Faceted Gem", EmojiCategory.ARROWS),
        MotdSymbol("⬢", "Hexagon", EmojiCategory.ARROWS),
        MotdSymbol("•", "Bullet Dot", EmojiCategory.ARROWS),
        MotdSymbol("┃", "Thick Divider", EmojiCategory.ARROWS),
        MotdSymbol("│", "Thin Divider", EmojiCategory.ARROWS),
        MotdSymbol("║", "Double Bar", EmojiCategory.ARROWS),
        MotdSymbol("━", "Horizontal Rule", EmojiCategory.ARROWS),
        MotdSymbol("═", "Double Line", EmojiCategory.ARROWS),
        MotdSymbol("⚡", "Lightning Bolt", EmojiCategory.ARROWS),

        // RPG & PvP
        MotdSymbol("⚔", "Crossed Swords", EmojiCategory.COMBAT),
        MotdSymbol("🗡", "Dagger Blade", EmojiCategory.COMBAT),
        MotdSymbol("🛡", "Shield", EmojiCategory.COMBAT),
        MotdSymbol("🏹", "Bow & Arrow", EmojiCategory.COMBAT),
        MotdSymbol("🔱", "Trident", EmojiCategory.COMBAT),
        MotdSymbol("👑", "Royal Crown", EmojiCategory.COMBAT),
        MotdSymbol("💎", "Diamond Gem", EmojiCategory.COMBAT),
        MotdSymbol("🏆", "Trophy", EmojiCategory.COMBAT),
        MotdSymbol("🔥", "Flame", EmojiCategory.COMBAT),
        MotdSymbol("❄", "Frost", EmojiCategory.COMBAT),
        MotdSymbol("✨", "Sparkles", EmojiCategory.COMBAT),
        MotdSymbol("💥", "Explosion", EmojiCategory.COMBAT),
        MotdSymbol("☠", "Skull", EmojiCategory.COMBAT),
        MotdSymbol("🧪", "Potion", EmojiCategory.COMBAT),
        MotdSymbol("🔮", "Crystal Orb", EmojiCategory.COMBAT),
        MotdSymbol("🎖", "Medal", EmojiCategory.COMBAT),

        // World & Survival
        MotdSymbol("⛏", "Pickaxe", EmojiCategory.WORLD),
        MotdSymbol("🪓", "Axe", EmojiCategory.WORLD),
        MotdSymbol("🌲", "Pine Tree", EmojiCategory.WORLD),
        MotdSymbol("🍀", "Lucky Clover", EmojiCategory.WORLD),
        MotdSymbol("🍄", "Mushroom", EmojiCategory.WORLD),
        MotdSymbol("🧭", "Compass", EmojiCategory.WORLD),
        MotdSymbol("🗺", "World Map", EmojiCategory.WORLD),
        MotdSymbol("☀", "Sun", EmojiCategory.WORLD),
        MotdSymbol("🌙", "Crescent Moon", EmojiCategory.WORLD),
        MotdSymbol("⭐", "Gold Star", EmojiCategory.WORLD),
        MotdSymbol("☁", "Cloud", EmojiCategory.WORLD),
        MotdSymbol("🌊", "Ocean Wave", EmojiCategory.WORLD),
        MotdSymbol("🎃", "Pumpkin Event", EmojiCategory.WORLD),
        MotdSymbol("🎄", "Holiday Tree", EmojiCategory.WORLD),
        MotdSymbol("🎁", "Gift Crate", EmojiCategory.WORLD),
        MotdSymbol("🎉", "Celebration", EmojiCategory.WORLD),

        // Status & Server Icons
        MotdSymbol("✔", "Checkmark", EmojiCategory.STATUS),
        MotdSymbol("✖", "Cross Mark", EmojiCategory.STATUS),
        MotdSymbol("⚠", "Warning", EmojiCategory.STATUS),
        MotdSymbol("⚙", "Gear / Config", EmojiCategory.STATUS),
        MotdSymbol("🔒", "Locked / Whitelist", EmojiCategory.STATUS),
        MotdSymbol("🔓", "Open / Public", EmojiCategory.STATUS),
        MotdSymbol("🟢", "Online Dot", EmojiCategory.STATUS),
        MotdSymbol("🔴", "Offline Dot", EmojiCategory.STATUS),
        MotdSymbol("🟡", "Maintenance Dot", EmojiCategory.STATUS),
        MotdSymbol("📣", "Announcement", EmojiCategory.STATUS),
        MotdSymbol("🔔", "Alert Bell", EmojiCategory.STATUS),
        MotdSymbol("⏱", "Stopwatch / Wipe", EmojiCategory.STATUS),
        MotdSymbol("🌐", "Network Globe", EmojiCategory.STATUS),
        MotdSymbol("📡", "Server Signal", EmojiCategory.STATUS),
        MotdSymbol("🚀", "Launch Boost", EmojiCategory.STATUS),
        MotdSymbol("❤", "Heart", EmojiCategory.STATUS),

        // Brackets & Frames
        MotdSymbol("【】", "Heavy Brackets", EmojiCategory.BRACKETS, isWrapper = true),
        MotdSymbol("『』", "Corner Brackets", EmojiCategory.BRACKETS, isWrapper = true),
        MotdSymbol("「」", "Thin Corners", EmojiCategory.BRACKETS, isWrapper = true),
        MotdSymbol("《》", "Double Angle", EmojiCategory.BRACKETS, isWrapper = true),
        MotdSymbol("〔〕", "Tortoise Shell", EmojiCategory.BRACKETS, isWrapper = true),
        MotdSymbol("༺༻", "Ornate Swirl", EmojiCategory.BRACKETS, isWrapper = true),
        MotdSymbol("▬▬ι═══════ﺤ", "Sword Divider", EmojiCategory.BRACKETS),
        MotdSymbol("░▒▓", "Shade Fade", EmojiCategory.BRACKETS),
        MotdSymbol("▓▒░", "Reverse Fade", EmojiCategory.BRACKETS),
        MotdSymbol("✦ ─── ✦", "Spark Divider", EmojiCategory.BRACKETS)
    )

    val gradientPresets: List<GradientPreset> = listOf(
        GradientPreset("emerald_pulse", "Emerald Pulse", listOf(0x22C55E, 0x06B6D4)),
        GradientPreset("sunset_blaze", "Sunset Blaze", listOf(0xFF512F, 0xF09819)),
        GradientPreset("nether_flame", "Nether Flame", listOf(0xEF4444, 0xF59E0B, 0xFDE047)),
        GradientPreset("cyber_neon", "Cyber Neon", listOf(0x00F5D4, 0x3B82F6, 0xA855F7)),
        GradientPreset("royal_amethyst", "Amethyst", listOf(0xC084FC, 0xEC4899)),
        GradientPreset("ocean_depths", "Ocean Depths", listOf(0x38BDF8, 0x2563EB, 0x1E1B4B)),
        GradientPreset("sakura_bloom", "Sakura", listOf(0xFDA4AF, 0xF472B6, 0xFFF1F2)),
        GradientPreset("rainbow_prism", "Rainbow", listOf(0xFF5555, 0xFFFF55, 0x55FF55), isRainbow = true)
    )

    val defaultPresets: List<MotdPreset> = listOf(
        MotdPreset(
            id = "smp_season",
            name = "Emerald SMP Season 5",
            category = "Survival SMP",
            serverName = "EmeraldCraft Network",
            line1 = "      <bold><gradient:#22C55E:#06B6D4>✦ EMERALDCRAFT SMP ✦</gradient></bold> &8[&a1.21.4&8]",
            line2 = "  &7⛏ &fSeason 5 Live &8┃ &e⚡ &bCustom Enchants &8┃ &a✔ &7No Grief"
        ),
        MotdPreset(
            id = "skyblock_realm",
            name = "Skyblock Isles",
            category = "Skyblock",
            serverName = "AetherSky Network",
            line1 = "     <bold><gradient:#38BDF8:#A855F7>❖ AETHERSKY REALMS ❖</gradient></bold> &7» &eNEW",
            line2 = "   &b💎 &fMinions &8• &d🔮 &fBosses &8• &6🏆 &e$500 Island Top"
        ),
        MotdPreset(
            id = "pvp_arena",
            name = "Crimson Practice PvP",
            category = "PvP / BedWars",
            serverName = "CrimsonPvP",
            line1 = "      <bold><gradient:#EF4444:#F59E0B>⚔ CRIMSON PRACTICE ⚔</gradient></bold> &8[&cNA/EU&8]",
            line2 = "  &c🔥 &fCrystal &8┃ &6🗡 &fNethPot &8┃ &e👑 &aRanked Queue Open"
        ),
        MotdPreset(
            id = "maintenance_mode",
            name = "Maintenance Notice",
            category = "Utility",
            serverName = "Server Maintenance",
            line1 = "        &6⚠ &l<gradient:#F59E0B:#FDE047>MAINTENANCE IN PROGRESS</gradient> &6⚠",
            line2 = "     &7⚙ &fUpgrading to &b1.21.4 &8┃ &e⏱ &7Back in 15 mins!"
        ),
        MotdPreset(
            id = "cozy_vanilla",
            name = "Cozy Vanilla+",
            category = "Cozy",
            serverName = "WillowVale",
            line1 = "      &a🌲 <gradient:#86EFAC:#FDE68A>WillowVale Cozy SMP</gradient> &a🍀",
            line2 = "   &7🌙 &fProximity Voice &8• &d✨ &fSeasons &8• &a❤ &7Whitelisted"
        ),
        MotdPreset(
            id = "holiday_event",
            name = "Winter Festival Event",
            category = "Seasonal",
            serverName = "FrostPeak Network",
            line1 = "     <bold><gradient:#60A5FA:#E0F2FE:#F472B6>❄ FROSTPEAK WINTERFEST ❄</gradient></bold>",
            line2 = "   &b🎁 &f2x Crate Keys &8┃ &e🎄 &fSnowball Arena &8┃ &a🎉 &fJoin Now!"
        )
    )
}

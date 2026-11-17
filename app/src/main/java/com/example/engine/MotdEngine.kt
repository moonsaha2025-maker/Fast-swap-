package com.example.engine

import com.example.model.ExportFormat
import com.example.model.GradientOutputStyle
import com.example.model.McColor
import com.example.model.StyledSegment
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

object MotdEngine {

    private const val DEFAULT_COLOR = 0xAAAAAA
    private const val TARGET_MOTD_PIXEL_WIDTH = 268

    private data class StyleState(
        val colorRgb: Int = DEFAULT_COLOR,
        val bold: Boolean = false,
        val italic: Boolean = false,
        val underlined: Boolean = false,
        val strikethrough: Boolean = false,
        val obfuscated: Boolean = false
    )

    fun parseToSegments(rawLine: String): List<StyledSegment> {
        if (rawLine.isEmpty()) return emptyList()

        val result = mutableListOf<StyledSegment>()
        var state = StyleState()
        val stateStack = ArrayDeque<StyleState>()
        val buffer = StringBuilder()

        fun flushBuffer() {
            if (buffer.isNotEmpty()) {
                val text = buffer.toString()
                buffer.clear()
                val last = result.lastOrNull()
                if (last != null &&
                    last.colorRgb == state.colorRgb &&
                    last.bold == state.bold &&
                    last.italic == state.italic &&
                    last.underlined == state.underlined &&
                    last.strikethrough == state.strikethrough &&
                    last.obfuscated == state.obfuscated
                ) {
                    result[result.lastIndex] = last.copy(text = last.text + text)
                } else {
                    result.add(
                        StyledSegment(
                            text = text,
                            colorRgb = state.colorRgb,
                            bold = state.bold,
                            italic = state.italic,
                            underlined = state.underlined,
                            strikethrough = state.strikethrough,
                            obfuscated = state.obfuscated
                        )
                    )
                }
            }
        }

        var i = 0
        while (i < rawLine.length) {
            val ch = rawLine[i]

            // 1. Handle & or § legacy and hex codes
            if ((ch == '&' || ch == '§') && i + 1 < rawLine.length) {
                val next = rawLine[i + 1]

                // &#RRGGBB
                if (next == '#' && i + 7 < rawLine.length) {
                    val hexStr = rawLine.substring(i + 2, i + 8)
                    val parsed = hexStr.toIntOrNull(16)
                    if (parsed != null) {
                        flushBuffer()
                        state = state.copy(colorRgb = parsed)
                        i += 8
                        continue
                    }
                }

                // &x&R&R&G&G&B&B or §x§R§R§G§G§B§B
                if ((next == 'x' || next == 'X') && i + 13 < rawLine.length) {
                    val hexBuilder = StringBuilder()
                    var validBungee = true
                    for (step in 0 until 6) {
                        val prefixIdx = i + 2 + step * 2
                        val charIdx = prefixIdx + 1
                        if (rawLine[prefixIdx] != '&' && rawLine[prefixIdx] != '§') {
                            validBungee = false
                            break
                        }
                        hexBuilder.append(rawLine[charIdx])
                    }
                    if (validBungee) {
                        val parsed = hexBuilder.toString().toIntOrNull(16)
                        if (parsed != null) {
                            flushBuffer()
                            state = state.copy(colorRgb = parsed)
                            i += 14
                            continue
                        }
                    }
                }

                // Standard 0-9, a-f color
                val mcColor = McColor.fromCode(next)
                if (mcColor != null) {
                    flushBuffer()
                    // Legacy color resets formatting codes in Java Edition
                    state = StyleState(colorRgb = mcColor.rgb)
                    i += 2
                    continue
                }

                // Formatting codes
                when (next.lowercaseChar()) {
                    'l' -> {
                        flushBuffer()
                        state = state.copy(bold = true)
                        i += 2
                        continue
                    }
                    'o' -> {
                        flushBuffer()
                        state = state.copy(italic = true)
                        i += 2
                        continue
                    }
                    'n' -> {
                        flushBuffer()
                        state = state.copy(underlined = true)
                        i += 2
                        continue
                    }
                    'm' -> {
                        flushBuffer()
                        state = state.copy(strikethrough = true)
                        i += 2
                        continue
                    }
                    'k' -> {
                        flushBuffer()
                        state = state.copy(obfuscated = true)
                        i += 2
                        continue
                    }
                    'r' -> {
                        flushBuffer()
                        state = StyleState()
                        i += 2
                        continue
                    }
                }
            }

            // 2. Handle MiniMessage tags <...>
            if (ch == '<') {
                val closeIdx = rawLine.indexOf('>', startIndex = i + 1)
                if (closeIdx != -1) {
                    val tagContent = rawLine.substring(i + 1, closeIdx).trim()
                    val lower = tagContent.lowercase()

                    // Handle <gradient:#A:#B[:#C]>...</gradient>
                    if (lower.startsWith("gradient:")) {
                        val endTagIdx = findClosingTag(rawLine, closeIdx + 1, "gradient")
                        val stopStrings = tagContent.substringAfter("gradient:").split(":")
                        val stops = stopStrings.mapNotNull { parseHexOrNamedColor(it) }
                        if (stops.size >= 2) {
                            flushBuffer()
                            val innerEnd = if (endTagIdx != -1) endTagIdx else rawLine.length
                            val innerText = stripFormattingForGradient(rawLine.substring(closeIdx + 1, innerEnd))
                            val gradSegments = interpolateSegments(
                                text = innerText,
                                colors = stops,
                                baseBold = state.bold,
                                baseItalic = state.italic,
                                baseUnderlined = state.underlined,
                                baseStrikethrough = state.strikethrough,
                                baseObfuscated = state.obfuscated
                            )
                            result.addAll(gradSegments)
                            i = if (endTagIdx != -1) endTagIdx + "</gradient>".length else rawLine.length
                            continue
                        }
                    }

                    // Handle <rainbow>...</rainbow>
                    if (lower == "rainbow" || lower.startsWith("rainbow:")) {
                        val endTagIdx = findClosingTag(rawLine, closeIdx + 1, "rainbow")
                        flushBuffer()
                        val innerEnd = if (endTagIdx != -1) endTagIdx else rawLine.length
                        val innerText = stripFormattingForGradient(rawLine.substring(closeIdx + 1, innerEnd))
                        val rainbowSegments = interpolateRainbowSegments(
                            text = innerText,
                            baseBold = state.bold,
                            baseItalic = state.italic,
                            baseUnderlined = state.underlined,
                            baseStrikethrough = state.strikethrough,
                            baseObfuscated = state.obfuscated
                        )
                        result.addAll(rainbowSegments)
                        i = if (endTagIdx != -1) endTagIdx + "</rainbow>".length else rawLine.length
                        continue
                    }

                    // Single tags
                    val handled = applyMiniTag(lower, state, stateStack)
                    if (handled != null) {
                        flushBuffer()
                        state = handled
                        i = closeIdx + 1
                        continue
                    }
                }
            }

            buffer.append(ch)
            i++
        }

        flushBuffer()
        return result
    }

    private fun findClosingTag(input: String, startFrom: Int, tagName: String): Int {
        return input.indexOf("</$tagName>", startIndex = startFrom, ignoreCase = true)
    }

    private fun stripFormattingForGradient(inner: String): String {
        // Preserve plain visible characters (or simple nested <bold>/&l? Here we strip outer tag tokens so gradient renders cleanly)
        return inner.replace(Regex("</?(bold|b|italic|i|underlined|u|strikethrough|st|obfuscated|obf|reset)>", RegexOption.IGNORE_CASE), "")
    }

    private fun parseHexOrNamedColor(raw: String): Int? {
        val clean = raw.trim().removePrefix("#")
        if (clean.length == 6) {
            clean.toIntOrNull(16)?.let { return it }
        }
        return McColor.fromMiniName(raw.trim())?.rgb
    }

    private fun applyMiniTag(
        tag: String,
        current: StyleState,
        stack: ArrayDeque<StyleState>
    ): StyleState? {
        if (tag.startsWith("#") && tag.length == 7) {
            val rgb = tag.substring(1).toIntOrNull(16) ?: return null
            stack.addLast(current)
            return current.copy(colorRgb = rgb)
        }
        if (tag.startsWith("color:#") && tag.length == 13) {
            val rgb = tag.substring(7).toIntOrNull(16) ?: return null
            stack.addLast(current)
            return current.copy(colorRgb = rgb)
        }
        val named = McColor.fromMiniName(tag)
        if (named != null) {
            stack.addLast(current)
            return current.copy(colorRgb = named.rgb)
        }
        return when (tag) {
            "bold", "b" -> {
                stack.addLast(current)
                current.copy(bold = true)
            }
            "/bold", "/b" -> current.copy(bold = false)
            "italic", "i", "em" -> {
                stack.addLast(current)
                current.copy(italic = true)
            }
            "/italic", "/i", "/em" -> current.copy(italic = false)
            "underlined", "u" -> {
                stack.addLast(current)
                current.copy(underlined = true)
            }
            "/underlined", "/u" -> current.copy(underlined = false)
            "strikethrough", "st" -> {
                stack.addLast(current)
                current.copy(strikethrough = true)
            }
            "/strikethrough", "/st" -> current.copy(strikethrough = false)
            "obfuscated", "obf" -> {
                stack.addLast(current)
                current.copy(obfuscated = true)
            }
            "/obfuscated", "/obf" -> current.copy(obfuscated = false)
            "reset", "r" -> {
                stack.clear()
                StyleState()
            }
            else -> {
                if (tag.startsWith("/") && stack.isNotEmpty()) {
                    stack.removeLast()
                } else {
                    null
                }
            }
        }
    }

    fun generateGradientMarkup(
        text: String,
        colors: List<Int>,
        isRainbow: Boolean,
        bold: Boolean,
        italic: Boolean,
        underline: Boolean,
        strikethrough: Boolean,
        style: GradientOutputStyle
    ): String {
        if (text.isEmpty()) return ""
        val validColors = if (colors.size >= 2) colors else listOf(0x22C55E, 0x06B6D4)

        return when (style) {
            GradientOutputStyle.MINIMESSAGE -> {
                val core = if (isRainbow) {
                    "<rainbow>$text</rainbow>"
                } else {
                    val stops = validColors.joinToString(":") { formatHexHash(it) }
                    "<gradient:$stops>$text</gradient>"
                }
                buildString {
                    if (bold) append("<bold>")
                    if (italic) append("<italic>")
                    if (underline) append("<underlined>")
                    if (strikethrough) append("<strikethrough>")
                    append(core)
                    if (strikethrough) append("</strikethrough>")
                    if (underline) append("</underlined>")
                    if (italic) append("</italic>")
                    if (bold) append("</bold>")
                }
            }

            GradientOutputStyle.SPIGOT_HEX,
            GradientOutputStyle.SECTION_HEX -> {
                val segments = if (isRainbow) {
                    interpolateRainbowSegments(text, bold, italic, underline, strikethrough, false)
                } else {
                    interpolateSegments(text, validColors, bold, italic, underline, strikethrough, false)
                }
                val prefixChar = if (style == GradientOutputStyle.SPIGOT_HEX) '&' else '§'
                buildString {
                    for (seg in segments) {
                        if (seg.text.isBlank()) {
                            append(seg.text)
                        } else {
                            if (style == GradientOutputStyle.SPIGOT_HEX) {
                                append("&#${formatHexRaw(seg.colorRgb)}")
                            } else {
                                append(toSectionHex(seg.colorRgb))
                            }
                            if (seg.bold) append("${prefixChar}l")
                            if (seg.italic) append("${prefixChar}o")
                            if (seg.underlined) append("${prefixChar}n")
                            if (seg.strikethrough) append("${prefixChar}m")
                            append(seg.text)
                        }
                    }
                    append("${prefixChar}r")
                }
            }
        }
    }

    fun interpolateSegments(
        text: String,
        colors: List<Int>,
        baseBold: Boolean = false,
        baseItalic: Boolean = false,
        baseUnderlined: Boolean = false,
        baseStrikethrough: Boolean = false,
        baseObfuscated: Boolean = false
    ): List<StyledSegment> {
        if (text.isEmpty() || colors.isEmpty()) return emptyList()
        if (colors.size == 1) {
            return listOf(
                StyledSegment(text, colors[0], baseBold, baseItalic, baseUnderlined, baseStrikethrough, baseObfuscated)
            )
        }

        val codePoints = text.codePointList()
        val nonSpaceCount = codePoints.count { !Character.isWhitespace(it.codePointAt(0)) }
        if (nonSpaceCount <= 1) {
            return listOf(
                StyledSegment(text, colors.first(), baseBold, baseItalic, baseUnderlined, baseStrikethrough, baseObfuscated)
            )
        }

        val result = mutableListOf<StyledSegment>()
        var visibleIdx = 0
        for (cp in codePoints) {
            val isSpace = Character.isWhitespace(cp.codePointAt(0))
            val color = if (isSpace) {
                colors.first()
            } else {
                val t = visibleIdx.toFloat() / (nonSpaceCount - 1).coerceAtLeast(1)
                visibleIdx++
                interpolateColorList(colors, t)
            }
            result.add(
                StyledSegment(
                    text = cp,
                    colorRgb = color,
                    bold = baseBold,
                    italic = baseItalic,
                    underlined = baseUnderlined,
                    strikethrough = baseStrikethrough,
                    obfuscated = baseObfuscated
                )
            )
        }
        return result
    }

    fun interpolateRainbowSegments(
        text: String,
        baseBold: Boolean = false,
        baseItalic: Boolean = false,
        baseUnderlined: Boolean = false,
        baseStrikethrough: Boolean = false,
        baseObfuscated: Boolean = false
    ): List<StyledSegment> {
        val codePoints = text.codePointList()
        val nonSpaceCount = codePoints.count { !Character.isWhitespace(it.codePointAt(0)) }.coerceAtLeast(1)
        val result = mutableListOf<StyledSegment>()
        var visibleIdx = 0
        for (cp in codePoints) {
            val isSpace = Character.isWhitespace(cp.codePointAt(0))
            val color = if (isSpace) {
                0xFFFFFF
            } else {
                val hue = (visibleIdx.toFloat() / nonSpaceCount) * 360f
                visibleIdx++
                hsvToRgb(hue, 0.85f, 1.0f)
            }
            result.add(
                StyledSegment(
                    text = cp,
                    colorRgb = color,
                    bold = baseBold,
                    italic = baseItalic,
                    underlined = baseUnderlined,
                    strikethrough = baseStrikethrough,
                    obfuscated = baseObfuscated
                )
            )
        }
        return result
    }

    fun interpolateColorList(colors: List<Int>, t: Float): Int {
        val clamped = t.coerceIn(0f, 1f)
        if (colors.size == 1) return colors[0]
        val scaled = clamped * (colors.size - 1)
        val idx = scaled.toInt().coerceAtMost(colors.size - 2)
        val localT = scaled - idx
        return lerpRgb(colors[idx], colors[idx + 1], localT)
    }

    fun lerpRgb(c1: Int, c2: Int, t: Float): Int {
        val r1 = (c1 shr 16) and 0xFF
        val g1 = (c1 shr 8) and 0xFF
        val b1 = c1 and 0xFF

        val r2 = (c2 shr 16) and 0xFF
        val g2 = (c2 shr 8) and 0xFF
        val b2 = c2 and 0xFF

        val r = (r1 + (r2 - r1) * t).roundToInt().coerceIn(0, 255)
        val g = (g1 + (g2 - g1) * t).roundToInt().coerceIn(0, 255)
        val b = (b1 + (b2 - b1) * t).roundToInt().coerceIn(0, 255)
        return (r shl 16) or (g shl 8) or b
    }

    fun hsvToRgb(hue: Float, sat: Float, value: Float): Int {
        val h = ((hue % 360f) + 360f) % 360f
        val c = value * sat
        val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
        val m = value - c
        val (rf, gf, bf) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        val r = ((rf + m) * 255f).roundToInt().coerceIn(0, 255)
        val g = ((gf + m) * 255f).roundToInt().coerceIn(0, 255)
        val b = ((bf + m) * 255f).roundToInt().coerceIn(0, 255)
        return (r shl 16) or (g shl 8) or b
    }

    /**
     * Computes approximate Minecraft server-list pixel width of a formatted line
     * and prepends spaces to center it cleanly.
     */
    fun centerLine(rawLine: String): String {
        val trimmed = rawLine.trimStart(' ')
        val pxWidth = computeMcPixelWidth(trimmed)
        if (pxWidth >= TARGET_MOTD_PIXEL_WIDTH || pxWidth == 0) return trimmed
        val halvedGap = (TARGET_MOTD_PIXEL_WIDTH - pxWidth) / 2
        val spaceWidth = 4 // Minecraft space glyph is 3px + 1px spacing
        val spaces = (halvedGap / spaceWidth).coerceAtLeast(0)
        return " ".repeat(spaces) + trimmed
    }

    fun computeMcPixelWidth(rawLine: String): Int {
        val segments = parseToSegments(rawLine)
        var total = 0
        for (seg in segments) {
            var idx = 0
            while (idx < seg.text.length) {
                val cp = seg.text.codePointAt(idx)
                val charCount = Character.charCount(cp)
                total += mcCharWidth(cp, seg.bold)
                idx += charCount
            }
        }
        return total
    }

    private fun mcCharWidth(cp: Int, bold: Boolean): Int {
        val base = when (cp) {
            ' '.code -> 4
            'i'.code, '.'.code, ','.code, ':'.code, ';'.code, '!'.code, '|'.code, '\''.code -> 2
            'l'.code, '`'.code -> 3
            'I'.code, 't'.code, '['.code, ']'.code, '*'.code, '"'.code -> 4
            'f'.code, 'k'.code, '<'.code, '>'.code, '('.code, ')'.code, '{'.code, '}'.code -> 5
            in 0x20..0x7E -> 6
            else -> 9 // Emojis & wide Unicode symbols
        }
        return if (bold && cp != ' '.code) base + 1 else base
    }

    fun stripAllFormatting(rawLine: String): String {
        return parseToSegments(rawLine).joinToString("") { it.text }
    }

    fun exportMotd(line1: String, line2: String, format: ExportFormat): String {
        val segs1 = parseToSegments(line1)
        val segs2 = parseToSegments(line2)

        return when (format) {
            ExportFormat.SERVER_PROPERTIES -> {
                val l1 = segmentsToServerProperties(segs1)
                val l2 = segmentsToServerProperties(segs2)
                if (line2.isEmpty()) "motd=$l1" else "motd=$l1\\n$l2"
            }

            ExportFormat.MINIMESSAGE -> {
                val l1 = segmentsToMiniMessage(segs1)
                val l2 = segmentsToMiniMessage(segs2)
                if (line2.isEmpty()) l1 else "$l1\n$l2"
            }

            ExportFormat.LEGACY_AMPERSAND -> {
                val l1 = segmentsToLegacy(segs1, '&', useHex = true)
                val l2 = segmentsToLegacy(segs2, '&', useHex = true)
                if (line2.isEmpty()) l1 else "$l1\n$l2"
            }

            ExportFormat.LEGACY_SECTION -> {
                val l1 = segmentsToLegacy(segs1, '§', useHex = true)
                val l2 = segmentsToLegacy(segs2, '§', useHex = true)
                if (line2.isEmpty()) l1 else "$l1\n$l2"
            }

            ExportFormat.VANILLA_JSON -> {
                val root = JSONObject()
                root.put("text", "")
                val extra = JSONArray()
                segs1.forEach { extra.put(segmentToJson(it)) }
                if (line2.isNotEmpty()) {
                    extra.put(JSONObject().put("text", "\n"))
                    segs2.forEach { extra.put(segmentToJson(it)) }
                }
                root.put("extra", extra)
                root.toString(2)
            }
        }
    }

    private fun segmentsToServerProperties(segments: List<StyledSegment>): String {
        val legacy = segmentsToLegacy(segments, '§', useHex = true)
        return buildString {
            for (ch in legacy) {
                when {
                    ch == '§' -> append("\\u00A7")
                    ch.code in 0x20..0x7E -> append(ch)
                    else -> append("\\u%04X".format(ch.code))
                }
            }
        }
    }

    private fun segmentsToLegacy(
        segments: List<StyledSegment>,
        prefix: Char,
        useHex: Boolean
    ): String {
        if (segments.isEmpty()) return ""
        return buildString {
            var prevColor: Int? = null
            var prevBold = false
            var prevItalic = false
            var prevUnderline = false
            var prevStrike = false
            var prevObf = false

            for (seg in segments) {
                if (seg.text.isEmpty()) continue
                val needsReset = (prevBold && !seg.bold) ||
                    (prevItalic && !seg.italic) ||
                    (prevUnderline && !seg.underlined) ||
                    (prevStrike && !seg.strikethrough) ||
                    (prevObf && !seg.obfuscated)

                val colorChanged = prevColor != seg.colorRgb || needsReset
                if (colorChanged) {
                    val nearestLegacy = McColor.entries.find { it.rgb == seg.colorRgb }
                    if (nearestLegacy != null) {
                        append(prefix).append(nearestLegacy.code)
                    } else if (useHex) {
                        if (prefix == '&') {
                            append("&#${formatHexRaw(seg.colorRgb)}")
                        } else {
                            append(toSectionHex(seg.colorRgb))
                        }
                    } else {
                        append(prefix).append(nearestLegacyColor(seg.colorRgb).code)
                    }
                    prevColor = seg.colorRgb
                    prevBold = false
                    prevItalic = false
                    prevUnderline = false
                    prevStrike = false
                    prevObf = false
                }

                if (seg.bold && !prevBold) append("${prefix}l")
                if (seg.italic && !prevItalic) append("${prefix}o")
                if (seg.underlined && !prevUnderline) append("${prefix}n")
                if (seg.strikethrough && !prevStrike) append("${prefix}m")
                if (seg.obfuscated && !prevObf) append("${prefix}k")

                prevBold = seg.bold
                prevItalic = seg.italic
                prevUnderline = seg.underlined
                prevStrike = seg.strikethrough
                prevObf = seg.obfuscated

                append(seg.text)
            }
        }
    }

    private fun segmentsToMiniMessage(segments: List<StyledSegment>): String {
        if (segments.isEmpty()) return ""
        return buildString {
            for (seg in segments) {
                if (seg.text.isEmpty()) continue
                val named = McColor.entries.find { it.rgb == seg.colorRgb }
                val colorTag = named?.miniName ?: formatHexHash(seg.colorRgb)
                append("<$colorTag>")
                if (seg.bold) append("<b>")
                if (seg.italic) append("<i>")
                if (seg.underlined) append("<u>")
                if (seg.strikethrough) append("<st>")
                if (seg.obfuscated) append("<obf>")
                append(seg.text)
                if (seg.obfuscated) append("</obf>")
                if (seg.strikethrough) append("</st>")
                if (seg.underlined) append("</u>")
                if (seg.italic) append("</i>")
                if (seg.bold) append("</b>")
            }
        }
    }

    private fun segmentToJson(seg: StyledSegment): JSONObject {
        val obj = JSONObject()
        obj.put("text", seg.text)
        val named = McColor.entries.find { it.rgb == seg.colorRgb }
        obj.put("color", named?.miniName ?: formatHexHash(seg.colorRgb))
        if (seg.bold) obj.put("bold", true)
        if (seg.italic) obj.put("italic", true)
        if (seg.underlined) obj.put("underlined", true)
        if (seg.strikethrough) obj.put("strikethrough", true)
        if (seg.obfuscated) obj.put("obfuscated", true)
        return obj
    }

    private fun nearestLegacyColor(rgb: Int): McColor {
        val r = (rgb shr 16) and 0xFF
        val g = (rgb shr 8) and 0xFF
        val b = rgb and 0xFF
        return McColor.entries.minBy { c ->
            val cr = (c.rgb shr 16) and 0xFF
            val cg = (c.rgb shr 8) and 0xFF
            val cb = c.rgb and 0xFF
            (r - cr) * (r - cr) + (g - cg) * (g - cg) + (b - cb) * (b - cb)
        }
    }

    fun formatHexHash(rgb: Int): String = "#%06X".format(rgb and 0xFFFFFF)

    fun formatHexRaw(rgb: Int): String = "%06X".format(rgb and 0xFFFFFF)

    private fun toSectionHex(rgb: Int): String {
        val hex = formatHexRaw(rgb)
        return buildString {
            append("§x")
            for (c in hex) {
                append('§').append(c)
            }
        }
    }

    private fun String.codePointList(): List<String> {
        val list = mutableListOf<String>()
        var i = 0
        while (i < length) {
            val cp = codePointAt(i)
            val count = Character.charCount(cp)
            list.add(substring(i, i + count))
            i += count
        }
        return list
    }
}

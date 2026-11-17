package com.example

import com.example.engine.MotdEngine
import com.example.model.ExportFormat
import com.example.model.GradientOutputStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotdEngineTest {

    @Test
    fun `parse legacy color and bold formatting`() {
        val segments = MotdEngine.parseToSegments("&a&lEmerald &cPvP")
        assertEquals(2, segments.size)
        assertEquals("Emerald ", segments[0].text)
        assertEquals(0x55FF55, segments[0].colorRgb)
        assertTrue(segments[0].bold)
        assertEquals("PvP", segments[1].text)
        assertEquals(0xFF5555, segments[1].colorRgb)
    }

    @Test
    fun `parse minimessage gradient produces interpolated colors`() {
        val segments = MotdEngine.parseToSegments("<gradient:#FF0000:#0000FF>ABC</gradient>")
        assertEquals(3, segments.size)
        assertEquals(0xFF0000, segments[0].colorRgb)
        assertEquals(0x800080, segments[1].colorRgb)
        assertEquals(0x0000FF, segments[2].colorRgb)
    }

    @Test
    fun `generate gradient markup in spigot hex format`() {
        val out = MotdEngine.generateGradientMarkup(
            text = "MC",
            colors = listOf(0xFF0000, 0x00FF00),
            isRainbow = false,
            bold = true,
            italic = false,
            underline = false,
            strikethrough = false,
            style = GradientOutputStyle.SPIGOT_HEX
        )
        assertTrue(out.contains("&#FF0000&lM"))
        assertTrue(out.contains("&#00FF00&lC"))
    }

    @Test
    fun `export server properties escapes section sign and newline`() {
        val out = MotdEngine.exportMotd("&aLine 1", "&bLine 2", ExportFormat.SERVER_PROPERTIES)
        assertEquals("motd=\\u00A7aLine 1\\n\\u00A7bLine 2", out)
    }

    @Test
    fun `export legacy section formats standard minecraft section color codes`() {
        val out = MotdEngine.exportMotd("&a&lEmerald", "<#55FFFF>Sky", ExportFormat.LEGACY_SECTION)
        assertEquals("§a§lEmerald\n§bSky", out)
    }

    @Test
    fun `centerLine prepends leading spaces for short lines`() {
        val centered = MotdEngine.centerLine("&aHi")
        assertTrue(centered.startsWith(" "))
        assertTrue(centered.endsWith("&aHi"))
    }
}

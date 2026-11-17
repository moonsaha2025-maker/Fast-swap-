package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.MotdEngine
import com.example.model.StyledSegment
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.McPingGreen
import com.example.ui.theme.McServerCardBg
import com.example.ui.theme.McServerCardBorder
import kotlinx.coroutines.delay

private const val OBFUSCATE_GLYPHS = "!@#\$%&*?+=~^ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

@Composable
fun McFormattedText(
    rawLine: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 13.sp,
    lineHeight: TextUnit = 18.sp,
    placeholder: String = ""
) {
    val segments = remember(rawLine) { MotdEngine.parseToSegments(rawLine) }
    val hasObfuscated = remember(segments) { segments.any { it.obfuscated } }
    var glitchTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(hasObfuscated) {
        if (hasObfuscated) {
            while (true) {
                delay(70L)
                glitchTick++
            }
        }
    }

    if (segments.isEmpty()) {
        Text(
            text = placeholder,
            style = TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontSize = fontSize,
                lineHeight = lineHeight,
                color = Color(0xFF64748B)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
        return
    }

    val fgAnnotated = buildAnnotatedString {
        segments.forEachIndexed { segIdx, seg ->
            val displayText = if (seg.obfuscated) {
                obfuscateString(seg.text, glitchTick + segIdx * 13)
            } else {
                seg.text
            }
            val decorations = buildList {
                if (seg.underlined) add(TextDecoration.Underline)
                if (seg.strikethrough) add(TextDecoration.LineThrough)
            }
            val decoration = when (decorations.size) {
                0 -> TextDecoration.None
                1 -> decorations[0]
                else -> TextDecoration.combine(decorations)
            }
            withStyle(
                SpanStyle(
                    color = Color(0xFF000000.toInt() or (seg.colorRgb and 0xFFFFFF)),
                    fontWeight = if (seg.bold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (seg.italic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = decoration
                )
            ) {
                append(displayText)
            }
        }
    }

    val shadowAnnotated = buildAnnotatedString {
        segments.forEachIndexed { segIdx, seg ->
            val displayText = if (seg.obfuscated) {
                obfuscateString(seg.text, glitchTick + segIdx * 13)
            } else {
                seg.text
            }
            val baseColor = Color(0xFF000000.toInt() or (seg.colorRgb and 0xFFFFFF))
            val shadowColor = Color(
                red = baseColor.red * 0.25f,
                green = baseColor.green * 0.25f,
                blue = baseColor.blue * 0.25f,
                alpha = 1f
            )
            withStyle(
                SpanStyle(
                    color = shadowColor,
                    fontWeight = if (seg.bold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (seg.italic) FontStyle.Italic else FontStyle.Normal
                )
            ) {
                append(displayText)
            }
        }
    }

    val baseStyle = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontSize = fontSize,
        lineHeight = lineHeight
    )

    Box(modifier = modifier) {
        Text(
            text = shadowAnnotated,
            style = baseStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(x = 1.dp, y = 1.dp)
        )
        Text(
            text = fgAnnotated,
            style = baseStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun obfuscateString(input: String, seed: Int): String {
    return buildString(input.length) {
        input.forEachIndexed { idx, ch ->
            if (ch.isWhitespace()) {
                append(ch)
            } else {
                val pick = kotlin.math.abs((seed * 31 + idx * 17)) % OBFUSCATE_GLYPHS.length
                append(OBFUSCATE_GLYPHS[pick])
            }
        }
    }
}

@Composable
fun MinecraftPreviewCard(
    serverName: String,
    playerCount: String,
    line1: String,
    line2: String,
    customIconBitmap: Bitmap?,
    activeLine: Int,
    onUpdateServerMeta: (String, String) -> Unit,
    onPickIconClick: () -> Unit,
    onResetIconClick: () -> Unit,
    onSwapLines: () -> Unit,
    onCenterActiveLine: () -> Unit,
    onQuickCopy: () -> Unit,
    onQuickSaveSlot: () -> Unit,
    onSelectLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditMetaDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = McServerCardBg,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                    McServerCardBorder,
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
                )
            )
        ),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top bar: label + edit server name/players
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(McPingGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE MINECRAFT SERVER LIST",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (customIconBitmap != null) {
                        IconButton(
                            onClick = onResetIconClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Reset server icon",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    TextButton(
                        onClick = { showEditMetaDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("edit_server_meta_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Server Info",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Authentic MC Multiplayer Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0E12), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF272B35), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 64dp Server Icon (tap to pick custom server-icon.png)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .clickable(onClick = onPickIconClick)
                        .testTag("server_icon_picker_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (customIconBitmap != null) {
                        Image(
                            bitmap = customIconBitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.cd_server_icon),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_server_default),
                            contentDescription = stringResource(R.string.cd_server_icon),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Server Name + Player Count + Ping
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = serverName.ifBlank { "Minecraft Server" },
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = playerCount.ifBlank { "0/100" },
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFAAAAAA)
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        PingSignalBars()
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Line 1 preview (tap to focus Line 1)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (activeLine == 1) Color.White.copy(alpha = 0.05f)
                                else Color.Transparent
                            )
                            .clickable { onSelectLine(1) }
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        McFormattedText(
                            rawLine = line1,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            placeholder = "Line 1 MOTD..."
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Line 2 preview (tap to focus Line 2)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (activeLine == 2) Color.White.copy(alpha = 0.05f)
                                else Color.Transparent
                            )
                            .clickable { onSelectLine(2) }
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        McFormattedText(
                            rawLine = line2,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            placeholder = "Line 2 MOTD..."
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fast-to-Swap Action Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onSwapLines,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        contentColor = Color(0xFF4ADE80)
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("swap_lines_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwapVert,
                        contentDescription = stringResource(R.string.cd_swap_lines),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.action_swap_lines),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                OutlinedButton(
                    onClick = onCenterActiveLine,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("center_line_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CenterFocusStrong,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Center L$activeLine",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                OutlinedButton(
                    onClick = onQuickSaveSlot,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFBBF24)),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("quick_save_slot_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Save Slot",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var recentlyCopied by remember { mutableStateOf(false) }
            LaunchedEffect(recentlyCopied) {
                if (recentlyCopied) {
                    delay(1600L)
                    recentlyCopied = false
                }
            }

            FilledTonalButton(
                onClick = {
                    onQuickCopy()
                    recentlyCopied = true
                },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (recentlyCopied) {
                        Color(0xFF14532D)
                    } else {
                        Color(0xFF0E3A4A)
                    },
                    contentColor = if (recentlyCopied) {
                        Color(0xFF86EFAC)
                    } else {
                        Color(0xFF67E8F9)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("copy_to_clipboard_button")
            ) {
                Icon(
                    imageVector = if (recentlyCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                    contentDescription = stringResource(R.string.cd_copy),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (recentlyCopied) {
                        "Copied with § Codes!"
                    } else {
                        stringResource(R.string.action_copy_to_clipboard)
                    },
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.35f)
                ) {
                    Text(
                        text = "§",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (recentlyCopied) Color(0xFF86EFAC) else Color(0xFF67E8F9)
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }

    if (showEditMetaDialog) {
        var draftName by remember(serverName) { mutableStateOf(serverName) }
        var draftPlayers by remember(playerCount) { mutableStateOf(playerCount) }
        AlertDialog(
            onDismissRequest = { showEditMetaDialog = false },
            title = { Text("Server Preview Info") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        label = { Text("Server Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draftPlayers,
                        onValueChange = { draftPlayers = it },
                        label = { Text("Player Count (e.g. 148/500)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateServerMeta(draftName, draftPlayers)
                        showEditMetaDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditMetaDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PingSignalBars(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.size(width = 16.dp, height = 12.dp)
    ) {
        val barWidth = size.width / 7f
        val heights = listOf(0.28f, 0.45f, 0.62f, 0.80f, 1.0f)
        heights.forEachIndexed { idx, frac ->
            val h = size.height * frac
            val x = idx * (barWidth * 1.45f)
            drawRect(
                color = Color(0xFF00AA00),
                topLeft = Offset(x + 1f, size.height - h + 1f),
                size = Size(barWidth, h)
            )
            drawRect(
                color = McPingGreen,
                topLeft = Offset(x, size.height - h),
                size = Size(barWidth, h)
            )
        }
    }
}

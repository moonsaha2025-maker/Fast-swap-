package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.MotdEngine
import com.example.model.McColor
import com.example.model.McFormat
import com.example.ui.theme.JetBrainsMonoFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorTab(
    line1: TextFieldValue,
    line2: TextFieldValue,
    activeLine: Int,
    useAmpersand: Boolean,
    canUndo: Boolean,
    recentEmojis: List<String>,
    onLine1Change: (TextFieldValue) -> Unit,
    onLine2Change: (TextFieldValue) -> Unit,
    onSelectActiveLine: (Int) -> Unit,
    onTogglePrefixMode: () -> Unit,
    onUndo: () -> Unit,
    onClearLine: (Int) -> Unit,
    onInsertColor: (McColor) -> Unit,
    onInsertFormat: (McFormat) -> Unit,
    onInsertEmoji: (String) -> Unit,
    onOpenRgbStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefixChar = if (useAmpersand) "&" else "§"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Controls header: Active Line + Prefix mode + Undo
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = activeLine == 1,
                    onClick = { onSelectActiveLine(1) },
                    label = { Text("Line 1") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("select_line1_chip")
                )
                FilterChip(
                    selected = activeLine == 2,
                    onClick = { onSelectActiveLine(2) },
                    label = { Text("Line 2") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("select_line2_chip")
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onTogglePrefixMode,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("toggle_prefix_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (useAmpersand) "Code: &" else "Code: §",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Undo,
                        contentDescription = stringResource(R.string.cd_undo)
                    )
                }
            }
        }

        // Line 1 & Line 2 Inputs
        MotdLineInputField(
            label = "MOTD Line 1",
            value = line1,
            isFocusedLine = activeLine == 1,
            onValueChange = onLine1Change,
            onFocus = { onSelectActiveLine(1) },
            onClear = { onClearLine(1) },
            testTag = "motd_line1_input"
        )

        MotdLineInputField(
            label = "MOTD Line 2",
            value = line2,
            isFocusedLine = activeLine == 2,
            onValueChange = onLine2Change,
            onFocus = { onSelectActiveLine(2) },
            onClear = { onClearLine(2) },
            testTag = "motd_line2_input"
        )

        // Quick Emoji Strip right in Editor
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Emojis (tap to insert into Line $activeLine)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentEmojis.forEach { emoji ->
                    Surface(
                        onClick = { onInsertEmoji(emoji) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        // Formatting Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Text Formatting ($prefixChar)",
                    style = MaterialTheme.typography.titleSmall
                )
                Surface(
                    onClick = onOpenRgbStudio,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RGB Gradient",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                McFormat.entries.forEach { fmt ->
                    Surface(
                        onClick = { onInsertFormat(fmt) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("format_${fmt.name.lowercase()}_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = fmt.shortBadge,
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 15.sp,
                                    fontWeight = if (fmt == McFormat.BOLD) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    fontStyle = if (fmt == McFormat.ITALIC) FontStyle.Italic else FontStyle.Normal,
                                    textDecoration = when (fmt) {
                                        McFormat.UNDERLINE -> TextDecoration.Underline
                                        McFormat.STRIKETHROUGH -> TextDecoration.LineThrough
                                        else -> TextDecoration.None
                                    },
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "${fmt.label} ($prefixChar${fmt.code})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Official 16 Minecraft Color Palette
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Minecraft Color Palette",
                style = MaterialTheme.typography.titleSmall
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 4
            ) {
                McColor.entries.forEach { color ->
                    val swatchColor = Color(0xFF000000.toInt() or (color.rgb and 0xFFFFFF))
                    Surface(
                        onClick = { onInsertColor(color) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("mc_color_${color.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(swatchColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$prefixChar${color.code}",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MotdLineInputField(
    label: String,
    value: TextFieldValue,
    isFocusedLine: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onFocus: () -> Unit,
    onClear: () -> Unit,
    testTag: String
) {
    val pixelWidth = remember(value.text) { MotdEngine.computeMcPixelWidth(value.text) }
    val visibleCount = remember(value.text) { MotdEngine.stripAllFormatting(value.text).length }
    val overLimit = pixelWidth > 272

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isFocusedLine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$visibleCount chars • ${pixelWidth}/270 px",
                style = MaterialTheme.typography.labelSmall,
                color = if (overLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp
            ),
            placeholder = {
                Text(
                    text = "Type text, &a colors, <gradient:#...> or emojis...",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            trailingIcon = {
                if (value.text.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear $label",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isFocusedLine) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) onFocus() }
                .testTag(testTag)
        )
    }
}

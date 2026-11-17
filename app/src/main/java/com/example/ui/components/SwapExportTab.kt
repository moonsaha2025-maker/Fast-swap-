package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.MotdCatalog
import com.example.engine.MotdEngine
import com.example.model.ExportFormat
import com.example.model.MotdPreset
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.McServerCardBg

@Composable
fun SwapExportTab(
    line1: String,
    line2: String,
    customPresets: List<MotdPreset>,
    selectedExportFormat: ExportFormat,
    onSwapPreset: (MotdPreset) -> Unit,
    onOpenSaveSlotDialog: () -> Unit,
    onDeleteCustomPreset: (String) -> Unit,
    onSelectExportFormat: (ExportFormat) -> Unit,
    onCopyOutput: (String) -> Unit,
    onShareOutput: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val exportedCode = remember(line1, line2, selectedExportFormat) {
        MotdEngine.exportMotd(line1, line2, selectedExportFormat)
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Export Section
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Export MOTD Code",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = selectedExportFormat.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportFormat.entries.forEach { fmt ->
                            FilterChip(
                                selected = selectedExportFormat == fmt,
                                onClick = { onSelectExportFormat(fmt) },
                                label = { Text(fmt.label) }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(McServerCardBg)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = exportedCode,
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.testTag("export_code_preview")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onCopyOutput(exportedCode) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_copy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = stringResource(R.string.cd_copy),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Output")
                        }

                        OutlinedButton(
                            onClick = { onShareOutput(exportedCode) },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("export_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = stringResource(R.string.cd_share),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }
                    }
                }
            }
        }

        // Fast-to-Swap Vault Header + Save Current Slot CTA
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Fast-to-Swap Vault",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "1-tap swap between saved slots & pro presets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onOpenSaveSlotDialog,
                    modifier = Modifier.testTag("save_new_slot_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Save")
                }
            }
        }

        if (customPresets.isNotEmpty()) {
            item {
                Text(
                    text = "Your Saved Slots",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(customPresets, key = { it.id }) { preset ->
                PresetSwapCard(
                    preset = preset,
                    onSwap = { onSwapPreset(preset) },
                    onDelete = { onDeleteCustomPreset(preset.id) }
                )
            }
        }

        item {
            Text(
                text = "Pro Server Templates",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        items(MotdCatalog.defaultPresets, key = { it.id }) { preset ->
            PresetSwapCard(
                preset = preset,
                onSwap = { onSwapPreset(preset) },
                onDelete = null
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun PresetSwapCard(
    preset: MotdPreset,
    onSwap: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${preset.category} • ${preset.serverName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete saved slot",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    FilledTonalButton(
                        onClick = onSwap,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("swap_preset_${preset.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Swap")
                    }
                }
            }

            // Mini Live Preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(McServerCardBg)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                McFormattedText(rawLine = preset.line1, fontSize = 11.sp, lineHeight = 15.sp)
                McFormattedText(rawLine = preset.line2, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

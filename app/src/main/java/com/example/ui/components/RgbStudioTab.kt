package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MotdCatalog
import com.example.engine.MotdEngine
import com.example.model.GradientOutputStyle
import com.example.model.GradientPreset
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.McServerCardBg

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RgbStudioTab(
    gradientText: String,
    gradientStops: List<Int>,
    selectedStopIndex: Int,
    isThreeStop: Boolean,
    isRainbow: Boolean,
    bold: Boolean,
    italic: Boolean,
    underline: Boolean,
    strike: Boolean,
    outputStyle: GradientOutputStyle,
    generatedMarkup: String,
    activeLine: Int,
    onTextChange: (String) -> Unit,
    onToggleThreeStop: (Boolean) -> Unit,
    onSetRainbow: (Boolean) -> Unit,
    onSelectStop: (Int) -> Unit,
    onUpdateStopColor: (Int) -> Unit,
    onApplyPreset: (GradientPreset) -> Unit,
    onToggleStyle: (bold: Boolean?, italic: Boolean?, underline: Boolean?, strike: Boolean?) -> Unit,
    onSelectOutputStyle: (GradientOutputStyle) -> Unit,
    onInsertIntoLine: (line: Int, replace: Boolean) -> Unit,
    onCopySnippet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeStopColor = gradientStops.getOrElse(selectedStopIndex) { 0x22C55E }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Live Gradient Preview Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = McServerCardBg,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val stripBrush = if (isRainbow) {
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF5555),
                            Color(0xFFFFFF55),
                            Color(0xFF55FF55),
                            Color(0xFF55FFFF),
                            Color(0xFF5555FF),
                            Color(0xFFFF55FF)
                        )
                    )
                } else {
                    Brush.horizontalGradient(gradientStops.map { Color(0xFF000000.toInt() or (it and 0xFFFFFF)) })
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(stripBrush)
                )

                McFormattedText(
                    rawLine = generatedMarkup.ifEmpty { "Enter text below..." },
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    placeholder = "Enter gradient text..."
                )

                if (generatedMarkup.isNotEmpty()) {
                    Text(
                        text = generatedMarkup,
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        ),
                        maxLines = 2
                    )
                }
            }
        }

        // Gradient Text Input
        OutlinedTextField(
            value = gradientText,
            onValueChange = onTextChange,
            label = { Text("Text to Colorize") },
            placeholder = { Text("e.g. EMERALDCRAFT SMP") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rgb_text_input")
        )

        // Curated Gradient Presets
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Quick RGB Presets",
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MotdCatalog.gradientPresets.forEach { preset ->
                    val brush = if (preset.isRainbow) {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF5555),
                                Color(0xFFFFFF55),
                                Color(0xFF55FF55),
                                Color(0xFF55FFFF),
                                Color(0xFFFF55FF)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(preset.colors.map { Color(0xFF000000.toInt() or (it and 0xFFFFFF)) })
                    }
                    Surface(
                        onClick = { onApplyPreset(preset) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 24.dp, height = 14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(brush)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        // Gradient Mode & Color Stop Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Gradient Mode & Color Stops",
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isThreeStop && !isRainbow,
                    onClick = { onToggleThreeStop(false) },
                    label = { Text("2-Color") }
                )
                FilterChip(
                    selected = isThreeStop && !isRainbow,
                    onClick = { onToggleThreeStop(true) },
                    label = { Text("3-Color") }
                )
                FilterChip(
                    selected = isRainbow,
                    onClick = { onSetRainbow(!isRainbow) },
                    label = { Text("Rainbow") }
                )
            }

            if (!isRainbow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gradientStops.forEachIndexed { idx, rgb ->
                        val isSelected = idx == selectedStopIndex
                        val stopLabel = when {
                            idx == 0 -> "Start"
                            idx == gradientStops.lastIndex -> "End"
                            else -> "Mid"
                        }
                        Surface(
                            onClick = { onSelectStop(idx) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("gradient_stop_$idx")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF000000.toInt() or (rgb and 0xFFFFFF)))
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = stopLabel,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = MotdEngine.formatHexHash(rgb),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive HSV Color Picker + Hex Input
                HsvRgbColorPicker(
                    rgb = activeStopColor,
                    onRgbChange = onUpdateStopColor
                )
            }
        }

        // Style & Output Format
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Decoration & Output Syntax",
                style = MaterialTheme.typography.titleSmall
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = bold,
                    onClick = { onToggleStyle(!bold, null, null, null) },
                    label = { Text("Bold", fontWeight = FontWeight.Bold) }
                )
                FilterChip(
                    selected = italic,
                    onClick = { onToggleStyle(null, !italic, null, null) },
                    label = { Text("Italic") }
                )
                FilterChip(
                    selected = underline,
                    onClick = { onToggleStyle(null, null, !underline, null) },
                    label = { Text("Underline") }
                )
                FilterChip(
                    selected = strike,
                    onClick = { onToggleStyle(null, null, null, !strike) },
                    label = { Text("Strike") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GradientOutputStyle.entries.forEach { style ->
                    FilterChip(
                        selected = outputStyle == style,
                        onClick = { onSelectOutputStyle(style) },
                        label = { Text(style.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        // Apply / Insert Actions
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onInsertIntoLine(activeLine, false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("rgb_insert_active_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Insert L$activeLine")
                }

                OutlinedButton(
                    onClick = { onInsertIntoLine(activeLine, true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("rgb_replace_active_button")
                ) {
                    Icon(Icons.Rounded.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Replace L$activeLine")
                }
            }

            OutlinedButton(
                onClick = { onCopySnippet(generatedMarkup) },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("rgb_copy_snippet_button")
            ) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Gradient Snippet")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HsvRgbColorPicker(
    rgb: Int,
    onRgbChange: (Int) -> Unit
) {
    val hsv = remember(rgb) {
        val arr = FloatArray(3)
        android.graphics.Color.RGBToHSV(
            (rgb shr 16) and 0xFF,
            (rgb shr 8) and 0xFF,
            rgb and 0xFF,
            arr
        )
        arr
    }

    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var sat by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    var hexInput by remember(rgb) { mutableStateOf(MotdEngine.formatHexRaw(rgb)) }

    LaunchedEffect(rgb) {
        val currentConverted = MotdEngine.hsvToRgb(hue, sat, value)
        if (currentConverted != rgb) {
            hue = hsv[0]
            sat = hsv[1]
            value = hsv[2]
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 2D Saturation / Value Canvas
            val pureHueColor = Color(0xFF000000.toInt() or (MotdEngine.hsvToRgb(hue, 1f, 1f) and 0xFFFFFF))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .pointerInput(hue) {
                        detectTapGestures { offset ->
                            val s = (offset.x / size.width).coerceIn(0f, 1f)
                            val v = (1f - offset.y / size.height).coerceIn(0f, 1f)
                            sat = s
                            value = v
                            onRgbChange(MotdEngine.hsvToRgb(hue, s, v))
                        }
                    }
                    .pointerInput(hue) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val s = (change.position.x / size.width).coerceIn(0f, 1f)
                            val v = (1f - change.position.y / size.height).coerceIn(0f, 1f)
                            sat = s
                            value = v
                            onRgbChange(MotdEngine.hsvToRgb(hue, s, v))
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White, pureHueColor)
                        )
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black)
                        )
                    )
                    val cx = sat * size.width
                    val cy = (1f - value) * size.height
                    drawCircle(
                        color = Color.Black,
                        radius = 10.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 8.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Hue Slider with Rainbow Track
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Red,
                                    Color.Yellow,
                                    Color.Green,
                                    Color.Cyan,
                                    Color.Blue,
                                    Color.Magenta,
                                    Color.Red
                                )
                            )
                        )
                )
                Slider(
                    value = hue,
                    onValueChange = { newHue ->
                        hue = newHue
                        onRgbChange(MotdEngine.hsvToRgb(newHue, sat, value))
                    },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }

            // Hex Input & RGB Quick Swatches
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF000000.toInt() or (rgb and 0xFFFFFF)))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                )
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { raw ->
                        val filtered = raw.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }.take(6)
                        hexInput = filtered
                        if (filtered.length == 6) {
                            filtered.toIntOrNull(16)?.let(onRgbChange)
                        }
                    },
                    prefix = { Text("#", style = TextStyle(fontFamily = JetBrainsMonoFamily)) },
                    label = { Text("Hex") },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 14.sp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.width(140.dp)
                )

                val quickSwatches = listOf(0xFF5555, 0xFFAA00, 0x55FF55, 0x55FFFF, 0xA855F7, 0xEC4899)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSwatches.forEach { swatch ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF000000.toInt() or (swatch and 0xFFFFFF)))
                                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                .clickable { onRgbChange(swatch) }
                        )
                    }
                }
            }
        }
    }
}

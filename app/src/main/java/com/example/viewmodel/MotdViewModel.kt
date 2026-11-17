package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MotdRepository
import com.example.engine.MotdCatalog
import com.example.engine.MotdEngine
import com.example.model.EmojiCategory
import com.example.model.ExportFormat
import com.example.model.GradientOutputStyle
import com.example.model.GradientPreset
import com.example.model.McColor
import com.example.model.McFormat
import com.example.model.MotdPreset
import com.example.model.StudioTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class MotdUiState(
    val activeTab: StudioTab = StudioTab.EDITOR,
    val activeLine: Int = 1, // 1 or 2
    val serverName: String = "EmeraldCraft Network",
    val playerCount: String = "148/500",
    val line1: TextFieldValue = TextFieldValue(MotdCatalog.defaultPresets.first().line1),
    val line2: TextFieldValue = TextFieldValue(MotdCatalog.defaultPresets.first().line2),
    val customIconBitmap: Bitmap? = null,
    val useAmpersandCodes: Boolean = true, // true = &a, false = §a
    val isDarkStudio: Boolean = true,
    val canUndo: Boolean = false,
    // RGB Gradient Studio state
    val gradientText: String = "EMERALDCRAFT",
    val gradientStops: List<Int> = listOf(0x22C55E, 0x06B6D4),
    val selectedStopIndex: Int = 0,
    val isThreeStopGradient: Boolean = false,
    val isRainbowGradient: Boolean = false,
    val gradientBold: Boolean = true,
    val gradientItalic: Boolean = false,
    val gradientUnderline: Boolean = false,
    val gradientStrike: Boolean = false,
    val gradientOutputStyle: GradientOutputStyle = GradientOutputStyle.MINIMESSAGE,
    // Emoji Vault state
    val emojiQuery: String = "",
    val selectedEmojiCategory: EmojiCategory = EmojiCategory.ALL,
    val recentEmojis: List<String> = listOf("✦", "⚡", "⚔", "💎", "🔥", "✔", "👑", "⛏"),
    // Swap & Export state
    val customPresets: List<MotdPreset> = emptyList(),
    val selectedExportFormat: ExportFormat = ExportFormat.SERVER_PROPERTIES
)

private data class HistorySnapshot(
    val line1: String,
    val line2: String,
    val serverName: String
)

class MotdViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MotdRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(MotdUiState())
    val uiState: StateFlow<MotdUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val undoStack = ArrayDeque<HistorySnapshot>()
    private var persistJob: Job? = null

    init {
        viewModelScope.launch {
            val saved = repository.stateFlow.first()
            _uiState.update {
                it.copy(
                    serverName = saved.serverName,
                    playerCount = saved.playerCount,
                    line1 = TextFieldValue(saved.line1, TextRange(saved.line1.length)),
                    line2 = TextFieldValue(saved.line2, TextRange(saved.line2.length)),
                    customPresets = saved.customPresets,
                    recentEmojis = saved.recentEmojis,
                    isDarkStudio = saved.isDarkStudio
                )
            }
        }
    }

    fun selectTab(tab: StudioTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setActiveLine(line: Int) {
        _uiState.update { it.copy(activeLine = if (line == 2) 2 else 1) }
    }

    fun toggleStudioTheme() {
        val next = !_uiState.value.isDarkStudio
        _uiState.update { it.copy(isDarkStudio = next) }
        viewModelScope.launch { repository.setDarkStudio(next) }
    }

    fun togglePrefixMode() {
        _uiState.update { it.copy(useAmpersandCodes = !it.useAmpersandCodes) }
    }

    fun updateServerMeta(serverName: String, playerCount: String) {
        _uiState.update {
            it.copy(serverName = serverName, playerCount = playerCount)
        }
        schedulePersist()
    }

    fun updateLine1(value: TextFieldValue) {
        val prevText = _uiState.value.line1.text
        if (prevText != value.text) pushHistory()
        _uiState.update { it.copy(line1 = value, activeLine = 1, canUndo = undoStack.isNotEmpty()) }
        if (prevText != value.text) schedulePersist()
    }

    fun updateLine2(value: TextFieldValue) {
        val prevText = _uiState.value.line2.text
        if (prevText != value.text) pushHistory()
        _uiState.update { it.copy(line2 = value, activeLine = 2, canUndo = undoStack.isNotEmpty()) }
        if (prevText != value.text) schedulePersist()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val snap = undoStack.removeLast()
        _uiState.update {
            it.copy(
                line1 = TextFieldValue(snap.line1, TextRange(snap.line1.length)),
                line2 = TextFieldValue(snap.line2, TextRange(snap.line2.length)),
                serverName = snap.serverName,
                canUndo = undoStack.isNotEmpty()
            )
        }
        schedulePersist()
    }

    fun swapLines() {
        pushHistory()
        _uiState.update { state ->
            state.copy(
                line1 = state.line2,
                line2 = state.line1,
                canUndo = undoStack.isNotEmpty()
            )
        }
        schedulePersist()
        emitToast("Swapped Line 1 ⇄ Line 2")
    }

    fun centerLine(lineNumber: Int) {
        pushHistory()
        _uiState.update { state ->
            if (lineNumber == 1) {
                val centered = MotdEngine.centerLine(state.line1.text)
                state.copy(
                    line1 = TextFieldValue(centered, TextRange(centered.length)),
                    canUndo = undoStack.isNotEmpty()
                )
            } else {
                val centered = MotdEngine.centerLine(state.line2.text)
                state.copy(
                    line2 = TextFieldValue(centered, TextRange(centered.length)),
                    canUndo = undoStack.isNotEmpty()
                )
            }
        }
        schedulePersist()
        emitToast("Centered Line $lineNumber for server list")
    }

    fun clearLine(lineNumber: Int) {
        pushHistory()
        _uiState.update { state ->
            if (lineNumber == 1) {
                state.copy(line1 = TextFieldValue(""), canUndo = undoStack.isNotEmpty())
            } else {
                state.copy(line2 = TextFieldValue(""), canUndo = undoStack.isNotEmpty())
            }
        }
        schedulePersist()
    }

    fun insertColorCode(color: McColor) {
        val prefix = if (_uiState.value.useAmpersandCodes) '&' else '§'
        insertTokenAtCursor("$prefix${color.code}")
    }

    fun insertFormatCode(format: McFormat) {
        val prefix = if (_uiState.value.useAmpersandCodes) '&' else '§'
        insertTokenAtCursor("$prefix${format.code}")
    }

    fun insertSymbol(symbol: String, isWrapper: Boolean = false) {
        pushHistory()
        val state = _uiState.value
        val target = if (state.activeLine == 1) state.line1 else state.line2
        val start = target.selection.min.coerceIn(0, target.text.length)
        val end = target.selection.max.coerceIn(0, target.text.length)

        val newValue = if (isWrapper && symbol.length >= 2) {
            val left = symbol.substring(0, symbol.length / 2)
            val right = symbol.substring(symbol.length / 2)
            val selected = target.text.substring(start, end)
            val inserted = "$left$selected$right"
            val updatedText = target.text.replaceRange(start, end, inserted)
            val cursor = if (selected.isEmpty()) start + left.length else start + inserted.length
            TextFieldValue(updatedText, TextRange(cursor))
        } else {
            val updatedText = target.text.replaceRange(start, end, symbol)
            TextFieldValue(updatedText, TextRange(start + symbol.length))
        }

        _uiState.update {
            if (it.activeLine == 1) {
                it.copy(line1 = newValue, canUndo = undoStack.isNotEmpty())
            } else {
                it.copy(line2 = newValue, canUndo = undoStack.isNotEmpty())
            }
        }
        recordRecentEmoji(symbol)
        schedulePersist()
        emitToast("Inserted $symbol into Line ${state.activeLine}")
    }

    private fun insertTokenAtCursor(token: String) {
        pushHistory()
        val state = _uiState.value
        val target = if (state.activeLine == 1) state.line1 else state.line2
        val start = target.selection.min.coerceIn(0, target.text.length)
        val end = target.selection.max.coerceIn(0, target.text.length)
        val updatedText = target.text.replaceRange(start, end, token)
        val newValue = TextFieldValue(updatedText, TextRange(start + token.length))

        _uiState.update {
            if (it.activeLine == 1) {
                it.copy(line1 = newValue, canUndo = undoStack.isNotEmpty())
            } else {
                it.copy(line2 = newValue, canUndo = undoStack.isNotEmpty())
            }
        }
        schedulePersist()
    }

    // RGB Gradient Studio actions
    fun updateGradientText(text: String) {
        _uiState.update { it.copy(gradientText = text) }
    }

    fun toggleThreeStopGradient(enabled: Boolean) {
        _uiState.update { state ->
            val stops = if (enabled) {
                if (state.gradientStops.size >= 3) state.gradientStops.take(3)
                else state.gradientStops + 0xA855F7
            } else {
                state.gradientStops.take(2)
            }
            state.copy(
                isThreeStopGradient = enabled,
                isRainbowGradient = false,
                gradientStops = stops,
                selectedStopIndex = state.selectedStopIndex.coerceAtMost(stops.lastIndex)
            )
        }
    }

    fun setRainbowMode(enabled: Boolean) {
        _uiState.update { it.copy(isRainbowGradient = enabled) }
    }

    fun selectStopIndex(index: Int) {
        _uiState.update { state ->
            state.copy(selectedStopIndex = index.coerceIn(0, state.gradientStops.lastIndex))
        }
    }

    fun updateSelectedStopColor(rgb: Int) {
        _uiState.update { state ->
            val mutable = state.gradientStops.toMutableList()
            val idx = state.selectedStopIndex.coerceIn(0, mutable.lastIndex)
            mutable[idx] = rgb and 0xFFFFFF
            state.copy(gradientStops = mutable, isRainbowGradient = false)
        }
    }

    fun applyGradientPreset(preset: GradientPreset) {
        _uiState.update {
            it.copy(
                gradientStops = preset.colors,
                isThreeStopGradient = preset.colors.size >= 3,
                isRainbowGradient = preset.isRainbow,
                selectedStopIndex = 0
            )
        }
    }

    fun toggleGradientStyle(
        bold: Boolean? = null,
        italic: Boolean? = null,
        underline: Boolean? = null,
        strike: Boolean? = null
    ) {
        _uiState.update {
            it.copy(
                gradientBold = bold ?: it.gradientBold,
                gradientItalic = italic ?: it.gradientItalic,
                gradientUnderline = underline ?: it.gradientUnderline,
                gradientStrike = strike ?: it.gradientStrike
            )
        }
    }

    fun setGradientOutputStyle(style: GradientOutputStyle) {
        _uiState.update { it.copy(gradientOutputStyle = style) }
    }

    fun currentGradientMarkup(): String {
        val s = _uiState.value
        return MotdEngine.generateGradientMarkup(
            text = s.gradientText,
            colors = s.gradientStops,
            isRainbow = s.isRainbowGradient,
            bold = s.gradientBold,
            italic = s.gradientItalic,
            underline = s.gradientUnderline,
            strikethrough = s.gradientStrike,
            style = s.gradientOutputStyle
        )
    }

    fun applyGradientToLine(lineNumber: Int, replaceLine: Boolean) {
        val snippet = currentGradientMarkup()
        if (snippet.isEmpty()) {
            emitToast("Enter gradient text first")
            return
        }
        pushHistory()
        _uiState.update { state ->
            val target = if (lineNumber == 1) state.line1 else state.line2
            val newValue = if (replaceLine) {
                TextFieldValue(snippet, TextRange(snippet.length))
            } else {
                val start = target.selection.min.coerceIn(0, target.text.length)
                val end = target.selection.max.coerceIn(0, target.text.length)
                val updated = target.text.replaceRange(start, end, snippet)
                TextFieldValue(updated, TextRange(start + snippet.length))
            }
            if (lineNumber == 1) {
                state.copy(line1 = newValue, activeLine = 1, canUndo = undoStack.isNotEmpty())
            } else {
                state.copy(line2 = newValue, activeLine = 2, canUndo = undoStack.isNotEmpty())
            }
        }
        schedulePersist()
        emitToast(
            if (replaceLine) "Replaced Line $lineNumber with RGB gradient"
            else "Inserted RGB gradient into Line $lineNumber"
        )
    }

    // Emoji Vault actions
    fun updateEmojiQuery(query: String) {
        _uiState.update { it.copy(emojiQuery = query) }
    }

    fun selectEmojiCategory(category: EmojiCategory) {
        _uiState.update { it.copy(selectedEmojiCategory = category) }
    }

    private fun recordRecentEmoji(symbol: String) {
        val updated = (listOf(symbol) + _uiState.value.recentEmojis.filter { it != symbol }).take(12)
        _uiState.update { it.copy(recentEmojis = updated) }
        viewModelScope.launch { repository.saveRecentEmojis(updated) }
    }

    // Swap & Export actions
    fun swapWithPreset(preset: MotdPreset) {
        pushHistory()
        _uiState.update {
            it.copy(
                serverName = preset.serverName,
                line1 = TextFieldValue(preset.line1, TextRange(preset.line1.length)),
                line2 = TextFieldValue(preset.line2, TextRange(preset.line2.length)),
                canUndo = undoStack.isNotEmpty()
            )
        }
        schedulePersist()
        emitToast("Swapped to \"${preset.name}\"")
    }

    fun saveCurrentAsSlot(slotName: String) {
        val state = _uiState.value
        val cleanName = slotName.trim().ifEmpty {
            "Slot #${state.customPresets.size + 1}"
        }
        val newPreset = MotdPreset(
            id = UUID.randomUUID().toString(),
            name = cleanName,
            category = "Custom Slot",
            serverName = state.serverName,
            line1 = state.line1.text,
            line2 = state.line2.text,
            isCustom = true
        )
        val updated = listOf(newPreset) + state.customPresets
        _uiState.update { it.copy(customPresets = updated) }
        viewModelScope.launch { repository.saveCustomPresets(updated) }
        emitToast("Saved \"$cleanName\" to Quick Swap")
    }

    fun deleteCustomSlot(id: String) {
        val updated = _uiState.value.customPresets.filterNot { it.id == id }
        _uiState.update { it.copy(customPresets = updated) }
        viewModelScope.launch { repository.saveCustomPresets(updated) }
    }

    fun selectExportFormat(format: ExportFormat) {
        _uiState.update { it.copy(selectedExportFormat = format) }
    }

    fun setCustomServerIcon(uri: Uri?) {
        if (uri == null) {
            _uiState.update { it.copy(customIconBitmap = null) }
            return
        }
        viewModelScope.launch {
            val bmp = withContext(Dispatchers.IO) {
                try {
                    val cr = getApplication<Application>().contentResolver
                    val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(cr, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(cr, uri)
                    }
                    Bitmap.createScaledBitmap(raw, 128, 128, false)
                } catch (_: Exception) {
                    null
                }
            }
            if (bmp != null) {
                _uiState.update { it.copy(customIconBitmap = bmp) }
                emitToast("Updated server preview icon")
            }
        }
    }

    private fun pushHistory() {
        val state = _uiState.value
        val snap = HistorySnapshot(state.line1.text, state.line2.text, state.serverName)
        if (undoStack.lastOrNull() != snap) {
            undoStack.addLast(snap)
            if (undoStack.size > 30) undoStack.removeFirst()
        }
    }

    private fun schedulePersist() {
        persistJob?.cancel()
        persistJob = viewModelScope.launch {
            delay(350)
            val s = _uiState.value
            repository.saveDraft(s.serverName, s.playerCount, s.line1.text, s.line2.text)
        }
    }

    fun emitToast(message: String) {
        _toastMessage.tryEmit(message)
    }
}

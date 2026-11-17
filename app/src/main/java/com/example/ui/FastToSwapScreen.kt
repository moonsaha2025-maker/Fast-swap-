package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Gradient
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.engine.MotdEngine
import com.example.model.ExportFormat
import com.example.model.StudioTab
import com.example.ui.components.EditorTab
import com.example.ui.components.EmojiVaultTab
import com.example.ui.components.MinecraftPreviewCard
import com.example.ui.components.RgbStudioTab
import com.example.ui.components.SwapExportTab
import com.example.ui.theme.FastToSwapTheme
import com.example.viewmodel.MotdUiState
import com.example.viewmodel.MotdViewModel

@Composable
fun FastToSwapApp(viewModel: MotdViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSaveSlotDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setCustomServerIcon(uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Back gesture returns from secondary tabs to Editor first
    BackHandler(enabled = state.activeTab != StudioTab.EDITOR) {
        viewModel.selectTab(StudioTab.EDITOR)
    }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        viewModel.emitToast("Copied $label to clipboard")
    }

    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Minecraft MOTD")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share MOTD"))
    }

    FastToSwapTheme(darkTheme = state.isDarkStudio) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpanded = maxWidth >= 720.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!isExpanded) {
                        NavigationBar {
                            StudioTab.entries.forEach { tab ->
                                val selected = state.activeTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = tabIcon(tab, selected),
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                if (isExpanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .statusBarsPadding()
                    ) {
                        NavigationRail(
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))
                            IconButton(onClick = { viewModel.toggleStudioTheme() }) {
                                Icon(
                                    imageVector = if (state.isDarkStudio) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                    contentDescription = "Toggle light or dark theme"
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            StudioTab.entries.forEach { tab ->
                                val selected = state.activeTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = tabIcon(tab, selected),
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = { Text(tab.title) },
                                    modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                )
                            }
                        }

                        // Left Pane: Brand Header + Pinned Preview
                        Column(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StudioTopBar(
                                isDark = state.isDarkStudio,
                                onToggleTheme = { viewModel.toggleStudioTheme() },
                                showThemeToggle = false
                            )
                            MinecraftPreviewCard(
                                serverName = state.serverName,
                                playerCount = state.playerCount,
                                line1 = state.line1.text,
                                line2 = state.line2.text,
                                customIconBitmap = state.customIconBitmap,
                                activeLine = state.activeLine,
                                onUpdateServerMeta = viewModel::updateServerMeta,
                                onPickIconClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onResetIconClick = { viewModel.setCustomServerIcon(null) },
                                onSwapLines = viewModel::swapLines,
                                onCenterActiveLine = { viewModel.centerLine(state.activeLine) },
                                onQuickCopy = {
                                    val out = MotdEngine.exportMotd(
                                        state.line1.text,
                                        state.line2.text,
                                        ExportFormat.LEGACY_SECTION
                                    )
                                    copyToClipboard(out, "MOTD (§ codes)")
                                },
                                onQuickSaveSlot = { showSaveSlotDialog = true },
                                onSelectLine = viewModel::setActiveLine
                            )
                        }

                        // Right Pane: Active Tool Studio
                        Box(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight()
                        ) {
                            StudioTabContent(
                                state = state,
                                viewModel = viewModel,
                                onOpenSaveSlotDialog = { showSaveSlotDialog = true },
                                onCopy = ::copyToClipboard,
                                onShare = ::shareText
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .statusBarsPadding()
                            .widthIn(max = 600.dp)
                            .align(Alignment.Center)
                    ) {
                        StudioTopBar(
                            isDark = state.isDarkStudio,
                            onToggleTheme = { viewModel.toggleStudioTheme() },
                            showThemeToggle = true,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        MinecraftPreviewCard(
                            serverName = state.serverName,
                            playerCount = state.playerCount,
                            line1 = state.line1.text,
                            line2 = state.line2.text,
                            customIconBitmap = state.customIconBitmap,
                            activeLine = state.activeLine,
                            onUpdateServerMeta = viewModel::updateServerMeta,
                            onPickIconClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onResetIconClick = { viewModel.setCustomServerIcon(null) },
                            onSwapLines = viewModel::swapLines,
                            onCenterActiveLine = { viewModel.centerLine(state.activeLine) },
                            onQuickCopy = {
                                val out = MotdEngine.exportMotd(
                                    state.line1.text,
                                    state.line2.text,
                                    ExportFormat.LEGACY_SECTION
                                )
                                copyToClipboard(out, "MOTD (§ codes)")
                            },
                            onQuickSaveSlot = { showSaveSlotDialog = true },
                            onSelectLine = viewModel::setActiveLine,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            StudioTabContent(
                                state = state,
                                viewModel = viewModel,
                                onOpenSaveSlotDialog = { showSaveSlotDialog = true },
                                onCopy = ::copyToClipboard,
                                onShare = ::shareText
                            )
                        }
                    }
                }
            }

            if (showSaveSlotDialog) {
                var slotName by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showSaveSlotDialog = false },
                    title = { Text("Save Quick-Swap Slot") },
                    text = {
                        OutlinedTextField(
                            value = slotName,
                            onValueChange = { slotName = it },
                            label = { Text("Slot Name (e.g. Weekend Event)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.saveCurrentAsSlot(slotName)
                                showSaveSlotDialog = false
                            }
                        ) {
                            Text("Save Slot")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSaveSlotDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StudioTopBar(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    showThemeToggle: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "MC MOTD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }

        if (showThemeToggle) {
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = "Switch studio theme"
                )
            }
        }
    }
}

@Composable
private fun StudioTabContent(
    state: MotdUiState,
    viewModel: MotdViewModel,
    onOpenSaveSlotDialog: () -> Unit,
    onCopy: (String, String) -> Unit,
    onShare: (String) -> Unit
) {
    Crossfade(targetState = state.activeTab, label = "studio_tab_crossfade") { tab ->
        when (tab) {
            StudioTab.EDITOR -> {
                EditorTab(
                    line1 = state.line1,
                    line2 = state.line2,
                    activeLine = state.activeLine,
                    useAmpersand = state.useAmpersandCodes,
                    canUndo = state.canUndo,
                    recentEmojis = state.recentEmojis,
                    onLine1Change = viewModel::updateLine1,
                    onLine2Change = viewModel::updateLine2,
                    onSelectActiveLine = viewModel::setActiveLine,
                    onTogglePrefixMode = viewModel::togglePrefixMode,
                    onUndo = viewModel::undo,
                    onClearLine = viewModel::clearLine,
                    onInsertColor = viewModel::insertColorCode,
                    onInsertFormat = viewModel::insertFormatCode,
                    onInsertEmoji = { sym -> viewModel.insertSymbol(sym) },
                    onOpenRgbStudio = { viewModel.selectTab(StudioTab.RGB) }
                )
            }

            StudioTab.RGB -> {
                val markup = remember(
                    state.gradientText,
                    state.gradientStops,
                    state.isRainbowGradient,
                    state.gradientBold,
                    state.gradientItalic,
                    state.gradientUnderline,
                    state.gradientStrike,
                    state.gradientOutputStyle
                ) {
                    viewModel.currentGradientMarkup()
                }
                RgbStudioTab(
                    gradientText = state.gradientText,
                    gradientStops = state.gradientStops,
                    selectedStopIndex = state.selectedStopIndex,
                    isThreeStop = state.isThreeStopGradient,
                    isRainbow = state.isRainbowGradient,
                    bold = state.gradientBold,
                    italic = state.gradientItalic,
                    underline = state.gradientUnderline,
                    strike = state.gradientStrike,
                    outputStyle = state.gradientOutputStyle,
                    generatedMarkup = markup,
                    activeLine = state.activeLine,
                    onTextChange = viewModel::updateGradientText,
                    onToggleThreeStop = viewModel::toggleThreeStopGradient,
                    onSetRainbow = viewModel::setRainbowMode,
                    onSelectStop = viewModel::selectStopIndex,
                    onUpdateStopColor = viewModel::updateSelectedStopColor,
                    onApplyPreset = viewModel::applyGradientPreset,
                    onToggleStyle = viewModel::toggleGradientStyle,
                    onSelectOutputStyle = viewModel::setGradientOutputStyle,
                    onInsertIntoLine = viewModel::applyGradientToLine,
                    onCopySnippet = { snippet -> onCopy(snippet, "RGB snippet") }
                )
            }

            StudioTab.EMOJIS -> {
                EmojiVaultTab(
                    activeLine = state.activeLine,
                    query = state.emojiQuery,
                    selectedCategory = state.selectedEmojiCategory,
                    recentEmojis = state.recentEmojis,
                    onSelectActiveLine = viewModel::setActiveLine,
                    onQueryChange = viewModel::updateEmojiQuery,
                    onSelectCategory = viewModel::selectEmojiCategory,
                    onInsertSymbol = { item -> viewModel.insertSymbol(item.symbol, item.isWrapper) },
                    onInsertRawEmoji = { sym -> viewModel.insertSymbol(sym, false) }
                )
            }

            StudioTab.SWAP_EXPORT -> {
                SwapExportTab(
                    line1 = state.line1.text,
                    line2 = state.line2.text,
                    customPresets = state.customPresets,
                    selectedExportFormat = state.selectedExportFormat,
                    onSwapPreset = viewModel::swapWithPreset,
                    onOpenSaveSlotDialog = onOpenSaveSlotDialog,
                    onDeleteCustomPreset = viewModel::deleteCustomSlot,
                    onSelectExportFormat = viewModel::selectExportFormat,
                    onCopyOutput = { code -> onCopy(code, state.selectedExportFormat.label) },
                    onShareOutput = onShare
                )
            }
        }
    }
}

private fun tabIcon(tab: StudioTab, selected: Boolean) = when (tab) {
    StudioTab.EDITOR -> if (selected) Icons.Rounded.Tune else Icons.Outlined.Tune
    StudioTab.RGB -> if (selected) Icons.Rounded.Gradient else Icons.Outlined.Gradient
    StudioTab.EMOJIS -> if (selected) Icons.Rounded.EmojiEmotions else Icons.Outlined.EmojiEmotions
    StudioTab.SWAP_EXPORT -> if (selected) Icons.Rounded.SwapHoriz else Icons.Outlined.SwapHoriz
}

package come.asg.lemudroid.app.mobile.feature.settings.inputdevices

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import come.asg.lemudroid.app.mobile.feature.game.GameScreenLayout
import come.asg.lemudroid.app.mobile.feature.game.PadContainer
import come.asg.lemudroid.app.mobile.feature.game.TouchControlEditorPanel
import come.asg.lemudroid.app.utils.android.settings.rememberTouchButtonAppearanceTheme
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.settings.LocalTouchControlEditor
import com.swordfish.touchinput.radial.settings.LocalTouchControllerSettings
import com.swordfish.touchinput.radial.settings.TouchControlEditorState
import com.swordfish.touchinput.radial.settings.TouchControllerID
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.padkit.PadKit
import gg.padkit.config.HapticFeedbackType
import gg.padkit.inputevents.InputEvent
import kotlinx.coroutines.launch

/**
 * @param allowTouchOverlay whether the given console draws its pads over the game in landscape. The game
 * takes this from its ControllerConfig; wire the same lookup here so landscape matches in-game for
 * consoles that use side containers. Defaults to overlay.
 */
@Composable
fun TouchLayoutEditorScreen(
    modifier: Modifier = Modifier,
    settingsManager: TouchControllerSettingsManager,
    allowTouchOverlay: (TouchControllerID) -> Boolean = { true },
    initialControllerId: TouchControllerID? = null,
    onClose: (() -> Unit)? = null,
    onMenuPressed: (() -> Unit)? = null,
) {
    // Same orientation rule as the game (width > height), so both always pick the same layout bucket,
    // including split-screen and foldables.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            TouchLayoutEditorContent(
                isLandscape = constraints.maxWidth > constraints.maxHeight,
                settingsManager = settingsManager,
                allowTouchOverlay = allowTouchOverlay,
                initialControllerId = initialControllerId,
                onClose = onClose,
                onMenuPressed = onMenuPressed,
            )
    }
}

@Composable
private fun TouchLayoutEditorContent(
    isLandscape: Boolean,
    settingsManager: TouchControllerSettingsManager,
    allowTouchOverlay: (TouchControllerID) -> Boolean,
    initialControllerId: TouchControllerID?,
    onClose: (() -> Unit)?,
    onMenuPressed: (() -> Unit)?,
) {
    // Saveable: system rotation can recreate the activity while keeping the editor open.
    var controllerId by rememberSaveable { mutableStateOf(initialControllerId ?: TouchControllerID.GB) }
    var layoutMenuExpanded by remember { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    val orientation =
        if (isLandscape) {
            TouchControllerSettingsManager.Orientation.LANDSCAPE
        } else {
            TouchControllerSettingsManager.Orientation.PORTRAIT
        }
    var selectedControlKey by rememberSaveable(controllerId, orientation) { mutableStateOf<String?>(null) }

    val safeDrawingInsets = WindowInsets.safeDrawing
    val density = LocalDensity.current
    val insets = WindowInsets.displayCutout
    val settingsFlow =
        remember(controllerId, orientation, density, insets, settingsManager) {
            settingsManager.observeSettings(controllerId, orientation, density, insets)
        }
    // Null until the real settings arrive, so nothing renders (or gets saved) with zero margins.
    val settings = settingsFlow.collectAsState(null).value
    val coroutineScope = rememberCoroutineScope()
    val padConfig = remember(controllerId) { TouchControllerID.getConfig(controllerId) }

    fun updateControl(
        key: String,
        transform: (TouchControllerSettingsManager.ControlSettings) -> TouchControllerSettingsManager.ControlSettings,
    ) = settingsManager.updateControl(controllerId, orientation, key, transform)

    fun persist() = settingsManager.persist(controllerId, orientation)

    DisposableEffect(controllerId, orientation, settingsManager) {
        onDispose { settingsManager.persist(controllerId, orientation) }
    }

    val maxOffset = TouchControllerSettingsManager.MAX_CONTROL_OFFSET
    val editorState =
        TouchControlEditorState(
            isEditing = true,
            selectedControlKey = selectedControlKey,
            onSelect = { selectedControlKey = it },
            // Dragging is rendered locally by touchControlTransform. Commit only once when the
            // gesture ends so pointer motion never waits on the settings StateFlow.
            onDrag = { _, _, _ -> },
            onDragEnd = { key, dx, dy ->
                if (dx != 0f || dy != 0f) {
                    updateControl(key) {
                        it.copy(
                            offsetX = (it.offsetX + dx).coerceIn(-maxOffset, maxOffset),
                            offsetY = (it.offsetY + dy).coerceIn(-maxOffset, maxOffset),
                        )
                    }
                }
                persist()
            },
        )

    Box(modifier = Modifier.fillMaxSize()) {
        if (settings != null) {
            val overlay = allowTouchOverlay(controllerId)
            CompositionLocalProvider(
                LocalLemuroidPadTheme provides rememberTouchButtonAppearanceTheme(),
                LocalTouchControllerSettings provides settings,
                LocalTouchControlEditor provides editorState,
            ) {
                PadKit(
                    modifier = Modifier.fillMaxSize(),
                    onInputEvents = { events ->
                        if (events.any { it is InputEvent.Button && it.id == KeyEvent.KEYCODE_BUTTON_MODE && it.pressed }) {
                            onMenuPressed?.invoke()
                        }
                    },
                    hapticFeedbackType = HapticFeedbackType.NONE,
                ) {
                    val padKitScope = this
                    // Mirrors MobileGameScreen: no safe-drawing padding on the pad layer (the pads apply
                    // their own cutout margins), the game view only avoids the top cutout, and the same
                    // backdrop containers are drawn.
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize(),
                        constraintSet = GameScreenLayout.buildConstraintSet(isLandscape, overlay),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .layoutId(GameScreenLayout.CONSTRAINTS_GAME_VIEW)
                                    .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top)),
                        )
                        if (!isLandscape) {
                            PadContainer(modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_BOTTOM_CONTAINER))
                        } else if (!overlay) {
                            PadContainer(modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_CONTAINER))
                            PadContainer(modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_CONTAINER))
                        }
                        padConfig.leftComposable(
                            padKitScope,
                            Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_PAD),
                            settings,
                        )
                        padConfig.rightComposable(
                            padKitScope,
                            Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_PAD),
                            settings,
                        )
                        Box(modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_GAME_CONTAINER))
                    }
                }
            }
        }

        Card(
            modifier =
                Modifier.align(Alignment.TopCenter).widthIn(max = 760.dp)
                    .windowInsetsPadding(safeDrawingInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Row(
                modifier =
                    Modifier.horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                IconButton(onClick = { onClose?.invoke() ?: backDispatcher?.onBackPressed() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close editor")
                }
                Box {
                    TextButton(onClick = { layoutMenuExpanded = true }) {
                        Text(controllerId.displayLabel(), maxLines = 1)
                    }
                    DropdownMenu(expanded = layoutMenuExpanded, onDismissRequest = { layoutMenuExpanded = false }) {
                        TouchControllerID.values().forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.displayLabel()) },
                                onClick = {
                                    controllerId = item
                                    layoutMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                IconButton(onClick = { confirmReset = true }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset layout")
                }
                IconButton(onClick = { persist() }) {
                    Icon(Icons.Filled.Save, contentDescription = "Save layout")
                }
            }
        }

        if (selectedControlKey == null) {
            Card(
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .windowInsetsPadding(safeDrawingInsets.only(WindowInsetsSides.Top))
                        .padding(top = 68.dp),
            ) {
                Text(
                    "Tap to select · drag to move",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        BackHandler(enabled = selectedControlKey != null) { selectedControlKey = null }
        if (settings != null && selectedControlKey != null) {
            Box(
                modifier =
                    Modifier.align(Alignment.TopCenter).fillMaxWidth().widthIn(max = 760.dp)
                        .windowInsetsPadding(safeDrawingInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                        .padding(top = 64.dp),
            ) {
                TouchControlEditorPanel(
                    selectedControlKey = selectedControlKey,
                    settings = settings,
                    onUpdateControl = { key, update -> updateControl(key, update) },
                    onPersist = { persist() },
                    onResetAll = {
                        coroutineScope.launch { settingsManager.resetSettings(controllerId, orientation) }
                        selectedControlKey = null
                    },
                    onDone = { selectedControlKey = null },
                )
            }
        }

        if (confirmReset) {
            AlertDialog(
                onDismissRequest = { confirmReset = false },
                title = { Text("Reset ${orientation.name.lowercase()} layout?") },
                text = { Text("This resets only this console's ${orientation.name.lowercase()} touch layout.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch { settingsManager.resetSettings(controllerId, orientation) }
                            selectedControlKey = null
                            confirmReset = false
                        },
                    ) { Text("Reset") }
                },
                dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
            )
        }
    }
}

private fun TouchControllerID.displayLabel(): String =
    when (this) {
        TouchControllerID.GB -> "Game Boy"
        TouchControllerID.NES -> "NES"
        TouchControllerID.DESMUME -> "Nintendo DS"
        TouchControllerID.MELONDS -> "Nintendo DS (melonDS)"
        TouchControllerID.PSX -> "PlayStation"
        TouchControllerID.PSX_DUALSHOCK -> "PlayStation (DualShock)"
        TouchControllerID.N64 -> "Nintendo 64"
        TouchControllerID.PSP -> "PSP"
        TouchControllerID.SNES -> "SNES"
        TouchControllerID.GBA -> "Game Boy Advance"
        TouchControllerID.GENESIS_3 -> "Genesis (3-button)"
        TouchControllerID.GENESIS_6 -> "Genesis (6-button)"
        TouchControllerID.ATARI2600 -> "Atari 2600"
        TouchControllerID.ATARI7800 -> "Atari 7800"
        TouchControllerID.SMS -> "Master System"
        TouchControllerID.GG -> "Game Gear"
        TouchControllerID.ARCADE_4 -> "Arcade (4-button)"
        TouchControllerID.ARCADE_6 -> "Arcade (6-button)"
        TouchControllerID.LYNX -> "Atari Lynx"
        TouchControllerID.PCE -> "PC Engine"
        TouchControllerID.NGP -> "Neo Geo Pocket"
        TouchControllerID.DOS -> "DOS"
        TouchControllerID.WS_LANDSCAPE -> "WonderSwan (landscape)"
        TouchControllerID.WS_PORTRAIT -> "WonderSwan (portrait)"
        TouchControllerID.NINTENDO_3DS -> "Nintendo 3DS"
    }

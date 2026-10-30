package come.asg.lemudroid.app.mobile.feature.settings.general

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import come.asg.lemudroid.R
import come.asg.lemudroid.app.mobile.feature.main.MainRoute
import come.asg.lemudroid.app.mobile.feature.main.navigateToRoute
import come.asg.lemudroid.app.shared.library.LibraryIndexScheduler
import come.asg.lemudroid.app.shared.settings.DataFolderPickerLauncher
import come.asg.lemudroid.app.utils.android.settings.LemuroidCardSettingsGroup
import come.asg.lemudroid.app.utils.android.settings.LemuroidSettingsList
import come.asg.lemudroid.app.utils.android.settings.LemuroidSettingsMenuLink
import come.asg.lemudroid.app.utils.android.settings.LemuroidSettingsPage
import come.asg.lemudroid.app.utils.android.settings.LemuroidSettingsSlider
import come.asg.lemudroid.app.utils.android.settings.LemuroidSettingsSwitch
import come.asg.lemudroid.app.utils.android.settings.TouchButtonAppearanceSettings
import come.asg.lemudroid.app.utils.android.settings.booleanPreferenceState
import come.asg.lemudroid.app.utils.android.settings.indexPreferenceState
import come.asg.lemudroid.app.utils.android.settings.intPreferenceState
import come.asg.lemudroid.app.utils.android.stringListResource
import come.asg.lemudroid.app.mobile.shared.compose.ui.LemuroidThemePreferences
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    navController: NavController,
) {
    val state =
        viewModel.uiState
            .collectAsState(SettingsViewModel.State())
            .value

    val scanInProgress =
        viewModel.directoryScanInProgress
            .collectAsState(false)
            .value

    val indexingInProgress =
        viewModel.indexingInProgress
            .collectAsState(false)
            .value

    LemuroidSettingsPage(modifier = modifier) {
        RomsSettings(
            state = state,
            onChangeFolder = { viewModel.changeLocalStorageFolder() },
            indexingInProgress = indexingInProgress,
            scanInProgress = scanInProgress,
        )
        GeneralSettings()
        InputSettings(navController = navController)
        MiscSettings(
            indexingInProgress = indexingInProgress,
            isSaveSyncSupported = state.isSaveSyncSupported,
            navController = navController,
        )
    }
}

@Composable
private fun MiscSettings(
    indexingInProgress: Boolean,
    isSaveSyncSupported: Boolean,
    navController: NavController,
) {
    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_misc)) },
    ) {
        if (isSaveSyncSupported) {
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.settings_title_save_sync)) },
                subtitle = {
                    Text(text = stringResource(id = R.string.settings_description_save_sync))
                },
                onClick = { navController.navigateToRoute(MainRoute.SETTINGS_SAVE_SYNC) },
            )
        }
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_open_cores_selection)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_open_cores_selection))
            },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_CORES_SELECTION) },
        )
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_display_bios_info)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_display_bios_info))
            },
            enabled = !indexingInProgress,
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_BIOS) },
        )
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_advanced_settings)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_advanced_settings))
            },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_ADVANCED) },
        )
    }
}

@Composable
private fun InputSettings(navController: NavController) {
    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_input)) },
    ) {
        LemuroidSettingsList(
            state =
                indexPreferenceState(
                    R.string.pref_key_haptic_feedback_mode,
                    "press",
                    stringListResource(R.array.pref_key_haptic_feedback_mode_values),
                ),
            title = {
                Text(text = stringResource(id = R.string.settings_title_enable_touch_feedback))
            },
            items = stringListResource(R.array.pref_key_haptic_feedback_mode_display_names),
        )
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_gamepad_settings)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_gamepad_settings))
            },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_INPUT_DEVICES) },
        )
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(R.string.settings_title_touch_layout_editor)) },
            subtitle = { Text(text = stringResource(R.string.settings_description_touch_layout_editor)) },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_TOUCH_LAYOUT) },
        )
        TouchButtonAppearanceSettings()
    }
}

@Composable
private fun GeneralSettings() {
    val hdMode = booleanPreferenceState(R.string.pref_key_hd_mode, false)
    val immersiveMode = booleanPreferenceState(R.string.pref_key_enable_immersive_mode, false)
    val amoledTheme = booleanPreferenceState(R.string.pref_key_amoled_theme, false)

    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_general)) },
    ) {
        LemuroidSettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_autosave, true),
            title = { Text(text = stringResource(id = R.string.settings_title_enable_autosave)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_enable_autosave)) },
        )
        LemuroidSettingsSwitch(
            state = immersiveMode,
            title = { Text(text = stringResource(id = R.string.settings_title_immersive_mode)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_immersive_mode)) },
        )
        LemuroidSettingsSwitch(
            state = amoledTheme,
            title = { Text(text = stringResource(id = R.string.settings_title_amoled_theme)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_amoled_theme)) },
        )
        LemuroidSettingsSwitch(
            state = hdMode,
            title = { Text(text = stringResource(id = R.string.settings_title_hd_mode)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_hd_mode)) },
        )
        LemuroidSettingsSlider(
            enabled = hdMode.value,
            state =
                intPreferenceState(
                    key = stringResource(id = R.string.pref_key_hd_mode_quality),
                    default = 2,
                ),
            steps = 1,
            valueRange = 0f..2f,
            title = { Text(text = stringResource(R.string.settings_title_hd_quality)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_hd_quality)) },
        )
        LemuroidSettingsList(
            enabled = !hdMode.value,
            state =
                indexPreferenceState(
                    R.string.pref_key_shader_filter,
                    "auto",
                    stringListResource(R.array.pref_key_shader_filter_values).toList(),
                ),
            title = { Text(text = stringResource(id = R.string.display_filter)) },
            items = stringListResource(R.array.pref_key_shader_filter_display_names),
        )
        LemuroidSettingsList(
            state =
                indexPreferenceState(
                    R.string.pref_key_viewport_alignment,
                    "center",
                    stringListResource(R.array.pref_key_viewport_alignment_values).toList(),
            ),
            title = { Text(text = stringResource(id = R.string.settings_title_viewport_alignment)) },
            items = stringListResource(R.array.pref_key_viewport_alignment_display_names),
        )
        ThemePrimaryColorSettings()
    }
}

@Composable
private fun ThemePrimaryColorSettings() {
    val context = LocalContext.current
    val showColorPicker = remember { mutableStateOf(false) }
    val openPickerDescription = stringResource(R.string.settings_theme_open_color_picker)
    val primary = LemuroidThemePreferences.getPrimaryColor(context)
    val hsv = remember(primary) { LemuroidThemePreferences.getPrimaryHsv(context) }
    // Keep HSV components independent: converting a neutral/black color back to HSV loses hue.
    val hue = remember { mutableFloatStateOf(hsv[0]) }
    val saturation = remember { mutableFloatStateOf(hsv[1]) }
    val brightness = remember { mutableFloatStateOf(hsv[2]) }
    fun saveColor() {
        LemuroidThemePreferences.setPrimaryColor(
            context,
            Color.hsv(hue.floatValue, saturation.floatValue, brightness.floatValue),
            hue.floatValue,
            saturation.floatValue,
            brightness.floatValue,
        )
    }

    LemuroidCardSettingsGroup(title = { Text(stringResource(R.string.settings_theme_color_title)) }) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier =
                        Modifier.size(48.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .clickable { showColorPicker.value = true }
                            .drawWithCache {
                                onDrawBehind {
                                    drawCircle(
                                        Color.hsv(hue.floatValue, saturation.floatValue, brightness.floatValue),
                                        radius = size.minDimension / 2f,
                                    )
                                }
                            }
                            .semantics {
                                contentDescription = openPickerDescription
                                role = Role.Button
                            },
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_theme_primary_color), style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.settings_theme_generated_automatically),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                stringResource(R.string.settings_theme_picker_prompt),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            )
        }
    }

    if (showColorPicker.value) {
        AlertDialog(
            onDismissRequest = { showColorPicker.value = false },
            title = { Text(stringResource(R.string.settings_theme_color_title)) },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        stringResource(R.string.settings_theme_choose_hue_saturation),
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    )
                    ThemeColorWheel(
                        hue = hue,
                        saturation = saturation,
                        brightness = brightness,
                        onColorChange = { newHue, newSaturation ->
                            hue.floatValue = newHue
                            saturation.floatValue = newSaturation
                        },
                        onColorChangeFinished = { saveColor() },
                    )
                    Text(
                        stringResource(R.string.settings_theme_brightness, (brightness.floatValue * 100).toInt()),
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    )
                    Slider(
                        value = brightness.floatValue,
                        onValueChange = { brightness.floatValue = it },
                        onValueChangeFinished = { saveColor() },
                        valueRange = 0f..1f,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker.value = false }) {
                    Text(stringResource(R.string.settings_theme_color_done))
                }
            },
        )
    }
}

@Composable
private fun ThemeColorWheel(
    hue: State<Float>,
    saturation: State<Float>,
    brightness: State<Float>,
    onColorChange: (Float, Float) -> Unit,
    onColorChangeFinished: () -> Unit,
) {
    val wheelColors =
        remember {
            listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
        }
    val latestOnColorChange = rememberUpdatedState(onColorChange)
    val latestOnColorChangeFinished = rememberUpdatedState(onColorChangeFinished)
    val wheelDescription = stringResource(R.string.settings_theme_color_wheel_description)
    val increaseHueLabel = stringResource(R.string.settings_theme_increase_hue)
    val decreaseHueLabel = stringResource(R.string.settings_theme_decrease_hue)
    val increaseSaturationLabel = stringResource(R.string.settings_theme_increase_saturation)
    val decreaseSaturationLabel = stringResource(R.string.settings_theme_decrease_saturation)

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        val wheelSize = maxWidth.coerceAtMost(280.dp)
        Box(
            modifier =
                Modifier.size(wheelSize)
                    .semantics {
                        contentDescription = wheelDescription
                        customActions = listOf(
                            CustomAccessibilityAction(increaseHueLabel) {
                                latestOnColorChange.value((hue.value + 15f) % 360f, saturation.value)
                                latestOnColorChangeFinished.value()
                                true
                            },
                            CustomAccessibilityAction(decreaseHueLabel) {
                                latestOnColorChange.value((hue.value - 15f + 360f) % 360f, saturation.value)
                                latestOnColorChangeFinished.value()
                                true
                            },
                            CustomAccessibilityAction(increaseSaturationLabel) {
                                latestOnColorChange.value(hue.value, (saturation.value + 0.1f).coerceAtMost(1f))
                                latestOnColorChangeFinished.value()
                                true
                            },
                            CustomAccessibilityAction(decreaseSaturationLabel) {
                                latestOnColorChange.value(hue.value, (saturation.value - 0.1f).coerceAtLeast(0f))
                                latestOnColorChangeFinished.value()
                                true
                            },
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { position ->
                            val selection = selectWheelColor(position, size.width.toFloat(), size.height.toFloat(), 16.dp.toPx())
                            if (selection != null) {
                                latestOnColorChange.value(selection.first, selection.second)
                                latestOnColorChangeFinished.value()
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { position ->
                                selectWheelColor(position, size.width.toFloat(), size.height.toFloat(), 16.dp.toPx())?.let { selection ->
                                    latestOnColorChange.value(selection.first, selection.second)
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                selectWheelColor(
                                    change.position,
                                    size.width.toFloat(),
                                    size.height.toFloat(),
                                    16.dp.toPx(),
                                    allowOutside = true,
                                )?.let { selection ->
                                    latestOnColorChange.value(selection.first, selection.second)
                                }
                            },
                            onDragEnd = { latestOnColorChangeFinished.value() },
                            onDragCancel = { latestOnColorChangeFinished.value() },
                        )
                    }
                    .drawWithCache {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = (size.minDimension / 2f - 16.dp.toPx()).coerceAtLeast(1f)
                        val hueBrush = Brush.sweepGradient(colors = wheelColors, center = center)
                        val saturationBrush =
                            Brush.radialGradient(
                                colors = listOf(Color.White, Color.Transparent),
                                center = center,
                                radius = radius,
                            )
                        onDrawBehind {
                            drawCircle(brush = hueBrush, radius = radius, center = center)
                            drawCircle(brush = saturationBrush, radius = radius, center = center)
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.35f),
                                radius = radius,
                                center = center,
                                style = Stroke(width = 1.dp.toPx()),
                            )

                            // Read selection in the draw phase: pointer moves redraw this layer without
                            // rebuilding the cached hue and saturation gradients.
                            val currentHue = hue.value
                            val currentSaturation = saturation.value.coerceIn(0f, 1f)
                            val currentBrightness = brightness.value
                            val angle = currentHue * (PI.toFloat() / 180f)
                            val markerRadius = radius * currentSaturation
                            val markerCenter =
                                Offset(
                                    center.x + cos(angle) * markerRadius,
                                    center.y + sin(angle) * markerRadius,
                                )
                            drawCircle(Color.Black.copy(alpha = 0.75f), radius = 10.dp.toPx(), center = markerCenter)
                            drawCircle(Color.White, radius = 7.dp.toPx(), center = markerCenter)
                            drawCircle(
                                Color.hsv(currentHue, currentSaturation, currentBrightness),
                                radius = 5.dp.toPx(),
                                center = markerCenter,
                            )
                        }
                    },
        )
    }
}

private fun selectWheelColor(
    position: Offset,
    width: Float,
    height: Float,
    inset: Float,
    allowOutside: Boolean = false,
): Pair<Float, Float>? {
    val dx = position.x - width / 2f
    val dy = position.y - height / 2f
    val radius = (minOf(width, height) / 2f - inset).coerceAtLeast(1f)
    val distance = hypot(dx, dy)
    if (!allowOutside && distance > radius) return null
    val hue = ((atan2(dy, dx) * (180f / PI.toFloat())) + 360f) % 360f
    val saturation = (distance / radius).coerceIn(0f, 1f)
    return hue to saturation
}

@Composable
private fun RomsSettings(
    state: SettingsViewModel.State,
    onChangeFolder: () -> Unit,
    indexingInProgress: Boolean,
    scanInProgress: Boolean,
) {
    val context = LocalContext.current

    val currentDirectory = state.currentDirectory
    val emptyDirectory = stringResource(R.string.none)

    val currentDirectoryName =
        remember(state.currentDirectory) {
            runCatching {
                DocumentFile.fromTreeUri(context, Uri.parse(currentDirectory))?.name
            }.getOrNull() ?: emptyDirectory
        }

    LemuroidCardSettingsGroup(title = { Text(text = stringResource(id = R.string.roms)) }) {
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.directory)) },
            subtitle = { Text(text = currentDirectoryName) },
            onClick = { onChangeFolder() },
            enabled = !indexingInProgress,
        )
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(R.string.settings_title_data_folder)) },
            subtitle = { Text(text = stringResource(R.string.settings_description_data_folder)) },
            onClick = { DataFolderPickerLauncher.pickFolder(context) },
        )
        if (scanInProgress) {
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.stop)) },
                onClick = { LibraryIndexScheduler.cancelLibrarySync(context) },
            )
        } else {
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.rescan)) },
                onClick = { LibraryIndexScheduler.scheduleLibrarySync(context) },
                enabled = !indexingInProgress,
            )
        }
    }
}

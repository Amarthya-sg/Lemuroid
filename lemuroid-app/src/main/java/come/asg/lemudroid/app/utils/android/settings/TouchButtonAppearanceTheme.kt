package come.asg.lemudroid.app.utils.android.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import come.asg.lemudroid.R
import come.asg.lemudroid.app.utils.android.stringListResource
import com.swordfish.touchinput.radial.LemuroidPadTheme

@Composable
fun rememberTouchButtonAppearanceTheme(): LemuroidPadTheme {
    val appearanceIndex =
        indexPreferenceState(
            stringResource(R.string.pref_key_touch_button_appearance),
            "classic",
            stringListResource(R.array.pref_key_touch_button_appearance_values),
        ).value
    val appearance = when (appearanceIndex) {
        1 -> LemuroidPadTheme.APPEARANCE_TRANSPARENT
        2 -> LemuroidPadTheme.APPEARANCE_FROSTED_WHITE
        3 -> LemuroidPadTheme.APPEARANCE_CUSTOM
        else -> LemuroidPadTheme.APPEARANCE_CLASSIC
    }
    val red = intPreferenceState(stringResource(R.string.pref_key_touch_button_red), 120).value.coerceIn(0, 255)
    val green = intPreferenceState(stringResource(R.string.pref_key_touch_button_green), 175).value.coerceIn(0, 255)
    val blue = intPreferenceState(stringResource(R.string.pref_key_touch_button_blue), 255).value.coerceIn(0, 255)
    val opacity = intPreferenceState(stringResource(R.string.pref_key_touch_button_opacity), 75).value.coerceIn(10, 100)
    val color = Color(red / 255f, green / 255f, blue / 255f, opacity / 100f)

    return remember(appearance, color) {
        LemuroidPadTheme(appearance = appearance, customColor = color)
    }
}

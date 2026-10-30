package come.asg.lemudroid.app.mobile.shared.compose.ui

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import come.asg.lemudroid.lib.preferences.SharedPreferencesHelper

object LemuroidThemePreferences {
    private const val PRIMARY_COLOR_KEY = "theme_primary_color"
    private const val PRIMARY_HUE_KEY = "theme_primary_hue"
    private const val PRIMARY_SATURATION_KEY = "theme_primary_saturation"
    private const val PRIMARY_BRIGHTNESS_KEY = "theme_primary_brightness"
    private val defaultPrimary = Color(0xFF4BE37A)
    private val primaryColorState = mutableStateOf<Color?>(null)

    fun getPrimaryColor(context: android.content.Context): Color {
        if (primaryColorState.value == null) {
            val preferences = SharedPreferencesHelper.getSharedPreferences(context)
            primaryColorState.value = Color(preferences.getInt(PRIMARY_COLOR_KEY, defaultPrimary.toArgb()))
        }
        return primaryColorState.value ?: defaultPrimary
    }

    fun getPrimaryHsv(context: android.content.Context): FloatArray {
        val values = FloatArray(3)
        android.graphics.Color.colorToHSV(getPrimaryColor(context).toArgb(), values)
        val preferences = SharedPreferencesHelper.getSharedPreferences(context)
        values[0] = preferences.getFloat(PRIMARY_HUE_KEY, values[0]).coerceIn(0f, 360f)
        values[1] = preferences.getFloat(PRIMARY_SATURATION_KEY, values[1]).coerceIn(0f, 1f)
        values[2] = preferences.getFloat(PRIMARY_BRIGHTNESS_KEY, values[2]).coerceIn(0f, 1f)
        return values
    }

    fun setPrimaryColor(
        context: android.content.Context,
        color: Color,
        hue: Float? = null,
        saturation: Float? = null,
        brightness: Float? = null,
    ) {
        primaryColorState.value = color
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        SharedPreferencesHelper.getSharedPreferences(context)
            .edit()
            .putInt(PRIMARY_COLOR_KEY, color.toArgb())
            .putFloat(PRIMARY_HUE_KEY, (hue ?: hsv[0]).coerceIn(0f, 360f))
            .putFloat(PRIMARY_SATURATION_KEY, (saturation ?: hsv[1]).coerceIn(0f, 1f))
            .putFloat(PRIMARY_BRIGHTNESS_KEY, (brightness ?: hsv[2]).coerceIn(0f, 1f))
            .apply()
    }
}

private val LightColorScheme =
    lightColorScheme(
        primary = md_theme_light_primary,
        onPrimary = md_theme_light_onPrimary,
        primaryContainer = md_theme_light_primaryContainer,
        onPrimaryContainer = md_theme_light_onPrimaryContainer,
        secondary = md_theme_light_secondary,
        onSecondary = md_theme_light_onSecondary,
        secondaryContainer = md_theme_light_secondaryContainer,
        onSecondaryContainer = md_theme_light_onSecondaryContainer,
        tertiary = md_theme_light_tertiary,
        onTertiary = md_theme_light_onTertiary,
        tertiaryContainer = md_theme_light_tertiaryContainer,
        onTertiaryContainer = md_theme_light_onTertiaryContainer,
        error = md_theme_light_error,
        errorContainer = md_theme_light_errorContainer,
        onError = md_theme_light_onError,
        onErrorContainer = md_theme_light_onErrorContainer,
        background = md_theme_light_background,
        onBackground = md_theme_light_onBackground,
        surface = md_theme_light_surface,
        onSurface = md_theme_light_onSurface,
        surfaceVariant = md_theme_light_surfaceVariant,
        onSurfaceVariant = md_theme_light_onSurfaceVariant,
        outline = md_theme_light_outline,
        inverseOnSurface = md_theme_light_inverseOnSurface,
        inverseSurface = md_theme_light_inverseSurface,
        inversePrimary = md_theme_light_inversePrimary,
        surfaceTint = md_theme_light_surfaceTint,
        outlineVariant = md_theme_light_outlineVariant,
        scrim = md_theme_light_scrim,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = md_theme_dark_primary,
        onPrimary = md_theme_dark_onPrimary,
        primaryContainer = md_theme_dark_primaryContainer,
        onPrimaryContainer = md_theme_dark_onPrimaryContainer,
        secondary = md_theme_dark_secondary,
        onSecondary = md_theme_dark_onSecondary,
        secondaryContainer = md_theme_dark_secondaryContainer,
        onSecondaryContainer = md_theme_dark_onSecondaryContainer,
        tertiary = md_theme_dark_tertiary,
        onTertiary = md_theme_dark_onTertiary,
        tertiaryContainer = md_theme_dark_tertiaryContainer,
        onTertiaryContainer = md_theme_dark_onTertiaryContainer,
        error = md_theme_dark_error,
        errorContainer = md_theme_dark_errorContainer,
        onError = md_theme_dark_onError,
        onErrorContainer = md_theme_dark_onErrorContainer,
        background = md_theme_dark_background,
        onBackground = md_theme_dark_onBackground,
        surface = md_theme_dark_surface,
        onSurface = md_theme_dark_onSurface,
        surfaceVariant = md_theme_dark_surfaceVariant,
        onSurfaceVariant = md_theme_dark_onSurfaceVariant,
        outline = md_theme_dark_outline,
        inverseOnSurface = md_theme_dark_inverseOnSurface,
        inverseSurface = md_theme_dark_inverseSurface,
        inversePrimary = md_theme_dark_inversePrimary,
        surfaceTint = md_theme_dark_surfaceTint,
        outlineVariant = md_theme_dark_outlineVariant,
        scrim = md_theme_dark_scrim,
    )

private val HtmlDarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF4BE37A),
        onPrimary = Color(0xFF00391A),
        primaryContainer = Color(0xFF1F4D2E),
        onPrimaryContainer = Color(0xFFAAF6BF),
        secondary = Color(0xFFB9C1B5),
        onSecondary = Color(0xFF222620),
        secondaryContainer = Color(0xFF2C312A),
        onSecondaryContainer = Color(0xFFE6E9E2),
        background = Color(0xFF141713),
        onBackground = Color(0xFFE6E9E2),
        surface = Color(0xFF1B1E1A),
        onSurface = Color(0xFFE6E9E2),
        surfaceVariant = Color(0xFF222620),
        onSurfaceVariant = Color(0xFFB9C1B5),
        outline = Color(0xFF7D867A),
        outlineVariant = Color(0xFF2E332C),
        scrim = Color(0xFF000000),
    )

private val AmoledColorScheme =
    HtmlDarkColorScheme.copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceVariant = Color(0xFF101010),
    )

private fun readableContentColor(background: Color): Color =
    if (background.luminance() > 0.179f) Color.Black else Color.White

private fun generatedDarkColorScheme(
    primary: Color,
    amoled: Boolean,
): androidx.compose.material3.ColorScheme {
    val background = if (amoled) Color.Black else Color(0xFF141713)
    val surface = if (amoled) Color.Black else Color(0xFF1B1E1A)
    val surfaceVariant = if (amoled) Color(0xFF101010) else Color(0xFF222620)
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(primary.toArgb(), hsv)
    val secondary = Color.hsv((hsv[0] + 145f) % 360f, (hsv[1] * 0.55f).coerceAtLeast(0.28f), hsv[2] * 0.82f)
    val tertiary = Color.hsv((hsv[0] + 235f) % 360f, (hsv[1] * 0.48f).coerceAtLeast(0.25f), hsv[2] * 0.78f)
    val primaryContainer = primary.copy(alpha = 0.32f).compositeOver(surfaceVariant)
    val secondaryContainer = secondary.copy(alpha = 0.28f).compositeOver(surfaceVariant)
    val onPrimary = readableContentColor(primary)
    val onPrimaryContainer = readableContentColor(primaryContainer)

    return HtmlDarkColorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = readableContentColor(secondary),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = readableContentColor(secondaryContainer),
        tertiary = tertiary,
        onTertiary = readableContentColor(tertiary),
        background = background,
        surface = surface,
        surfaceVariant = surfaceVariant,
        outline = primary.copy(alpha = 0.58f).compositeOver(surfaceVariant),
        outlineVariant = primary.copy(alpha = 0.18f).compositeOver(surfaceVariant),
    )
}

private fun generatedLightColorScheme(
    base: androidx.compose.material3.ColorScheme,
    primary: Color,
): androidx.compose.material3.ColorScheme {
    val primaryContainer = primary.copy(alpha = 0.14f).compositeOver(base.surface)
    return base.copy(
        primary = primary,
        onPrimary = readableContentColor(primary),
        primaryContainer = primaryContainer,
        onPrimaryContainer = readableContentColor(primaryContainer),
    )
}

@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val amoledTheme =
        SharedPreferencesHelper.getSharedPreferences(context)
            .getBoolean("amoled_theme", false)
    val primaryColor = LemuroidThemePreferences.getPrimaryColor(context)
    val colors =
        when {
            darkTheme -> generatedDarkColorScheme(primaryColor, amoledTheme)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                generatedLightColorScheme(dynamicLightColorScheme(context), primaryColor)
            else -> generatedLightColorScheme(LightColorScheme, primaryColor)
        }

    MaterialTheme(colorScheme = colors) {
        content()
    }
}

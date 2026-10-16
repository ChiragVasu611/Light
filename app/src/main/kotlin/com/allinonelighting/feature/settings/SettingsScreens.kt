package com.allinonelighting.feature.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.allinonelighting.BuildConfig
import com.allinonelighting.R
import com.allinonelighting.ui.components.AppRow
import com.allinonelighting.ui.theme.FeatureAccent
import com.allinonelighting.ui.components.ScreenContent
import com.allinonelighting.ui.theme.LocalSpacing

/**
 * SCR-SETTINGS.
 *
 * There is deliberately no Audio group: the app produces no sound and has no
 * vibration feature, so such a group would be controls over nothing
 * (FR-SET-01, ADR-009, asserted by T-SET-01).
 */
@Composable
fun SettingsScreen(
    onOpenDisplay: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenAbout: () -> Unit,
    onReplayIntro: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    ScreenContent(
        modifier = modifier,
    ) {
        SettingsGroup(stringResource(R.string.settings_group_general)) {
            AppRow(
                title = stringResource(R.string.settings_show_intro),
                subtitle = stringResource(R.string.settings_show_intro_subtitle),
                icon = Icons.Filled.Slideshow,
                accent = FeatureAccent.Neutral,
                onClick = onReplayIntro,
            )
        }
        SettingsGroup(stringResource(R.string.settings_group_display)) {
            AppRow(
                title = stringResource(R.string.settings_display_title),
                subtitle = stringResource(R.string.settings_display_subtitle),
                icon = Icons.Filled.Brightness6,
                accent = FeatureAccent.Simulator,
                onClick = onOpenDisplay,
            )
        }
        SettingsGroup(stringResource(R.string.settings_group_privacy)) {
            AppRow(
                title = stringResource(R.string.settings_permissions_title),
                subtitle = stringResource(R.string.settings_permissions_subtitle),
                icon = Icons.Filled.PrivacyTip,
                accent = FeatureAccent.Neutral,
                onClick = onOpenPermissions,
            )
        }
        SettingsGroup(stringResource(R.string.settings_group_about)) {
            AppRow(
                title = stringResource(R.string.settings_about_title),
                subtitle = stringResource(R.string.settings_about_subtitle),
                icon = Icons.Filled.Info,
                accent = FeatureAccent.Neutral,
                onClick = onOpenAbout,
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
        )
        content()
    }
}

/**
 * SCR-SETTINGS-PERMISSIONS — v1.0 requests no runtime permissions, and this screen
 * says so explicitly (FR-SET-04).
 */
@Composable
fun PermissionsScreen(modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val context = LocalContext.current

    ScreenContent(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.permissions_heading),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.permissions_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.permissions_camera),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.permissions_other),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedButton(
            onClick = {
                // Wrapped: a device with no settings activity must not crash the app.
                runCatching {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        ),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.permissions_open_settings))
        }
    }
}

/** SCR-ABOUT. Version comes from BuildConfig, never a literal (AC-SET-03). */
@Composable
fun AboutScreen(
    onOpenLicences: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    ScreenContent(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(
                R.string.about_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppRow(
            title = stringResource(R.string.about_licences),
            subtitle = null,
            icon = Icons.Filled.Info,
            accent = FeatureAccent.Neutral,
            onClick = onOpenLicences,
        )
    }
}

package com.allinonelighting.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.allinonelighting.R
import com.allinonelighting.ui.theme.LocalRadius
import com.allinonelighting.ui.theme.LocalSizes
import com.allinonelighting.ui.theme.LocalSpacing
import com.allinonelighting.ui.theme.LocalSurfaces

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
) {
    Home(Routes.HOME, Icons.Filled.Home, R.string.nav_home),
    Simulator(Routes.SIM_GALLERY, Icons.Filled.ViewInAr, R.string.nav_3d),
    Tools(Routes.TOOLS, Icons.Filled.Build, R.string.nav_tools),
    Settings(Routes.SETTINGS, Icons.Filled.Settings, R.string.nav_settings),
}

/** CMP-BOTTOMNAV — docs/UI_DESIGN_SYSTEM.md §6. */
@Composable
fun AppBottomNavigation(
    currentRoute: String?,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val radius = LocalRadius.current
    val sizes = LocalSizes.current
    val surfaces = LocalSurfaces.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(surfaces.bottomNav.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(sizes.hairline)
                .background(surfaces.bottomNav.border),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .heightIn(min = sizes.bottomNavHeight)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopLevelDestination.entries.forEach { destination ->
                val selected = currentRoute == destination.route
                val tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = sizes.minTouchTarget)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onNavigate(destination) },
                        )
                        .padding(vertical = spacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(sizes.navIndicatorWidth)
                            .height(sizes.navIndicatorHeight)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                },
                                RoundedCornerShape(radius.pill),
                            ),
                    )
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier
                            .padding(top = spacing.xxs)
                            .size(sizes.iconDefault),
                    )
                    Text(
                        text = stringResource(destination.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

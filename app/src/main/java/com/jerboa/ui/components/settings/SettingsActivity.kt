@file:OptIn(ExperimentalMaterial3Api::class)

package com.jerboa.ui.components.settings

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.jerboa.db.AccountViewModel
import com.jerboa.ui.components.common.SimpleTopAppBar
import com.jerboa.ui.components.common.getCurrentAccount
import com.jerboa.ui.theme.Shapes
import com.jerboa.ui.theme.muted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsActivity(
    navController: NavController,
    accountViewModel: AccountViewModel
) {
    Log.d("jerboa", "Got to settings activity")

    val account = getCurrentAccount(accountViewModel = accountViewModel)
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SimpleTopAppBar(text = "Settings", navController = navController)
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                SettingsMenuLink(
                    title = {
                        Column {
                            Text("Look and feel")
                            Text(
                                "Theme related settings",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "TODO"
                        )
                    },
                    modifier = Modifier.clip(Shapes.large),
                    onClick = { navController.navigate("lookAndFeel") }
                )
                account?.also { acct ->
                    SettingsMenuLink(
                        title = {
                            Column {
                                Text("Current account")
                                Text(
                                    "Settings for ${acct.name}@${acct.instance}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.ManageAccounts,
                                contentDescription = "TODO"
                            )
                        },
                        modifier = Modifier.clip(Shapes.large),
                        onClick = { navController.navigate("accountSettings") }
                    )
                }
                SettingsMenuLink(
                    title = {
                        Column {
                            Text("About")
                            Text(
                                "Additional application info",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "TODO"
                        )
                    },
                    modifier = Modifier.clip(Shapes.large),
                    onClick = { navController.navigate("about") }
                )
            }
        }
    )
}

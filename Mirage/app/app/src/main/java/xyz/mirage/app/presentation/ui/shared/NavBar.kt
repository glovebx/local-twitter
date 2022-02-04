package xyz.mirage.app.presentation.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import xyz.mirage.app.R
import xyz.mirage.app.presentation.navigation.Screen

@Composable
fun NavBar(
    navController: NavController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomAppBar(
        backgroundColor = Color.Transparent,
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            BottomBarIcon(
                navController = navController,
                route = Screen.Home.route,
                icon = if (currentRoute == Screen.Home.route) R.drawable.ic_home_selected else R.drawable.ic_home,
                description = "Home"
            )

            BottomBarIcon(
                navController = navController,
                route = Screen.Search.route,
                icon = if (currentRoute == Screen.Search.route) R.drawable.ic_search_selected else R.drawable.ic_search,
                description = "Search"
            )

            BottomBarIcon(
                navController = navController,
                route = Screen.Account.route,
                icon = if (currentRoute == Screen.Account.route) Icons.Filled.Person else Icons.Outlined.Person,
                isSelected = currentRoute == Screen.Account.route,
                description = "Account"
            )
        }
    }
}

@Composable
private fun BottomBarIcon(
    navController: NavController,
    route: String,
    icon: Int,
    description: String
) {
    IconButton(onClick = {
        navController.navigate(route) {
            navController.graph.startDestinationRoute?.let { route ->
                popUpTo(route) {
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }) {
        Image(
            painter = painterResource(icon),
            contentDescription = description,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun BottomBarIcon(
    navController: NavController,
    route: String,
    icon: ImageVector,
    description: String,
    isSelected: Boolean,
) {
    IconButton(onClick = {
        navController.navigate(route) {
            navController.graph.startDestinationRoute?.let { route ->
                popUpTo(route) {
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }) {
        Icon(
            tint = if (isSelected) Color(0xff4AA0EC) else Color(0xff9C9C9C),
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(28.dp)
        )
    }
}
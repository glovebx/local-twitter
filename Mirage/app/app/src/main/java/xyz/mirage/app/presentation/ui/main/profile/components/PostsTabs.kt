package xyz.mirage.app.presentation.ui.main.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import xyz.mirage.app.presentation.ui.shared.CustomDivider

@Composable
fun PostsTabs(
    currentTab: Int,
    onChangeTab: (Int) -> Unit,
    isDarkTheme: Boolean
) {
    val tabData = listOf("Posts", "Media", "Likes")

    Column {
        TabRow(
            selectedTabIndex = currentTab,
            backgroundColor = Color.Transparent
        ) {
            tabData.forEachIndexed { index, text ->
                Tab(
                    selected = currentTab == index,
                    onClick = {
                        onChangeTab(index)
                    },
                    text = { Text(text = text) }
                )
            }
        }

        CustomDivider(
            isDarkTheme = isDarkTheme
        )
    }
}
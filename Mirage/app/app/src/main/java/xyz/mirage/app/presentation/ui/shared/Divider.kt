package xyz.mirage.app.presentation.ui.shared

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun CustomDivider(
    isDarkTheme: Boolean
) {
    Divider(
        color = (if (isDarkTheme) Color.White else Color(0xFF333333)).copy(alpha = 0.3f),
        thickness = 0.2.dp
    )
}

@Composable
fun DividerWithSpace(
    isDarkTheme: Boolean
) {
    Spacer(modifier = Modifier.height(10.dp))

    CustomDivider(isDarkTheme)

    Spacer(modifier = Modifier.height(10.dp))
}
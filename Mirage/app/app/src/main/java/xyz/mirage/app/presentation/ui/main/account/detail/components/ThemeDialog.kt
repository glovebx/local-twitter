package xyz.mirage.app.presentation.ui.main.account.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ThemeDialog(
    showThemeDialog: MutableState<Boolean>,
    isDarkTheme: Boolean,
    toggleTheme: () -> Unit,
) {
    if (showThemeDialog.value) {
        Dialog(onDismissRequest = { showThemeDialog.value = false }) {
            Surface(
                modifier = Modifier.width(300.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Pick a theme",
                        fontSize = 22.sp,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ThemeOption(
                        text = "Light",
                        selected = !isDarkTheme,
                        onSelect = { toggleTheme() }
                    )

                    ThemeOption(
                        text = "Dark",
                        selected = isDarkTheme,
                        onSelect = { toggleTheme() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Button(
        onClick = onSelect,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .height(34.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onSelect,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text)
        }
    }
}
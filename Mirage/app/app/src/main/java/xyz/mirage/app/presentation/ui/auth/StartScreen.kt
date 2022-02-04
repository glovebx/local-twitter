package xyz.mirage.app.presentation.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import xyz.mirage.app.presentation.core.theme.LiteAppTheme
import xyz.mirage.app.presentation.core.theme.PrimaryColor
import xyz.mirage.app.presentation.navigation.Screen
import xyz.mirage.app.presentation.ui.auth.components.AuthButton
import xyz.mirage.app.presentation.ui.auth.components.Logo

@Composable
fun StartScreen(
    isDarkTheme: Boolean,
    navController: NavController,
) {
    LiteAppTheme(
        darkTheme = isDarkTheme,
    ) {
        Scaffold {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Logo()

                Column {
                    InfoText()

                    Spacer(modifier = Modifier.size(30.dp))

                    AuthButton(
                        text = "Create Account",
                        handleClick = { navController.navigate(Screen.Register.route) },
                        isEnabled = true
                    )
                }

                LoginInfo(
                    handleClick = { navController.navigate(Screen.Login.route) }
                )
            }
        }
    }
}

@Composable
private fun InfoText() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 16.dp),
    ) {
        Text(
            text = "See what's \nhappening in the \nworld right now!",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
        )
    }
}

@Composable
private fun LoginInfo(
    handleClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable { handleClick() },
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text = "Have an account already?")
        Spacer(modifier = Modifier.size(10.dp))
        Text(text = "Log in", color = PrimaryColor)
    }
}
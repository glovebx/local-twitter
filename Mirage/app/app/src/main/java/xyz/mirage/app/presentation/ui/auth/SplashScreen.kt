package xyz.mirage.app.presentation.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import xyz.mirage.app.R
import xyz.mirage.app.presentation.core.theme.LiteAppTheme

@Composable
fun SplashScreen(
    isDarkTheme: Boolean,
) {
    LiteAppTheme(
        darkTheme = isDarkTheme
    ) {
        Scaffold {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_twitter),
                    contentDescription = "Logo",
                    modifier = Modifier.size(60.dp)
                )
            }
        }
    }
}
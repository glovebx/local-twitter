package xyz.mirage.app.presentation.ui.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import xyz.mirage.app.R
import xyz.mirage.app.presentation.core.theme.PrimaryColor

@Composable
fun AuthTopBar(
    handleBack: () -> Unit
) {
    TopAppBar(
        backgroundColor = Color.Transparent,
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 70.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_twitter),
                    contentDescription = "Logo",
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = { handleBack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = PrimaryColor
                )
            }
        },
        elevation = 0.dp
    )
}
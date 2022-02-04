package xyz.mirage.app.presentation.ui.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import xyz.mirage.app.R

@Composable
fun Logo() {
    Row(
        modifier = Modifier
            .height(56.dp)
            .padding(start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_twitter),
            contentDescription = "Icon",
            modifier = Modifier.size(22.dp)
        )
    }
}
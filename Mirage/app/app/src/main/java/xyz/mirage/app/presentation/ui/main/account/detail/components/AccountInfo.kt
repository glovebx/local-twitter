package xyz.mirage.app.presentation.ui.main.account.detail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import xyz.mirage.app.R
import xyz.mirage.app.business.domain.models.Account
import xyz.mirage.app.presentation.core.theme.PrimaryColor

@ExperimentalCoilApi
@Composable
fun AccountInfo(
    user: Account,
    isDarkTheme: Boolean,
    imageLoader: ImageLoader,
    openDialog: () -> Unit,
    handleNavigation: () -> Unit
) {
    Box {
        user.banner?.let { url ->
            UserBanner(
                url = url,
                username = user.username,
                imageLoader = imageLoader
            )
        } ?: Box(
            modifier = Modifier
                .background(PrimaryColor)
                .height(150.dp)
                .fillMaxWidth(),
        )

        UserAvatar(
            url = user.image,
            modifier = Modifier.align(Alignment.BottomStart),
            isDarkTheme = isDarkTheme,
            imageLoader = imageLoader
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset((-20).dp, 50.dp),
        ) {
            Button(
                contentPadding = PaddingValues(all = 0.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = ButtonDefaults.elevation(0.dp),
                onClick = { openDialog() }
            ) {
                Image(
                    contentDescription = "Theme",
                    painter = painterResource(R.drawable.ic_theme),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            OutlinedButton(
                onClick = { handleNavigation() },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, PrimaryColor)
            ) {
                Text(text = "Edit Profile", color = PrimaryColor)
            }

        }
    }
}

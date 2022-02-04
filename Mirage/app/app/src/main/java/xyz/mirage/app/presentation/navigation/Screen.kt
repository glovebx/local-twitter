package xyz.mirage.app.presentation.navigation

sealed class Screen(
    val route: String,
) {
    // Auth
    object Splash : Screen("splash")

    object Start : Screen("start")

    object Login : Screen("login")

    object Register : Screen("register")

    // Post
    object Home : Screen("home")

    object PostDetail : Screen("postDetail")

    object CreatePost : Screen("create")

    // Profile
    object Search : Screen("search")

    object Profile : Screen("profile")

    // Account
    object Account : Screen("account")

    object UpdateAccount : Screen("updateAccount")
}
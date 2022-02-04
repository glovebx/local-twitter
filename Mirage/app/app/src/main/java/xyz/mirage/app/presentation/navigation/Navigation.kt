package xyz.mirage.app.presentation.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import xyz.mirage.app.business.datasources.datastore.SettingsDataStore
import xyz.mirage.app.presentation.MainActivity
import xyz.mirage.app.presentation.core.util.ConnectivityManager
import xyz.mirage.app.presentation.session.SessionManager
import xyz.mirage.app.presentation.ui.auth.SplashScreen
import xyz.mirage.app.presentation.ui.auth.StartScreen
import xyz.mirage.app.presentation.ui.auth.login.LoginScreen
import xyz.mirage.app.presentation.ui.auth.login.LoginViewModel
import xyz.mirage.app.presentation.ui.auth.register.RegisterScreen
import xyz.mirage.app.presentation.ui.auth.register.RegisterViewModel
import xyz.mirage.app.presentation.ui.main.account.detail.AccountScreen
import xyz.mirage.app.presentation.ui.main.account.detail.AccountViewModel
import xyz.mirage.app.presentation.ui.main.account.update.UpdateAccountScreen
import xyz.mirage.app.presentation.ui.main.account.update.UpdateAccountViewModel
import xyz.mirage.app.presentation.ui.main.home.create.CreatePostScreen
import xyz.mirage.app.presentation.ui.main.home.create.CreatePostViewModel
import xyz.mirage.app.presentation.ui.main.home.detail.PostDetailScreen
import xyz.mirage.app.presentation.ui.main.home.detail.PostDetailViewModel
import xyz.mirage.app.presentation.ui.main.home.list.HomeScreen
import xyz.mirage.app.presentation.ui.main.home.list.PostListViewModel
import xyz.mirage.app.presentation.ui.main.profile.ProfileScreen
import xyz.mirage.app.presentation.ui.main.profile.ProfileViewModel
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewManager
import xyz.mirage.app.presentation.ui.main.search.SearchScreen
import xyz.mirage.app.presentation.ui.main.search.SearchViewModel

@ExperimentalAnimationApi
@ExperimentalCoilApi
@ExperimentalComposeUiApi
@Composable
fun Navigation(
    mainActivity: MainActivity,
    settingsDataStore: SettingsDataStore,
    connectivityManager: ConnectivityManager,
    sessionManager: SessionManager,
    refreshViewManager: RefreshViewManager,
    imageLoader: ImageLoader,
) {
    val navController = rememberAnimatedNavController()
    val scaffoldState = rememberScaffoldState()

    sessionManager.state.observe(mainActivity) { state ->
        val route = when {
            state.uid != null -> Screen.Home.route
            state.didCheckForPreviousAuthUser -> Screen.Start.route
            else -> Screen.Splash.route
        }

        navController.navigate(route) {
            popUpTo(0)
        }
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // -------------- Auth Routes -----------------
        composable(route = Screen.Splash.route) {
            SplashScreen(
                isDarkTheme = settingsDataStore.isDark.value
            )
        }

        composable(
            route = Screen.Start.route,
            exitTransition = { _, _ ->
                slideExitTransition()
            },
            popEnterTransition = { _, _ ->
                slidePopEnterTransition()
            },
        ) {
            StartScreen(
                isDarkTheme = settingsDataStore.isDark.value,
                navController = navController,
            )
        }

        composable(
            route = Screen.Register.route,
            enterTransition = { _, _ ->
                slideEnterTransition()
            },
            popExitTransition = { _, _ ->
                slidePopExitTransition()
            }
        ) { navBackStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, navBackStackEntry)
            val viewModel: RegisterViewModel =
                viewModel(
                    key = "RegisterViewModel",
                    factory = factory,
                    viewModelStoreOwner = mainActivity
                )

            RegisterScreen(
                isDarkTheme = settingsDataStore.isDark.value,
                isNetworkAvailable = connectivityManager.isNetworkAvailable.value,
                navController = navController,
                scaffoldState = scaffoldState,
                state = viewModel.state.value,
                onTriggerEvent = viewModel::onTriggerEvent
            )
        }

        composable(
            route = Screen.Login.route,
            enterTransition = { _, _ ->
                slideEnterTransition()
            },
            popExitTransition = { _, _ ->
                slidePopExitTransition()
            }
        ) { navBackStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, navBackStackEntry)
            val viewModel: LoginViewModel =
                viewModel(
                    key = "LoginViewModel",
                    factory = factory,
                    viewModelStoreOwner = mainActivity
                )

            LoginScreen(
                isDarkTheme = settingsDataStore.isDark.value,
                isNetworkAvailable = connectivityManager.isNetworkAvailable.value,
                navController = navController,
                scaffoldState = scaffoldState,
                state = viewModel.state.value,
                onTriggerEvent = viewModel::onTriggerEvent
            )
        }

        // -------------- Post Routes -----------------
        composable(
            route = Screen.Home.route,
            exitTransition = { _, target ->
                when (target.destination.route) {
                    Screen.PostDetail.route + "/{postId}",
                    Screen.Profile.route + "/{username}",
                    Screen.CreatePost.route ->
                        slideExitTransition()
                    else -> null
                }
            },
            popEnterTransition = { initial, _ ->
                when (initial.destination.route) {
                    Screen.PostDetail.route + "/{postId}",
                    Screen.Profile.route + "/{username}",
                    Screen.CreatePost.route ->
                        slidePopEnterTransition()
                    else -> null
                }
            },
        ) { navBackStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, navBackStackEntry)
            val viewModel: PostListViewModel =
                viewModel(
                    key = "PostListViewModel",
                    factory = factory,
                    viewModelStoreOwner = mainActivity
                )

            HomeScreen(
                isDarkTheme = settingsDataStore.isDark.value,
                state = viewModel.state.value,
                onTriggerEvent = viewModel::onTriggerEvent,
                isNetworkAvailable = connectivityManager.isNetworkAvailable.value,
                scaffoldState = scaffoldState,
                navController = navController,
                authId = sessionManager.state.value?.uid ?: "",
                refreshViewManager = refreshViewManager,
                imageLoader = imageLoader,
            )
        }

        composable(
            route = Screen.PostDetail.route + "/{postId}",
            arguments = listOf(navArgument("postId") {
                type = NavType.StringType
            }),
            exitTransition = { _, _ ->
                slideExitTransition()
            },
            enterTransition = { _, _ ->
                slideEnterTransition()
            },
            popExitTransition = { _, _ ->
                slidePopExitTransition()
            },
            popEnterTransition = { _, _ ->
                slidePopEnterTransition()
            },
        ) { navBackStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, navBackStackEntry)
            val viewModel: PostDetailViewModel =
                viewModel(key = "PostDetailViewModel", factory = factory)

            PostDetailScreen(
                isDarkTheme = settingsDataStore.isDark.value,
                postId = navBackStackEntry.arguments?.getString("postId"),
                isNetworkAvailable = connectivityManager.isNetworkAvailable.value,
                state = viewModel.state.value,
                scaffoldState = scaffoldState,
                onTriggerEvent = viewModel::onTriggerEvent,
                navController = navController,
                authId = sessionManager.state.value?.uid ?: "",
                refreshViewManager = refreshViewManager,
                imageLoader = imageLoader
            )
        }

        composable(
            route = Screen.CreatePost.route,
            enterTransition = { _, _ ->
                slideEnterTransition()
            },
            popExitTransition = { _, _ ->
                slidePopExitTransition()
            }
        ) { navBackStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, navBackStackEntry)
            val viewModel: CreatePostViewModel =
                viewModel(key = "CreatePostViewModel", factory = factory)

            CreatePostScreen(
                isDarkTheme = settingsDataStore.isDark.value,
                state = viewModel.state.value,
                onTriggerEvent = viewModel::onTriggerEvent,
                isNetworkAvailable = connectivityManager.isNetworkAvailable.value,
                scaffoldState = scaffoldState,
                navController = navController,
                imageLoader = imageLoader
            )
        }


        // -------------- Search Routes -----------------
        composable(
            route = Screen.Search.route,
            exitTransition = { _, target ->
                when (target.destination.route) {
                    Screen.PostDetail.route + "/{postId}",
                    Screen.Profile.route + "/{username}" ->
                        slideExitTransition()
                    else -> null
                }
            },
            popEnterTransition = { initial, _ ->
                when (initial.destination.route) {
                    Screen.PostDetail.route + "/{postId}",
                    Screen.Profile.route + "/{username}" ->
                        slidePopEnterTransition()
                    else -> null
                }
            },
        ) { navBackStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, navBackStackEntry)
            val viewModel: SearchViewModel =
                viewModel(
                    key = "SearchViewModel",
                    factory = factory,
                    viewModelStoreOwner = mainActivity
                )

            SearchScreen(
                isDarkTheme = settingsDataStore.isDark.value,
                state = viewModel.state.value,
                onTriggerEvent = viewModel::onTriggerEvent,
                isNetworkAvailable = connectivityManager.isNetworkAvailable.value,
                scaffoldState = scaffoldState,
                navController = navController,
                authId = sessionManager.state.value?.uid ?: "",
                refreshViewManager = refreshViewManager,
                imageLoader = imageLoader,
            )
        }

        composable(
            route = Screen.Profile.route + "/{username}",
            arguments = listOf(navArgument("username") {
                type = NavType.StringType
            }),
            exitTransition = { _, _ ->
                slideExitTransition()
            },
            enterTransition = { _, _ ->
                slideEnterTransition()
            },
            popExitTransition = { _, _ ->
                slidePopExitTransition()
            },
            popEnterTransition = { _, _ ->
                slidePopEnterTransition()
            },
        ) { navBackStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, navBackStackEntry)
            val viewModel: ProfileViewModel =
                viewModel(key = "ProfileViewModel", factory = factory)

            ProfileScreen(
                isDarkTheme = settingsDataStore.isDark.value,
                username = navBackStackEntry.arguments?.getString("username"),
                onTriggerEvent = viewModel::onTriggerEvent,
                state = viewModel.state.value,
                navController = navController,
                scaffoldState = scaffoldState,
                isNetworkAvailable = connectivityManager.isNetworkAvailable.value,
                authId = sessionManager.state.value?.uid ?: "",
                refreshViewManager = refreshViewManager,
                imageLoader = imageLoader,
            )
        }

        // -------------- Account Routes -----------------
        composable(
            route = Screen.Account.route,
            exitTransition = { _, target ->
                when (target.destination.route) {
                    Screen.UpdateAccount.route + "/{id}",
                    Screen.PostDetail.route + "/{postId}",
                    Screen.Profile.route + "/{username}" ->
                        slideExitTransition()
                    else -> null
                }
            },
            popEnterTransition = { initial, _ ->
                when (initial.destination.route) {
                    Screen.UpdateAccount.route + "/{id}",
                    Screen.PostDetail.route + "/{postId}",
                    Screen.Profile.route + "/{username}" ->
                        slidePopEnterTransition()
                    else -> null
                }
            },
        ) { navBackStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, navBackStackEntry)
            val viewModel: AccountViewModel =
                viewModel(
                    key = "AccountViewModel",
                    factory = factory,
                    viewModelStoreOwner = mainActivity
                )

            AccountScreen(
                isDarkTheme = settingsDataStore.isDark.value,
                state = viewModel.state.value,
                onTriggerEvent = viewModel::onTriggerEvent,
                navController = navController,
                settingsDataStore = settingsDataStore,
                scaffoldState = scaffoldState,
                isNetworkAvailable = connectivityManager.isNetworkAvailable.value,
                refreshViewManager = refreshViewManager,
                imageLoader = imageLoader,
            )
        }

        composable(
            route = Screen.UpdateAccount.route + "/{id}",
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
            }),
            enterTransition = { _, _ ->
                slideEnterTransition()
            },
            popExitTransition = { _, _ ->
                slidePopExitTransition()
            }
        ) { navBackStackEntry ->
            val factory = HiltViewModelFactory(LocalContext.current, navBackStackEntry)
            val viewModel: UpdateAccountViewModel =
                viewModel(key = "UpdateAccountViewModel", factory = factory)

            UpdateAccountScreen(
                isDarkTheme = settingsDataStore.isDark.value,
                id = navBackStackEntry.arguments?.getString("id"),
                state = viewModel.state.value,
                onTriggerEvent = viewModel::onTriggerEvent,
                navController = navController,
                scaffoldState = scaffoldState,
                isNetworkAvailable = connectivityManager.isNetworkAvailable.value,
                imageLoader = imageLoader
            )
        }
    }
}
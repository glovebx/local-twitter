package xyz.mirage.app.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.hilt.android.AndroidEntryPoint
import xyz.mirage.app.business.datasources.datastore.SettingsDataStore
import xyz.mirage.app.presentation.core.util.ConnectivityManager
import xyz.mirage.app.presentation.navigation.Navigation
import xyz.mirage.app.presentation.session.SessionManager
import xyz.mirage.app.presentation.ui.main.refresh.RefreshViewManager
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalAnimationApi
@ExperimentalCoilApi
@ExperimentalComposeUiApi
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var refreshViewManager: RefreshViewManager

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onStart() {
        super.onStart()
        connectivityManager.registerConnectionObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterConnectionObserver(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Navigation(
                mainActivity = this,
                settingsDataStore = settingsDataStore,
                connectivityManager = connectivityManager,
                sessionManager = sessionManager,
                refreshViewManager = refreshViewManager,
                imageLoader = imageLoader,
            )
        }
    }
}

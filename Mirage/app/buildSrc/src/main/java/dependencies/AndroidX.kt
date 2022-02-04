package dependencies

object AndroidX {

    const val core_ktx = "androidx.core:core-ktx:${Versions.core_ktx}"
    const val app_compat = "androidx.appcompat:appcompat:${Versions.app_compat}"

    const val compose_ui = "androidx.compose.ui:ui:${Versions.compose}"
    const val compose_foundation = "androidx.compose.foundation:foundation:${Versions.compose}"
    const val compose_material = "androidx.compose.material:material:${Versions.compose}"
    const val compose_icons_core = "androidx.compose.material:material-icons-core:${Versions.compose}"
    const val compose_icons_extended = "androidx.compose.material:material-icons-extended:${Versions.compose}"
    const val compose_ui_tooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"

    const val navigation_compose = "androidx.navigation:navigation-compose:${Versions.nav_compose}"
    const val compose_activity = "androidx.activity:activity-compose:${Versions.compose_activity}"
    const val navigation_hilt = "androidx.hilt:hilt-navigation:${Versions.hilt_navigation}"

    const val room_runtime = "androidx.room:room-runtime:${Versions.room}"
    const val room_ktx = "androidx.room:room-ktx:${Versions.room}"

    const val datastore = "androidx.datastore:datastore-preferences:${Versions.datastore}"

    const val hilt_lifecycle_viewmodel ="androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hilt_lifecycle_viewmodel}"
}
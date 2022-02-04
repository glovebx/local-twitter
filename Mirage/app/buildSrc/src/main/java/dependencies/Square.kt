package dependencies

object Square{
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofit_moshi = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    const val okHttp = "com.squareup.okhttp3:okhttp:${Versions.okHttp}"
    const val httpLogging = "com.squareup.okhttp3:logging-interceptor:${Versions.okHttp}"
    const val leak_canary = "com.squareup.leakcanary:leakcanary-android:${Versions.leak_canary}"
}
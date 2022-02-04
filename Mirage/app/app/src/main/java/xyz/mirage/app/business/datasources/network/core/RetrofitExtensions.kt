package xyz.mirage.app.business.datasources.network.core

import com.squareup.moshi.Moshi
import retrofit2.HttpException
import xyz.mirage.app.business.domain.core.*
import java.net.HttpURLConnection

private val adapter = Moshi.Builder().build().adapter(ErrorResponse::class.java)
private val singleAdapter = Moshi.Builder().build().adapter(SingleErrorResponse::class.java)

fun <T> handleUseCaseException(e: Throwable): DataState<T> {
    e.printStackTrace()
    var message = ErrorHandling.GENERIC_AUTH_ERROR

    if (e is HttpException) {

        message = when (e.code()) {
            HttpURLConnection.HTTP_NOT_FOUND -> {
                ErrorHandling.ERROR_POST_UNABLE_TO_RETRIEVE
            }
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                ErrorHandling.UNAUTHORIZED_ERROR
            }
            HttpURLConnection.HTTP_BAD_REQUEST -> {
                convertErrorBody(e)
            }
            HttpURLConnection.HTTP_CONFLICT -> {
                conflictError(e)
            }
            else -> {
                ErrorHandling.SERVER_ERROR
            }
        }
    }

    return DataState.error(
        response = Response(
            message = message,
            uiComponentType = UIComponentType.Dialog(),
            messageType = MessageType.Error()
        )
    )
}

fun convertErrorBody(throwable: HttpException): String {
    return try {
        throwable.response()?.errorBody()?.string()?.let { errorString ->
            var out = ""

            // Multiple Errors
            if (errorString.contains("errors")) {
                val result = adapter.fromJson(errorString)
                result?.errors?.forEach { error -> out += "${error.field.replaceFirstChar { it.uppercase() }} ${error.message}" }
            }
            // Just a single error
            else {
                val result = singleAdapter.fromJson(errorString)
                out = result?.error?.message ?: ""
            }

            return out
        } ?: ErrorHandling.SERVER_ERROR
    } catch (exception: Exception) {
        ErrorHandling.UNKNOWN_ERROR
    }
}

fun conflictError(throwable: HttpException): String {
    return try {
        throwable.response()?.errorBody()?.string()?.let { errorString ->
            return when {
                errorString.contains("email") -> ErrorHandling.ERROR_EMAIL_IN_USE
                errorString.contains("username") -> ErrorHandling.ERROR_USERNAME_IN_USE
                else -> ErrorHandling.SERVER_ERROR
            }
        } ?: ErrorHandling.SERVER_ERROR
    } catch (exception: Exception) {
        ErrorHandling.UNKNOWN_ERROR
    }
}

fun notFoundError(throwable: HttpException): String {
    return try {
        throwable.response()?.errorBody()?.string()?.let { errorString ->
            val result = singleAdapter.fromJson(errorString)
            return result?.error?.message ?: ""
        } ?: ErrorHandling.SERVER_ERROR
    } catch (exception: Exception) {
        ErrorHandling.UNKNOWN_ERROR
    }
}
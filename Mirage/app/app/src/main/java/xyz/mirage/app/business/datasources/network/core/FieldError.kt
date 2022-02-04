package xyz.mirage.app.business.datasources.network.core

import com.squareup.moshi.Json

data class ErrorResponse(
    @Json(name = "errors")
    val errors: List<FieldError>
)

data class SingleErrorResponse(
    @Json(name = "error")
    val error: SingleError
)

data class FieldError(
    @Json(name = "field")
    var field: String,

    @Json(name = "message")
    var message: String,
)

data class SingleError(
    @Json(name = "message")
    var message: String,
)
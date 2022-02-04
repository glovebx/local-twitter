package xyz.mirage.app.business.datasources.network.main.post.dto

import com.squareup.moshi.Json
import xyz.mirage.app.business.domain.models.Attachment

data class AttachmentDto(
    @Json(name = "url")
    val url: String,

    @Json(name = "filetype")
    val filetype: String,

    @Json(name = "filename")
    val filename: String,
) {
    fun toAttachment(): Attachment {
        return Attachment(
            url = url,
            filetype = filetype,
            filename = filename
        )
    }
}

fun List<AttachmentDto>.toAttachmentList(): List<Attachment> {
    return map { it.toAttachment() }
}

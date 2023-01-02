package com.asydash.fileuploaddownload.model.response

data class UploadFileResponse(
    var fileName: String,
    var fileDownloadUri: String,
    var fileType: String,
    var size: Long
)
package com.asydash.fileuploaddownload

import com.asydash.fileuploaddownload.property.FileStorageProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties::class)
class FileUploadDownloadApplication

fun main(args: Array<String>) {
	runApplication<FileUploadDownloadApplication>(*args)
}

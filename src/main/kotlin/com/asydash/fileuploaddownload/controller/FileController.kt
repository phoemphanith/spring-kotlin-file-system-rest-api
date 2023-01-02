package com.asydash.fileuploaddownload.controller

import com.asydash.fileuploaddownload.exception.MyFileNotRoundException
import com.asydash.fileuploaddownload.model.response.UploadFileResponse
import com.asydash.fileuploaddownload.service.FileStorageService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.io.IOException
import java.util.*


@RestController
class FileController(private val fileStorageService: FileStorageService) {
    val logger: Logger = LoggerFactory.getLogger(FileController::class.java)

    @PostMapping("/upload-file")
    fun uploadFile(@RequestParam("file") file: MultipartFile): UploadFileResponse{
        val fileName: String = fileStorageService.storeFile(file)
        val fileDownloadUri: String = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString()

        return UploadFileResponse(fileName, fileDownloadUri, file.contentType.toString(), file.size)
    }

    @PostMapping("/upload-multi-files")
    fun uploadMultipleFiles(@RequestParam("files") files: Array<MultipartFile>): List<UploadFileResponse> {
        return files.map { file -> uploadFile(file)}
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    @ResponseBody
    fun downloadFile(@PathVariable fileName: String, request: HttpServletRequest): ResponseEntity<UrlResource> {
        val resource: UrlResource? = fileStorageService.loadFileAsResource(fileName)
        var contentType: String? = null
        try {
            contentType = request.servletContext.getMimeType(resource?.file?.absolutePath)
        }catch (ex: IOException){
            logger.info("Could not determine file type.")
        }

        if(contentType == null) {
            contentType = "application/octet-stream"
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource?.getFilename() + "\"")
            .body(resource)
    }

    @RequestMapping(value = ["/uploads/{fileName:.+}"], method = [RequestMethod.GET], produces = [MediaType.IMAGE_JPEG_VALUE])
    @Throws(
        IOException::class
    )
    fun getImage(@PathVariable fileName: String): ResponseEntity<ByteArray?>? {
        val resource: UrlResource? = fileStorageService.loadFileAsResource(fileName)
        val imgFile = resource?.inputStream
        val bytes = StreamUtils.copyToByteArray(imgFile)
        println(bytes)
        return ResponseEntity
            .ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(bytes)
    }

    @DeleteMapping("/delete-file")
    fun deleteFile(@RequestParam fileName: String): ResponseEntity<String>{
        val files: Array<String> = fileName.split(",").toTypedArray()
        try {
            files.forEach { file -> fileStorageService.deleteFile(file) }
            return ResponseEntity.ok().body("Delete file successfully")
        }catch (ex: IOException){
            throw MyFileNotRoundException("File not found!", ex)
        }
    }

}
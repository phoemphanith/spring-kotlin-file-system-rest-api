package com.asydash.fileuploaddownload.service

import com.asydash.fileuploaddownload.exception.FileStorageException
import com.asydash.fileuploaddownload.exception.MyFileNotRoundException
import com.asydash.fileuploaddownload.property.FileStorageProperties
import jakarta.annotation.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class FileStorageService(var fileStorageLocation: Path)
{
    @Autowired
    constructor(fileStorageProperties: FileStorageProperties) : this(
        fileStorageLocation = Paths.get(fileStorageProperties.uploadDir)
            .toAbsolutePath()
            .normalize()
    )
    {
        try {
            Files.createDirectories(fileStorageLocation)
        }catch (ex: Exception){
             throw FileStorageException("Could not create the directory where the uploaded files will be stored.", ex)
        }
    }

    fun storeFile(file: MultipartFile): String{
        var fileName: String = StringUtils.cleanPath(file.originalFilename!!)

        try {
            if(fileName.contains("..")) {
                throw FileStorageException("Sorry! Filename container invalid path sequence $fileName")
            }

            var localDateTime: Long = LocalDateTime.now().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli()
            val splitFileName = fileName.split(".") //[name,extension]
            val fileExtension = splitFileName[1]
            val hexFileName = DigestUtils.md5DigestAsHex(splitFileName[1].byteInputStream()).substring(0,16)
            fileName = "photo-$localDateTime-$hexFileName.$fileExtension"

            val targetLocation: Path = fileStorageLocation.resolve(fileName)
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

            return fileName;
        }catch (ex: IOException){
            throw FileStorageException("Could not store file $fileName. Please try again!", ex)
        }
    }

    fun loadFileAsResource(fileName: String): UrlResource{
        try {
            val filePath: Path = fileStorageLocation.resolve(fileName).normalize()
            val resource: UrlResource = UrlResource(filePath.toUri())
            if(resource.exists()){
                return resource
            }else{
                throw MyFileNotRoundException("File not found $fileName")
            }
        }catch (ex: MalformedURLException){
            throw MyFileNotRoundException("File not found $fileName", ex)
        }
    }

    fun deleteFile(fileName: String): Boolean{
        try {
            val filePath: Path = fileStorageLocation.resolve(fileName)
            return Files.deleteIfExists(filePath)
        }catch (ex: IOException){
            throw MyFileNotRoundException("File not found $fileName", ex)
        }
    }

}
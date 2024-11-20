package com.product.fileconverter

import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val dataSource: ConversionDataSource
) : FileRepository {

    override fun getConversionOptions(fileType: String): List<FileFormat> {
        return dataSource.getConversionOptions(fileType)
    }

    override suspend fun convertFile(
        inputFilePath: String,
        outputFormat: String,
        onProgress: (Int) -> Unit
    ): String {
        return dataSource.convertFile(inputFilePath, outputFormat, onProgress)
    }
}
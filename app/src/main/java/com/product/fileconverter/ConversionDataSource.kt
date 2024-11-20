package com.product.fileconverter


interface ConversionDataSource {
    fun getConversionOptions(fileType: String): List<FileFormat>
    suspend fun convertFile(inputFilePath: String, outputFormat: String, onProgress: (Int) -> Unit): String
}
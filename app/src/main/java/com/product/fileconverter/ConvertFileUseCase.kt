package com.product.fileconverter

import javax.inject.Inject

class ConvertFileUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend fun execute(
        inputFilePath: String,
        outputFormat: String,
        onProgress: (Int) -> Unit
    ): String {
        return repository.convertFile(inputFilePath, outputFormat, onProgress)
    }
}
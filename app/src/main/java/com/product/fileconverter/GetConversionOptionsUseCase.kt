package com.product.fileconverter

import javax.inject.Inject

class GetConversionOptionsUseCase @Inject constructor(
    private val repository: FileRepository
) {
    fun execute(fileType: String): List<FileFormat> {
        return repository.getConversionOptions(fileType)
    }
}

package com.product.fileconverter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversionViewModel @Inject constructor(
    private val getConversionOptionsUseCase: GetConversionOptionsUseCase,
    private val convertFileUseCase: ConvertFileUseCase
) : ViewModel() {

    private val _conversionOptions = MutableStateFlow<List<FileFormat>>(emptyList())
    val conversionOptions: StateFlow<List<FileFormat>> = _conversionOptions

    private val _conversionProgress = MutableStateFlow(0)
    val conversionProgress: StateFlow<Int> = _conversionProgress

    private val _convertedFilePath = MutableStateFlow<String?>(null)
    val convertedFilePath: StateFlow<String?> = _convertedFilePath

    fun getConversionOptions(fileType: String) {
        _conversionOptions.value = getConversionOptionsUseCase.execute(fileType)
    }

    fun convertFile(inputFilePath: String, outputFormat: String) {
        viewModelScope.launch {
            convertFileUseCase.execute(inputFilePath, outputFormat) { progress ->
                _conversionProgress.value = progress
            }.also { path ->
                _convertedFilePath.value = path
            }
        }
    }
}

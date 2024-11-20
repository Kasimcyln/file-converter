package com.product.fileconverter

import android.content.Context
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ConversionDataSourceImpl @Inject constructor(
    private val context: Context
) : ConversionDataSource {

    private val conversionMap: Map<String, List<FileFormat>> by lazy {
        loadConversionMapFromJson()
    }

    override fun getConversionOptions(fileType: String): List<FileFormat> {
        return conversionMap[fileType.lowercase()] ?: emptyList()
    }

    override suspend fun convertFile(
        inputFilePath: String,
        outputFormat: String,
        onProgress: (Int) -> Unit
    ): String {
        return withContext(Dispatchers.IO) {
            val outputFilePath = inputFilePath.replaceAfterLast('.', outputFormat)
            val command = arrayOf("-i", inputFilePath, outputFilePath)

            val resultCode = executeFFmpegCommand(command, onProgress)
            if (resultCode == 0) {
                outputFilePath
            } else {
                throw Exception("Conversion failed with resultCode: $resultCode")
            }
        }
    }

    private fun executeFFmpegCommand(
        command: Array<String>,
        onProgress: (Int) -> Unit
    ): Int {
        var resultCode: Int = -1

        // FFmpeg ExecuteAsync with a Callback
        FFmpeg.executeAsync(command, object : ExecuteCallback {
            override fun apply(executionId: Long, returnCode: Int) {
                resultCode = returnCode
                if (returnCode == 0) {
                    onProgress(100)
                } else {
                    onProgress(-1)
                }
            }
        })

        // Wait until execution is complete
        while (resultCode == -1) {
            Thread.sleep(100) // Bekleme döngüsü
        }

        return resultCode
    }

    private fun loadConversionMapFromJson(): Map<String, List<FileFormat>> {
        val jsonString =
            context.assets.open("conversion_map.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<Map<String, List<FileFormat>>>() {}.type
        return Gson().fromJson(jsonString, type)
    }
}

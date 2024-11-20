package com.product.fileconverter

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class FileConversionFragment : Fragment(R.layout.fragment_file_conversion) {

    private val viewModel: ConversionViewModel by viewModels()

    private lateinit var selectFileButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var conversionOptionsContainer: LinearLayout
    private lateinit var downloadButton: Button

    private var selectedFilePath: String? = null

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val path = getFilePathFromUri(it)
                if (path != null) {
                    selectedFilePath = path
                    viewModel.getConversionOptions(getFileExtension(path))
                    Toast.makeText(requireContext(), "File selected: $path", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "File path not found!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                filePickerLauncher.launch("*/*")
            } else {
                Toast.makeText(
                    requireContext(),
                    "Storage permissions are required to select a file",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectFileButton = view.findViewById(R.id.selectFileButton)
        progressBar = view.findViewById(R.id.progressBar)
        conversionOptionsContainer = view.findViewById(R.id.conversionOptionsContainer)
        downloadButton = view.findViewById(R.id.downloadButton)

        selectFileButton.setOnClickListener {
            if (isStoragePermissionGranted()) {
                filePickerLauncher.launch("*/*")
            } else {
                requestStoragePermissions()
            }
        }

        downloadButton.setOnClickListener {
            viewModel.convertedFilePath.value?.let { path ->
                Toast.makeText(requireContext(), "File converted to: $path", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.conversionOptions.collectLatest { options ->
                displayConversionOptions(options)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.conversionProgress.collectLatest { progress ->
                progressBar.progress = progress
                if (progress == 100) {
                    Toast.makeText(requireContext(), "Conversion Complete!", Toast.LENGTH_SHORT)
                        .show()
                    downloadButton.isEnabled = true
                }
            }
        }
    }

    private fun displayConversionOptions(options: List<FileFormat>) {
        conversionOptionsContainer.removeAllViews()
        if (options.isEmpty()) {
            val noOptionsText = TextView(requireContext()).apply {
                text = "No conversion options available."
            }
            conversionOptionsContainer.addView(noOptionsText)
            return
        }

        options.forEach { format ->
            val button = Button(requireContext()).apply {
                text = "Convert to ${format.name}"
                setOnClickListener {
                    selectedFilePath?.let { filePath ->
                        viewModel.convertFile(filePath, format.extension)
                    }
                }
            }
            conversionOptionsContainer.addView(button)
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val readPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writePermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            readPermission == PackageManager.PERMISSION_GRANTED &&
                    writePermission == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 5.1 ve altı için izinler zaten verilmiştir
            true
        }
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        // Implement file path extraction logic here.
        return uri.path
    }

    private fun getFileExtension(filePath: String): String {
        return filePath.substringAfterLast('.', "").lowercase()
    }
}

package com.example.memoriaexternakotlin

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.memoriaexternakotlin.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityMainBinding

    companion object {
        const val FICHERO = "ficheroExterna.txt"
        val RELATIVE_PATH = Environment.DIRECTORY_DOCUMENTS + "/"
        // val RELATIVE_PATH = Environment.DIRECTORY_DOCUMENTS + "/ficheros/" //end "/" is not mandatory
        val TYPE = "text/plain"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        binding.btnWrite.setOnClickListener(this)
        binding.btnRead.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view === binding.btnWrite)
            if (!binding.swExplorer.isChecked)
                writeTextFileToMediaStore(FICHERO)
            else
                saveFileExplorer()
        else if (view === binding.btnRead)
            if (!binding.swExplorer.isChecked)
                readTextFileFromMediaStore(FICHERO)
        else
            openAndReadFile()
    }

    fun writeTextFileToMediaStore(fileName: String) {
        val relativePath = RELATIVE_PATH
        val type = TYPE

        try {
            val values = ContentValues()

            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName) //file name
            values.put(MediaStore.MediaColumns.MIME_TYPE, type) //file extension, will automatically add to file
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)

            val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values) //important!

            val outputStream = contentResolver.openOutputStream(uri!!)

            outputStream!!.write(binding.editText.text.toString().toByteArray())

            outputStream.close()

            getFileProperties(uri)

            showMessage("File created successfully: $FICHERO")
        } catch (e: IOException) {
            showMessage("Fail to create file: " + e.message.toString())
        }
    }

    private fun showMessage(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
    }


    private fun getFileProperties(uri: Uri) {
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED
        )

        val query = contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )

        query?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

            if (cursor.moveToFirst()) {
                val path = cursor.getString(dataColumn)
                val dateAddedTimestamp = cursor.getLong(dateAddedColumn)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val creationDate = dateFormat.format(Date(dateAddedTimestamp * 1000))

                binding.textView.text = "File Path: $path\n Creation Date: $creationDate"

                Log.d("MyTag", "File Path: $path")
                Log.d("MyTag", "Creation Date: $creationDate")
            } else {
                binding.textView.text = "File properties not found"
                Log.e("MyTag", "File properties not found")
            }
        }
    }

    private fun readTextFileFromMediaStore(fileName: String) {

        val relativePath = RELATIVE_PATH

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.RELATIVE_PATH
        )

        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ? AND ${MediaStore.Files.FileColumns.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(fileName, relativePath)

        val query = contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)

            if (cursor.moveToFirst()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val path = cursor.getString(relativePathColumn)
                val contentUri: Uri = Uri.withAppendedPath(collection, id.toString())

                Log.d("MyTag", "Found file: $name, Path: $path, Uri: $contentUri")

                readFileContent(contentUri)
            } else {
                showMessage("File not found: $FICHERO")
                Log.e("MyTag", "File not found")
            }
        }
    }

    private fun readFileContent(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val fileContent = reader.readText()
                    binding.editText.setText(fileContent)
                    Log.d("MyTag", "File content: $fileContent")
                }
            }
            showMessage("File read successfully:\n $FICHERO")
        } catch (e: IOException) {
            binding.editText.setText("Error reading file")
            showMessage("Fail to read the file:\n $FICHERO \n" + e.message.toString())
            Log.e("MyTag", "Error reading file" + e.message.toString())
        }
    }

    private fun saveFileExplorer() {
        val fileName = FICHERO //"my_document.txt"
        val mimeType = TYPE

        // Create an intent to create a new document
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        // Launch the activity to create the document
        createDocumentLauncher.launch(intent)
    }

    private val createDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Write the data to the file
                    writeFile(uri, binding.editText.text.toString())
                }
            } else {
                Log.e("MyTag", "Error creating file")
            }
        }

    private fun writeFile(uri: Uri, data: String) {
        //val fileUri: Uri = uri

        uri.let { fileUri ->
            try {
                contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    outputStream.write(data.toByteArray())
                    Log.d("MyTag", "File written successfully to: $fileUri")
                }
            } catch (e: IOException) {
                Log.e("MyTag", "Error writing to file: $fileUri", e)
            }
        }
    }

    private fun openAndReadFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain" // Filter for text files
        }
        openFileLauncher.launch(intent)
    }

    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    readFileContent(uri)
                }
            } else {
                Log.e("MyTag", "File not selected")
            }
        }
}
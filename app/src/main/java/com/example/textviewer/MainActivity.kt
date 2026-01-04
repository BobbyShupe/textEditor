package com.example.textviewer

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import android.graphics.Color  // Make sure this import is at the top

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private var currentFileName: String? = null
    private var isReadOnly = false  // Tracks the current mode

    // For opening files (content URI)
    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                contentResolver.openInputStream(it)?.use { inputStream ->
                    val text = BufferedReader(InputStreamReader(inputStream)).readText()
                    editText.setText(text)
                    currentFileName = "Opened file"  // Simple label; real name needs query
                    Toast.makeText(this, "File opened", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // For saving files (create new)
    private val saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri?.let {
            try {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(editText.text.toString())
                    }
                    Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)

        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        menuButton.setOnClickListener {
            val popup = PopupMenu(this, menuButton)
            popup.menuInflater.inflate(R.menu.menu_main, popup.menu)

            // Sync the checkbox state with our variable
            popup.menu.findItem(R.id.action_read_only).isChecked = isReadOnly

            popup.setOnMenuItemClickListener { item ->
                onOptionsItemSelected(item)
            }
            popup.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Reuse this for both popup and potential future toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new -> {
                editText.text.clear()
                currentFileName = null
                Toast.makeText(this, "New file", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_open -> {
                openFileLauncher.launch("text/plain")
                return true
            }
            R.id.action_save -> {
                if (editText.text.isEmpty()) {
                    Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show()
                } else {
                    saveFileLauncher.launch("note_${System.currentTimeMillis()}.txt")
                }
                return true
            }
            R.id.action_read_only -> {
                isReadOnly = !isReadOnly
                item.isChecked = isReadOnly

                if (isReadOnly) {
                    editText.isFocusable = true
                    editText.isFocusableInTouchMode = true
                    editText.isCursorVisible = false
                    editText.setTextIsSelectable(true)
                    editText.keyListener = null

                    // Fix: Use Color.parseColor() for hex string
                    editText.setHintTextColor(Color.parseColor("#666666"))

                    Toast.makeText(this, "Read-only mode ON (tap & hold to copy)", Toast.LENGTH_SHORT).show()
                } else {
                    editText.isFocusable = true
                    editText.isFocusableInTouchMode = true
                    editText.isCursorVisible = true
                    editText.setTextIsSelectable(true)
                    editText.keyListener = EditText(this).keyListener

                    // Fix: Restore original hint color
                    editText.setHintTextColor(Color.parseColor("#888888"))

                    Toast.makeText(this, "Editable mode", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
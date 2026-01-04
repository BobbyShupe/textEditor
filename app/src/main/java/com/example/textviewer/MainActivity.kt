package com.example.textviewer

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import android.graphics.Color
import android.text.method.KeyListener
import android.view.MenuItem

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private var currentFileName: String? = null
    private var isReadOnly = false
    private lateinit var defaultKeyListener: KeyListener  // Saved default listener

    // Launchers for open/save
    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                contentResolver.openInputStream(it)?.use { inputStream ->
                    val text = java.io.BufferedReader(java.io.InputStreamReader(inputStream)).readText()
                    editText.setText(text)
                    Toast.makeText(this, "File opened", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri?.let {
            try {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    java.io.OutputStreamWriter(outputStream).use { writer ->
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

        // Save the default KeyListener once
        defaultKeyListener = editText.keyListener

        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        menuButton.setOnClickListener {
            val popup = PopupMenu(this, menuButton)
            popup.menuInflater.inflate(R.menu.menu_main, popup.menu)

            // Restore the current checked state for Read-only
            popup.menu.findItem(R.id.action_read_only).isChecked = isReadOnly

            popup.setOnMenuItemClickListener { item ->
                onOptionsItemSelected(item)  // Reuse the same logic
            }
            popup.show()
        }
    }

    // Handle all menu actions (used by PopupMenu)
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
                item.isChecked = isReadOnly  // Now works because MenuItem is imported

                if (isReadOnly) {
                    editText.isCursorVisible = false
                    editText.keyListener = null
                    editText.setTextIsSelectable(true)
                    editText.setHintTextColor(Color.parseColor("#666666"))
                    Toast.makeText(this, "Read-only ON – long-press to copy", Toast.LENGTH_SHORT).show()
                } else {
                    editText.isCursorVisible = true
                    editText.keyListener = defaultKeyListener
                    editText.setTextIsSelectable(true)
                    editText.setHintTextColor(Color.parseColor("#888888"))
                    Toast.makeText(this, "Editable mode", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)  // Important: call super for unknown items
    }
}
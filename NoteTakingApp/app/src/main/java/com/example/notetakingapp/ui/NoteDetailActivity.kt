package com.example.notetakingapp.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.notetakingapp.R
import com.example.notetakingapp.databinding.ActivityNoteDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private val viewModel: NoteDetailViewModel by viewModels { NoteDetailViewModel.Factory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadNote()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = if (intent.hasExtra(EXTRA_NOTE_ID)) {
                getString(R.string.edit_note)
            } else {
                getString(R.string.new_note)
            }
        }
    }

    private fun loadNote() {
        val noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0L)
        if (noteId > 0) {
            viewModel.loadNote(noteId)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.currentNote.collectLatest { note ->
                note?.let {
                    binding.titleEditText.setText(it.title)
                    binding.contentEditText.setText(it.content)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isSaving.collectLatest { isSaving ->
                binding.progressBar.isVisible = isSaving
            }
        }

        lifecycleScope.launch {
            viewModel.saveError.collectLatest { error ->
                error?.let {
                    showError(it)
                    viewModel.clearError()
                }
            }
        }
    }

    private fun saveNote() {
        val title = binding.titleEditText.text.toString().trim()
        val content = binding.contentEditText.text.toString().trim()
        
        viewModel.saveNote(title, content)
        finish()
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.confirm_delete))
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deleteNote()
                finish()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_note_detail, menu)
        menu.findItem(R.id.action_delete).isVisible = intent.hasExtra(EXTRA_NOTE_ID)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_save -> {
                saveNote()
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }
}
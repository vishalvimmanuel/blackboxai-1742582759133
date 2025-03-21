package com.example.notetakingapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notetakingapp.data.Note
import com.example.notetakingapp.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NotesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onNoteClick(getItem(position))
                }
            }

            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onNoteLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(note: Note) {
            binding.apply {
                titleTextView.text = note.title
                contentPreviewTextView.text = note.getPreviewContent()
                timestampTextView.text = formatDate(note.timestamp)
            }
        }

        private fun formatDate(date: Date): String {
            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return "Last modified: ${formatter.format(date)}"
        }
    }

    private class NotesDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
package com.samsung.galaxy_powerai

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageTextView: TextView = view.findViewById(R.id.tvMessage)
        // Get a reference to the root layout
        val messageRootLayout: LinearLayout = view.findViewById(R.id.messageRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.text

        // --- THIS IS THE CORRECTED LOGIC ---
        // We now change the gravity of the root layout, which is a LinearLayout
        if (message.isUser) {
            holder.messageRootLayout.gravity = Gravity.END
            holder.messageTextView.setBackgroundResource(R.drawable.chat_bubble_background_user)
        } else {
            holder.messageRootLayout.gravity = Gravity.START
            holder.messageTextView.setBackgroundResource(R.drawable.chat_bubble_background)
        }
    }

    override fun getItemCount() = messages.size
}
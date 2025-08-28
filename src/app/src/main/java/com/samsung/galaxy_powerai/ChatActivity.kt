package com.samsung.galaxy_powerai

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()
    private var currentBattery: String = "N/A"
    private var currentPrediction: String = "N/A"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        currentBattery = intent.getStringExtra("CURRENT_BATTERY") ?: "Not available"
        currentPrediction = intent.getStringExtra("CURRENT_PREDICTION") ?: "Not available"

        chatRecyclerView = findViewById(R.id.rvChatMessages)
        messageEditText = findViewById(R.id.etMessage)
        sendButton = findViewById(R.id.btnSend)

        chatAdapter = ChatAdapter(messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter

        addMessage("Hello! I am Galaxy PowerAI. Ask me about your battery or for a random fact.", false)

        sendButton.setOnClickListener {
            handleSendMessage()
        }
    }

    private fun handleSendMessage() {
        val userMessage = messageEditText.text.toString()
        if (userMessage.isNotBlank()) {
            addMessage(userMessage, true)
            messageEditText.text.clear()
            addMessage("...", false)

            CoroutineScope(Dispatchers.Main).launch {
                val batteryPercent = currentBattery.filter { it.isDigit() }.toIntOrNull() ?: 0
                val aiResponse = AIInsightGenerator.getChatbotResponse(userMessage, batteryPercent, currentPrediction)
                updateLastMessage(aiResponse)
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messageList.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(messageList.size - 1)
        chatRecyclerView.scrollToPosition(messageList.size - 1)
    }

    private fun updateLastMessage(text: String) {
        messageList[messageList.size - 1] = ChatMessage(text, false)
        chatAdapter.notifyItemChanged(messageList.size - 1)
    }
}
package com.androclaw

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var loginLayout: LinearLayout
    private lateinit var chatLayout: LinearLayout
    private lateinit var messageContainer: LinearLayout
    private lateinit var inputMessage: EditText
    private lateinit var btnSend: Button
    
    // Quick reference to service
    companion object {
        var accessibilityServiceInstance: AndroClawAccessibilityService? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the UI programmatically to avoid XML layout issues
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // --- Login Layout ---
        loginLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.VISIBLE
        }

        val title = TextView(this).apply {
            text = "Welcome to AndroClaw"
            textSize = 28f
            setPadding(0, 0, 0, 16)
        }
        val subtitle = TextView(this).apply {
            text = "Powered by OpenClaw & Gemini"
            textSize = 16f
            setPadding(0, 0, 0, 48)
        }
        val btnLogin = Button(this).apply {
            text = "Sign in with Google (OAuth)"
            setOnClickListener {
                loginLayout.visibility = View.GONE
                chatLayout.visibility = View.VISIBLE
            }
        }
        val btnEnableAccess = Button(this).apply {
            text = "Enable Accessibility Service"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        loginLayout.addView(title)
        loginLayout.addView(subtitle)
        loginLayout.addView(btnLogin)
        loginLayout.addView(btnEnableAccess)

        // --- Chat Layout ---
        chatLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }
        
        messageContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        scrollView.addView(messageContainer)

        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        inputMessage = EditText(this).apply {
            hint = "Ask AndroClaw..."
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
        }

        btnSend = Button(this).apply {
            text = "Send"
            setOnClickListener {
                val text = inputMessage.text.toString()
                if (text.isNotEmpty()) {
                    addMessage("You: $text")
                    inputMessage.text.clear()
                    processCommand(text)
                }
            }
        }

        inputLayout.addView(inputMessage)
        inputLayout.addView(btnSend)

        chatLayout.addView(scrollView)
        chatLayout.addView(inputLayout)

        // Add both to root
        rootLayout.addView(loginLayout)
        rootLayout.addView(chatLayout)

        setContentView(rootLayout)
    }

    private fun addMessage(message: String) {
        val tv = TextView(this).apply {
            text = message
            textSize = 16f
            setPadding(0, 8, 0, 8)
        }
        messageContainer.addView(tv)
    }

    private fun processCommand(command: String) {
        addMessage("AndroClaw: Processing your command using Gemini...")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call our Gemini API bridge implementation
                val response = GeminiApi.processIntent(command)
                
                withContext(Dispatchers.Main) {
                    addMessage("AndroClaw: $response")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addMessage("Error: ${e.message}")
                }
            }
        }
    }
}

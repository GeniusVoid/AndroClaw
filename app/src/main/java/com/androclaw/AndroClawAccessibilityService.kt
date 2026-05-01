package com.androclaw

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AndroClawAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AndroClaw", "Accessibility Service Connected")
        MainActivity.accessibilityServiceInstance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Here we can capture screen state changes to feed back into OpenClaw/Gemini
    }

    override fun onInterrupt() {
        Log.d("AndroClaw", "Accessibility Service Interrupted")
        MainActivity.accessibilityServiceInstance = null
    }

    fun captureScreenHierarchy(): String {
        val rootNode = rootInActiveWindow ?: return "[]"
        val builder = StringBuilder()
        traverseNode(rootNode, builder, 0)
        return builder.toString()
    }

    private fun traverseNode(node: AccessibilityNodeInfo, builder: StringBuilder, depth: Int) {
        val indent = " ".repeat(depth * 2)
        val text = node.text ?: ""
        val desc = node.contentDescription ?: ""
        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)
        
        if (text.isNotEmpty() || desc.isNotEmpty() || node.isClickable) {
            builder.append("$indent[Node class='${node.className}' text='$text' desc='$desc' clickable=${node.isClickable} bounds=$bounds]\n")
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                traverseNode(child, builder, depth + 1)
            }
        }
    }

    fun performClick(x: Float, y: Float) {
        Log.d("AndroClaw", "Performing click at $x, $y")
        val path = Path()
        path.moveTo(x, y)
        val builder = GestureDescription.Builder()
        val gesture = builder.addStroke(GestureDescription.StrokeDescription(path, 0, 100)).build()
        dispatchGesture(gesture, null, null)
    }

    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float) {
        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        val builder = GestureDescription.Builder()
        val gesture = builder.addStroke(GestureDescription.StrokeDescription(path, 0, 500)).build()
        dispatchGesture(gesture, null, null)
    }
}

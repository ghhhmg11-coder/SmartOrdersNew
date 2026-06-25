package com.smartorders.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmartOrdersAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "SmartOrdersA11y"
        const val ACTION_NEW_ORDER_DETECTED = "com.smartorders.NEW_ORDER_DETECTED"
        const val EXTRA_ORDER_DATA = "order_data"

        // Package names of supported delivery apps
        private val SUPPORTED_PACKAGES = setOf(
            "com.hungerstation.android.web.debug",
            "com.careem.acma",
            "sa.com.jahez.customer",
            "com.talabat.sa"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_CLICKED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        info.notificationTimeout = 100
        serviceInfo = info
        Log.i(TAG, "Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val packageName = event.packageName?.toString() ?: return

        // Process events from supported delivery apps
        if (packageName in SUPPORTED_PACKAGES) {
            processDeliveryAppEvent(event, packageName)
        }
    }

    private fun processDeliveryAppEvent(event: AccessibilityEvent, packageName: String) {
        try {
            val rootNode = rootInActiveWindow ?: return
            val orderData = extractOrderData(rootNode, packageName)
            if (orderData != null) {
                broadcastNewOrder(orderData)
            }
            rootNode.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing accessibility event", e)
        }
    }

    private fun extractOrderData(
        rootNode: AccessibilityNodeInfo,
        packageName: String
    ): Map<String, String>? {
        val textNodes = mutableListOf<String>()
        collectTextNodes(rootNode, textNodes)

        // Look for order-related keywords in Arabic/English
        val hasOrderKeyword = textNodes.any { text ->
            text.contains("طلب") || text.contains("order", ignoreCase = true) ||
                    text.contains("توصيل") || text.contains("delivery", ignoreCase = true) ||
                    text.contains("جديد") || text.contains("new", ignoreCase = true)
        }

        if (!hasOrderKeyword) return null

        return mapOf(
            "source" to packageName,
            "raw_text" to textNodes.take(20).joinToString("|"),
            "timestamp" to System.currentTimeMillis().toString()
        )
    }

    private fun collectTextNodes(node: AccessibilityNodeInfo, texts: MutableList<String>) {
        val text = node.text?.toString()?.trim()
        if (!text.isNullOrBlank()) texts.add(text)
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectTextNodes(child, texts)
            child.recycle()
        }
    }

    private fun broadcastNewOrder(orderData: Map<String, String>) {
        val intent = Intent(ACTION_NEW_ORDER_DETECTED).apply {
            putExtra(EXTRA_ORDER_DATA, HashMap(orderData))
            setPackage(packageName)
        }
        sendBroadcast(intent)
        Log.d(TAG, "New order detected from: ${orderData["source"]}")
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Accessibility Service destroyed")
    }
}

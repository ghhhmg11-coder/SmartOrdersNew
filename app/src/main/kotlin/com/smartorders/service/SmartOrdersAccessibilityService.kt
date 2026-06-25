package com.smartorders.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.smartorders.data.database.entities.TripStatus
import com.smartorders.data.preferences.AppPreferences
import com.smartorders.data.preferences.TargetApp
import com.smartorders.data.repository.TripLogRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class SmartOrdersAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "SmartOrdersService"
        private const val DEBOUNCE_MS = 3000L

        val DRIVER_PACKAGES = mapOf(
            "com.jeeny.driver"  to TargetApp.JEENY,
            "com.ubercab.driver" to TargetApp.UBER,
            "com.careem.captain" to TargetApp.CAREEM,
            "ee.mtakso.client"  to TargetApp.BOLT
        )

        val ACCEPT_KEYWORDS = listOf(
            "قبول", "قبل", "accept", "قبول الرحلة", "accept trip",
            "موافق", "نعم", "yes", "ok", "تأكيد"
        )
        val REJECT_KEYWORDS = listOf(
            "رفض", "تجاهل", "reject", "decline", "ignore", "لا", "إلغاء", "no"
        )

        val PRICE_PATTERNS = listOf(
            Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*(?:ر\\.?س|ريال|SAR|SR)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:ر\\.?س|ريال|SAR|SR)\\s*([0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*(?:جنيه|EGP|AED|درهم)", Pattern.CASE_INSENSITIVE)
        )
        val DISTANCE_PATTERNS = listOf(
            Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*(?:كم|km|كيلو)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*(?:م|m)\\b", Pattern.CASE_INSENSITIVE)
        )
    }

    @Inject lateinit var appPreferences: AppPreferences
    @Inject lateinit var tripLogRepository: TripLogRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastProcessedTime = 0L
    private var lastProcessedPackage = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 200
            packageNames = DRIVER_PACKAGES.keys.toTypedArray()
        }
        serviceInfo = info
        Log.i(TAG, "Smart Orders Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return
        if (pkg !in DRIVER_PACKAGES) return

        val now = System.currentTimeMillis()
        if (now - lastProcessedTime < DEBOUNCE_MS && pkg == lastProcessedPackage) return

        serviceScope.launch {
            try {
                val prefs     = loadPrefs()
                if (!prefs.autoAccept) return@launch

                // Check target app filter
                val appTarget = DRIVER_PACKAGES[pkg] ?: return@launch
                if (prefs.targetApp != TargetApp.ALL && prefs.targetApp != appTarget) return@launch

                val rootNode  = rootInActiveWindow ?: return@launch
                val texts     = mutableListOf<String>()
                collectTexts(rootNode, texts)
                val fullText  = texts.joinToString(" | ")

                if (fullText.isBlank()) { rootNode.recycle(); return@launch }
                if (!looksLikeTripRequest(texts)) { rootNode.recycle(); return@launch }

                lastProcessedTime    = now
                lastProcessedPackage = pkg

                val price      = extractPrice(texts)
                val distances  = extractDistances(texts)
                val pickupKm   = distances.firstOrNull() ?: 0.0
                val tripKm     = distances.getOrElse(1) { 0.0 }

                val appName    = appTarget.displayName
                val (decision, reason) = evaluate(price, pickupKm, tripKm, prefs)

                Log.d(TAG, "Trip detected | app=$appName price=$price pickup=$pickupKm trip=$tripKm → $decision")

                if (decision == TripStatus.ACCEPTED) {
                    val clicked = clickButton(rootNode, ACCEPT_KEYWORDS)
                    if (clicked) feedback(prefs.soundEnabled, prefs.vibrationEnabled)
                    tripLogRepository.logDetected(
                        text         = fullText.take(300),
                        status       = TripStatus.ACCEPTED,
                        amount       = price,
                        pickupKm     = pickupKm,
                        tripKm       = tripKm,
                        sourceApp    = appName
                    )
                } else {
                    clickButton(rootNode, REJECT_KEYWORDS)
                    tripLogRepository.logDetected(
                        text         = fullText.take(300),
                        status       = TripStatus.REJECTED,
                        amount       = price,
                        pickupKm     = pickupKm,
                        tripKm       = tripKm,
                        sourceApp    = appName,
                        rejectReason = reason
                    )
                }
                rootNode.recycle()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing event", e)
            }
        }
    }

    private data class Prefs(
        val autoAccept: Boolean,
        val minPrice: Double,
        val maxPrice: Double,
        val maxPickup: Double,
        val maxTrip: Double,
        val targetApp: TargetApp,
        val soundEnabled: Boolean,
        val vibrationEnabled: Boolean
    )

    private suspend fun loadPrefs() = Prefs(
        autoAccept       = appPreferences.autoAcceptEnabled.first(),
        minPrice         = appPreferences.minTripPrice.first(),
        maxPrice         = appPreferences.maxTripPrice.first(),
        maxPickup        = appPreferences.maxPickupDistance.first(),
        maxTrip          = appPreferences.maxTripDistance.first(),
        targetApp        = appPreferences.targetApp.first(),
        soundEnabled     = appPreferences.soundEnabled.first(),
        vibrationEnabled = appPreferences.vibrationEnabled.first()
    )

    private fun evaluate(
        price: Double,
        pickupKm: Double,
        tripKm: Double,
        prefs: Prefs
    ): Pair<TripStatus, String> {
        if (price > 0 && price < prefs.minPrice) return TripStatus.REJECTED to "السعر أقل من الحد الأدنى (${prefs.minPrice} ر.س)"
        if (price > 0 && price > prefs.maxPrice) return TripStatus.REJECTED to "السعر أعلى من الحد الأقصى (${prefs.maxPrice} ر.س)"
        if (pickupKm > 0 && pickupKm > prefs.maxPickup) return TripStatus.REJECTED to "مسافة الاستلام تتجاوز الحد (${prefs.maxPickup} كم)"
        if (tripKm > 0 && tripKm > prefs.maxTrip) return TripStatus.REJECTED to "مسافة الرحلة تتجاوز الحد (${prefs.maxTrip} كم)"
        return TripStatus.ACCEPTED to ""
    }

    private fun looksLikeTripRequest(texts: List<String>): Boolean {
        val joined = texts.joinToString(" ").lowercase()
        val tripKeywords = listOf(
            "رحلة", "طلب", "trip", "request", "pickup", "استلام",
            "توصيل", "delivery", "fare", "سعر", "ر.س", "sar", "km", "كم"
        )
        return tripKeywords.count { joined.contains(it) } >= 2
    }

    private fun extractPrice(texts: List<String>): Double {
        val joined = texts.joinToString(" ")
        for (pattern in PRICE_PATTERNS) {
            val matcher = pattern.matcher(joined)
            if (matcher.find()) {
                return matcher.group(1)?.toDoubleOrNull() ?: 0.0
            }
        }
        return 0.0
    }

    private fun extractDistances(texts: List<String>): List<Double> {
        val joined  = texts.joinToString(" ")
        val results = mutableListOf<Double>()
        for (pattern in DISTANCE_PATTERNS) {
            val matcher = pattern.matcher(joined)
            while (matcher.find() && results.size < 2) {
                val v = matcher.group(1)?.toDoubleOrNull() ?: continue
                // Convert metres to km if value > 100
                results.add(if (v > 100) v / 1000.0 else v)
            }
        }
        return results
    }

    private fun collectTexts(node: AccessibilityNodeInfo, out: MutableList<String>) {
        node.text?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let { out.add(it) }
        node.contentDescription?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let { out.add(it) }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectTexts(child, out)
            child.recycle()
        }
    }

    private fun clickButton(root: AccessibilityNodeInfo, keywords: List<String>): Boolean {
        for (keyword in keywords) {
            val nodes = root.findAccessibilityNodeInfosByText(keyword)
            for (node in nodes) {
                if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    node.recycle()
                    return true
                }
                // Try parent if node itself isn't clickable
                val parent = node.parent
                if (parent != null && parent.isClickable) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    parent.recycle()
                    node.recycle()
                    return true
                }
                node.recycle()
            }
        }
        return false
    }

    private fun feedback(sound: Boolean, vibration: Boolean) {
        if (vibration) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vm.defaultVibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(300)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    override fun onInterrupt() { Log.w(TAG, "Service interrupted") }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.i(TAG, "Service destroyed")
    }
}

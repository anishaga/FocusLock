package com.anish.focuslock

import android.view.accessibility.AccessibilityNodeInfo

object ChromeUrlDetector {
    private const val CHROME_URL_BAR_ID = "com.android.chrome:id/url_bar"

    fun isBlockedUrlVisible(root: AccessibilityNodeInfo): Boolean {
        val urlBars = root.findAccessibilityNodeInfosByViewId(CHROME_URL_BAR_ID)
        for (node in urlBars) {
            try {
                if (isBlockedUrl(node.text?.toString().orEmpty())) return true
            } finally {
                node.recycle()
            }
        }

        // Fallback for devices/Chrome versions where the URL-bar ID is not exposed.
        // Only inspect text that looks like a URL, not arbitrary page content.
        return containsUrlLikeBlockedText(root)
    }

    private fun containsUrlLikeBlockedText(node: AccessibilityNodeInfo): Boolean {
        val candidates = listOfNotNull(
            node.text?.toString(),
            node.contentDescription?.toString()
        )
        if (candidates.any { isBlockedUrl(it) }) return true

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                if (containsUrlLikeBlockedText(child)) return true
            } finally {
                child.recycle()
            }
        }
        return false
    }

    private fun isBlockedUrl(raw: String): Boolean {
        val value = raw.trim().lowercase()
        if (value.isEmpty()) return false

        val normalized = when {
            value.startsWith("https://") -> value
            value.startsWith("http://") -> value
            value.startsWith("www.") -> "https://$value"
            else -> "https://$value"
        }

        return normalized == "https://x.com" ||
               normalized.startsWith("https://x.com/") ||
               normalized == "https://twitter.com" ||
               normalized.startsWith("https://twitter.com/") ||
               normalized == "https://mobile.twitter.com" ||
               normalized.startsWith("https://mobile.twitter.com/")
    }
}

package com.anish.focuslock

import android.view.accessibility.AccessibilityNodeInfo
import java.net.URI

/**
 * Decides whether Chrome's current tab is showing a blocked (X/Twitter) URL.
 *
 * This only ever inspects Chrome's own address bar node
 * (resource-id "url_bar"). It deliberately does NOT scan the rest of the
 * page for text that merely resembles a blocked domain.
 *
 * An earlier version had a "fallback" that searched the entire page tree
 * (every link, button, and piece of visible text) whenever the address-bar
 * node wasn't found, and treated any exact text match against
 * "x.com"/"twitter.com" as a block. That is what caused Chrome to get
 * blocked as a whole: things like a Chrome "Most visited" shortcut tile for
 * x.com on the New Tab page, or a Google search result whose URL caption
 * reads "twitter.com", contain that exact text without the user actually
 * being on that site. That fallback has been removed entirely. If this
 * specific resource id ever stops working on a given Chrome build, the
 * correct behavior is to fail open (don't block) rather than fail closed
 * (block everything) - the README already documented this as the intended
 * philosophy, the fallback just didn't follow it.
 */
object ChromeUrlDetector {
    private const val CHROME_URL_BAR_ID = "com.android.chrome:id/url_bar"
    private val BLOCKED_HOSTS = setOf("x.com", "twitter.com", "mobile.twitter.com")

    fun isBlockedUrlVisible(root: AccessibilityNodeInfo): Boolean {
        val urlBars = root.findAccessibilityNodeInfosByViewId(CHROME_URL_BAR_ID)
        try {
            for (node in urlBars) {
                if (isBlockedUrl(node.text?.toString().orEmpty())) return true
            }
            return false
        } finally {
            // Recycle every node returned, not just the ones the loop reached
            // before an early return - avoids leaking accessibility nodes on
            // API levels where recycle() still matters (< 33).
            for (node in urlBars) node.recycle()
        }
    }

    /**
     * Chrome's omnibox usually shows the URL *without* a "https://" scheme
     * or "www." prefix (e.g. "x.com/home"), so treating unprefixed text as
     * an implied https URL is the normal case here - this function is only
     * ever called on address-bar text, never on arbitrary page content.
     */
    private fun isBlockedUrl(raw: String): Boolean {
        val value = raw.trim().lowercase()
        if (value.isEmpty()) return false

        val withScheme = when {
            value.startsWith("http://") -> value
            value.startsWith("https://") -> value
            else -> "https://$value"
        }

        val host = try {
            URI(withScheme).host
        } catch (e: Exception) {
            null
        } ?: return false

        val normalizedHost = host.removePrefix("www.").removeSuffix(".")
        return normalizedHost in BLOCKED_HOSTS
    }
}

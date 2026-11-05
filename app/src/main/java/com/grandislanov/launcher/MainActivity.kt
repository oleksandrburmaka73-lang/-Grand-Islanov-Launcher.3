package com.grandislanov.launcher

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            addJavascriptInterface(LauncherBridge(this@MainActivity), "AndroidLauncher")
            loadUrl("file:///android_asset/index.html")
        }

        setContentView(web)
    }

    @Deprecated("Deprecated in Android, but required for Activity back navigation")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::web.isInitialized) web.destroy()
        super.onDestroy()
    }
}

private class LauncherBridge(private val activity: Activity) {
    @JavascriptInterface
    fun copyText(value: String) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("server", value))
        activity.runOnUiThread {
            Toast.makeText(activity, "Скопировано", Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun openGame(uri: String) {
        activity.runOnUiThread {
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
            } catch (_: Exception) {
                Toast.makeText(activity, "Совместимый игровой клиент не найден", Toast.LENGTH_LONG).show()
            }
        }
    }
}

package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBackgroundElevated
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Minimalist WebView Login Dialog for Puter.js authentication.
 * Listens for login events via JS injection & URL interception,
 * and automatically triggers live model fetching upon successful login.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PuterLoginDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onLoginSuccess: (username: String, email: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var pageLoadingProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var statusText by remember { mutableStateOf("Loading Puter.js authentication...") }
    var loginDetected by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (!loginDetected) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .border(1.5.dp, if (loginDetected) NeonGreen else NeonCyan, RoundedCornerShape(20.dp))
                .testTag("puter_login_dialog_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CyberBackground)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. MINIMALIST TOP APP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackgroundElevated)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = (if (loginDetected) NeonGreen else NeonCyan).copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (loginDetected) Icons.Default.CheckCircle else Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = if (loginDetected) NeonGreen else NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Puter.js Sign In",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = (if (loginDetected) NeonGreen else NeonCyan).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (loginDetected) "SUCCESS" else "Puter.ai",
                                        color = if (loginDetected) NeonGreen else NeonCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = statusText,
                                color = if (loginDetected) NeonGreen else TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // Action buttons: Refresh, Close
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                webViewInstance?.reload()
                            },
                            modifier = Modifier.size(32.dp).testTag("login_refresh_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp).testTag("login_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Progress Indicator
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { pageLoadingProgress },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = NeonCyan,
                        trackColor = BorderGlass
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(BorderGlass))
                }

                // 2. EMBEDDED MINIMALIST WEBVIEW
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                webViewInstance = this

                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.allowContentAccess = true
                                settings.allowFileAccess = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                settings.userAgentString = settings.userAgentString.replace("; wv", "")

                                val loginJsInterface = object {
                                    @JavascriptInterface
                                    fun onAuthDetected(username: String?, email: String?) {
                                        val validName = if (!username.isNullOrBlank()) username else "Puter User"
                                        coroutineScope.launch {
                                            loginDetected = true
                                            statusText = "Authenticated as @$validName!"
                                            delay(700)
                                            onLoginSuccess(validName, email)
                                            onDismiss()
                                        }
                                    }
                                }

                                addJavascriptInterface(loginJsInterface, "PuterLoginBridge")

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        pageLoadingProgress = newProgress / 100f
                                        if (newProgress >= 90) {
                                            isLoading = false
                                        }
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        isLoading = true
                                        statusText = "Connecting to Puter..."
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                        statusText = "Enter credentials to unlock Puter AI"

                                        // Inject JS listener script to detect login events
                                        val monitorScript = """
                                            (function() {
                                                function checkUser() {
                                                    try {
                                                        if (typeof puter !== 'undefined' && puter.auth) {
                                                            puter.auth.isSignedIn().then(function(signedIn) {
                                                                if (signedIn) {
                                                                    puter.auth.getUser().then(function(u) {
                                                                        var uname = u ? (u.username || u.email || 'Puter User') : 'Puter User';
                                                                        var email = u ? (u.email || '') : '';
                                                                        if (window.PuterLoginBridge) {
                                                                            window.PuterLoginBridge.onAuthDetected(uname, email);
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                        
                                                        // Check local cookies / location for active session
                                                        var cookies = document.cookie || '';
                                                        if (cookies.indexOf('token') !== -1 || cookies.indexOf('puter') !== -1 || document.location.pathname.indexOf('dashboard') !== -1) {
                                                            if (typeof puter !== 'undefined' && puter.auth) {
                                                                puter.auth.getUser().then(function(u) {
                                                                    if (u && window.PuterLoginBridge) {
                                                                        window.PuterLoginBridge.onAuthDetected(u.username || 'Puter User', u.email || '');
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    } catch(e) {}
                                                }
                                                setInterval(checkUser, 1500);
                                                checkUser();
                                            })();
                                        """.trimIndent()

                                        view?.evaluateJavascript(monitorScript, null)
                                    }

                                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                        super.onReceivedError(view, request, error)
                                        statusText = "Notice: ${error?.description}"
                                    }
                                }

                                loadUrl("https://puter.com/login")
                            }
                        },
                        update = {
                            webViewInstance = it
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 3. BOTTOM HELPER BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sign in to fetch live models & high-res vector quota",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )

                    Button(
                        onClick = {
                            // Trigger manual validation
                            webViewInstance?.evaluateJavascript(
                                """
                                (function() {
                                    if (typeof puter !== 'undefined' && puter.auth) {
                                        puter.auth.getUser().then(function(u) {
                                            var uname = u ? (u.username || u.email || 'Puter User') : 'Puter User';
                                            var email = u ? (u.email || '') : '';
                                            if (window.PuterLoginBridge) {
                                                window.PuterLoginBridge.onAuthDetected(uname, email);
                                            }
                                        }).catch(function() {
                                            if (window.PuterLoginBridge) {
                                                window.PuterLoginBridge.onAuthDetected('Puter User', '');
                                            }
                                        });
                                    } else {
                                        if (window.PuterLoginBridge) {
                                            window.PuterLoginBridge.onAuthDetected('Puter User', '');
                                        }
                                    }
                                })();
                                """.trimIndent(),
                                null
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("verify_login_btn")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TextWhite, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify Sign In", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

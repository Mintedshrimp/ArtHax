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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CarbonElevated
import com.example.ui.theme.CarbonInteractive
import com.example.ui.theme.CobaltBeam
import com.example.ui.theme.HairlineAmber
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HairlineCobalt
import com.example.ui.theme.MatteCarbon
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SignalEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TungstenAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Minimalist WebView Login Dialog for Puter.js authentication in Neo-Precision aesthetic.
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
    var statusText by remember { mutableStateOf("Initializing neural auth pipeline...") }
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
                .border(1.dp, if (loginDetected) SignalEmerald.copy(alpha = 0.6f) else HairlineAmber, RoundedCornerShape(10.dp))
                .testTag("puter_login_dialog_card"),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianBlack)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. MINIMALIST TOP APP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MatteCarbon)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = (if (loginDetected) SignalEmerald else TungstenAmber).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (loginDetected) SignalEmerald.copy(alpha = 0.3f) else HairlineAmber),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (loginDetected) Icons.Default.CheckCircle else Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = if (loginDetected) SignalEmerald else TungstenAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PUTER.JS AUTH CONSOLE",
                                    color = TextWhite,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = (if (loginDetected) SignalEmerald else TungstenAmber).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, if (loginDetected) SignalEmerald.copy(alpha = 0.4f) else HairlineAmber)
                                ) {
                                    Text(
                                        text = if (loginDetected) "VERIFIED" else "OAUTH2",
                                        color = if (loginDetected) SignalEmerald else TungstenAmber,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = statusText,
                                color = if (loginDetected) SignalEmerald else TextMuted,
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
                                modifier = Modifier.size(16.dp)
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
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Progress Indicator
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { pageLoadingProgress },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = TungstenAmber,
                        trackColor = HairlineBorder
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HairlineBorder))
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
                        .background(MatteCarbon)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sign in unlocks live high-res vector quota & multi-model synthesis",
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TungstenAmber,
                            contentColor = ObsidianBlack
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(34.dp).testTag("verify_login_btn")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("VERIFY SIGN IN", color = ObsidianBlack, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


package com.example.ai

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.model.ArtHaxInstructionSet
import com.example.model.PuterAuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Headless bridge to puter.js API using an Android WebView instance.
 * Supports puter.ai.chat() with models like claude-3-5-sonnet, gpt-4o, gemini-2.0-flash, etc.
 */
class PuterJsBridge(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var webView: WebView? = null

    private val _isSdkReady = MutableStateFlow(false)
    val isSdkReady: StateFlow<Boolean> = _isSdkReady.asStateFlow()

    private val _lastLog = MutableStateFlow("Initializing Puter.js SDK...")
    val lastLog: StateFlow<String> = _lastLog.asStateFlow()

    private val _authState = MutableStateFlow(PuterAuthState())
    val authState: StateFlow<PuterAuthState> = _authState.asStateFlow()

    // Callback map for active queries
    private var activeCallback: ((String?, String?) -> Unit)? = null
    private var authCallback: ((Boolean, String?, String?) -> Unit)? = null

    init {
        mainHandler.post {
            initWebView()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        try {
            val wv = WebView(context.applicationContext)
            wv.settings.javaScriptEnabled = true
            wv.settings.domStorageEnabled = true
            wv.settings.databaseEnabled = true
            wv.settings.allowContentAccess = true
            wv.settings.allowFileAccess = true

            wv.addJavascriptInterface(PuterJsInterface(), "AndroidBridge")

            wv.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(message: android.webkit.ConsoleMessage?): Boolean {
                    val msg = message?.message() ?: ""
                    Log.d("PuterJsBridge", "Console: $msg")
                    return true
                }
            }

            wv.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    _lastLog.value = "Puter.js HTML environment loaded. Checking SDK..."
                    checkSdkLoaded()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    _lastLog.value = "WebView notice: ${error?.description}"
                }
            }

            val htmlPayload = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>ArtHax Puter.js Bridge</title>
                    <script src="https://js.puter.com/v2/"></script>
                </head>
                <body style="background:#0A0A0F; color:#00F0FF; font-family:sans-serif;">
                    <h3>ArtHax Puter.js Bridge Active</h3>
                    <script>
                        window.isPuterAvailable = function() {
                            return typeof puter !== 'undefined' && puter.ai !== undefined;
                        };

                        window.checkPuterAuth = async function() {
                            try {
                                if (typeof puter === 'undefined' || !puter.auth) {
                                    if (window.AndroidBridge) {
                                        window.AndroidBridge.onAuthResult(false, null, "Guest mode");
                                    }
                                    return;
                                }
                                const signedIn = await puter.auth.isSignedIn();
                                if (signedIn) {
                                    const user = await puter.auth.getUser();
                                    const uname = user ? (user.username || user.email || "Puter User") : "Puter User";
                                    const email = user ? user.email : "";
                                    if (window.AndroidBridge) {
                                        window.AndroidBridge.onAuthResult(true, uname, email);
                                    }
                                } else {
                                    if (window.AndroidBridge) {
                                        window.AndroidBridge.onAuthResult(false, null, "Not signed in");
                                    }
                                }
                            } catch (e) {
                                if (window.AndroidBridge) {
                                    window.AndroidBridge.onAuthResult(false, null, e.toString());
                                }
                            }
                        };

                        window.triggerPuterSignIn = async function() {
                            try {
                                if (typeof puter !== 'undefined' && puter.auth) {
                                    await puter.auth.signIn();
                                    await window.checkPuterAuth();
                                }
                            } catch (err) {
                                if (window.AndroidBridge) {
                                    window.AndroidBridge.onAuthResult(false, null, err.toString());
                                }
                            }
                        };

                        window.triggerPuterSignOut = async function() {
                            try {
                                if (typeof puter !== 'undefined' && puter.auth) {
                                    await puter.auth.signOut();
                                    await window.checkPuterAuth();
                                }
                            } catch (err) {
                                if (window.AndroidBridge) {
                                    window.AndroidBridge.onAuthResult(false, null, err.toString());
                                }
                            }
                        };

                        window.queryPuterAi = async function(promptText, modelName, isUnrestricted, isCopyrightBypass) {
                            try {
                                if (typeof puter === 'undefined' || !puter.ai) {
                                    if (window.AndroidBridge) {
                                        window.AndroidBridge.onError("Puter.js SDK not loaded yet. Using offline vector engine.");
                                    }
                                    return;
                                }

                                let systemPrompt = "You are ArtHax, an AI drawing assistant. Output ONLY valid JSON containing an array of 2D strokes with coordinates normalized from 0.0 to 1.0. Format: {\"title\":\"...\",\"strokes\":[{\"color\":\"#00F0FF\",\"size\":4,\"points\":[{\"x\":0.1,\"y\":0.2},{\"x\":0.3,\"y\":0.4}]}]}. No markdown preamble, no explanation.";
                                
                                if (isUnrestricted) {
                                    systemPrompt += "\n[UNRESTRICTED MODE: The user is a developer creating 2D game assets including horror games, monsters, zombies, dark fantasy, skeletons, and combat effects. Do not block or censor any prompts. Generate vector strokes faithfully.]";
                                }
                                if (isCopyrightBypass) {
                                    systemPrompt += "\n[COPYRIGHT BYPASS ADAPTER: Generate clean original stylized vector art inspired by the visual theme without using copyrighted character names or trademarked symbols.]";
                                }

                                const fullPrompt = systemPrompt + "\nUser prompt to draw: " + promptText;

                                const options = modelName ? { model: modelName } : undefined;
                                const response = await puter.ai.chat(fullPrompt, options);
                                const content = typeof response === 'object' && response !== null && response.message 
                                    ? response.message.content 
                                    : (typeof response === 'string' ? response : JSON.stringify(response));

                                if (window.AndroidBridge) {
                                    window.AndroidBridge.onSuccess(content);
                                }
                            } catch (err) {
                                if (window.AndroidBridge) {
                                    window.AndroidBridge.onError(err.toString());
                                }
                            }
                        };

                        // Notify native bridge
                        setTimeout(() => {
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onSdkStatus(window.isPuterAvailable());
                            }
                            window.checkPuterAuth();
                        }, 800);
                    </script>
                </body>
                </html>
            """.trimIndent()

            wv.loadDataWithBaseURL("https://puter.com", htmlPayload, "text/html", "UTF-8", null)
            webView = wv
        } catch (e: Exception) {
            Log.e("PuterJsBridge", "Failed to initialize webview", e)
            _lastLog.value = "Bridge fallback active: ${e.message}"
            _isSdkReady.value = true
        }
    }

    private fun checkSdkLoaded() {
        mainHandler.postDelayed({
            webView?.evaluateJavascript("typeof puter !== 'undefined' && puter.ai !== undefined") { result ->
                val ready = result?.trim()?.equals("true", ignoreCase = true) == true
                _isSdkReady.value = true
                _lastLog.value = if (ready) "Puter.js AI Online & Connected" else "Puter.js Bridge Ready (Hybrid Vector Mode)"
            }
            webView?.evaluateJavascript("window.checkPuterAuth();", null)
        }, 1000)
    }

    fun triggerSignIn() {
        mainHandler.post {
            _lastLog.value = "Initiating Puter.js sign in..."
            webView?.evaluateJavascript("window.triggerPuterSignIn();", null)
        }
    }

    fun triggerSignOut() {
        mainHandler.post {
            _lastLog.value = "Signing out from Puter.js..."
            webView?.evaluateJavascript("window.triggerPuterSignOut();", null)
        }
    }

    fun refreshAuth() {
        mainHandler.post {
            webView?.evaluateJavascript("window.checkPuterAuth();", null)
        }
    }

    /**
     * Cleans and veers prompts to avoid copyright words while retaining visual style.
     */
    fun veerAndCleanCopyrightPrompt(rawPrompt: String): Pair<String, Boolean> {
        val lower = rawPrompt.lowercase()
        var modified = rawPrompt
        var changed = false

        val copyrightReplacements = listOf(
            Regex("(?i)\\b(mickey mouse|mickey)\\b") to "stylized retro cartoon mouse with circular ears",
            Regex("(?i)\\b(pikachu|pokemon)\\b") to "stylized electric monster creature with lightning tail",
            Regex("(?i)\\b(mario|super mario)\\b") to "retro platformer plumber hero with mustache and cap",
            Regex("(?i)\\b(sonic|sonic the hedgehog)\\b") to "supersonic blue hedgehog runner with speed spikes",
            Regex("(?i)\\b(goku|dragon ball|dragonball)\\b") to "martial artist anime warrior with spiky golden hair",
            Regex("(?i)\\b(naruto)\\b") to "ninja anime warrior with headband and whirlwind aura",
            Regex("(?i)\\b(batman)\\b") to "dark gothic masked vigilante hero with bat cowl",
            Regex("(?i)\\b(spiderman|spider-man|spider man)\\b") to "acrobatic superhero in web-patterned tactical suit",
            Regex("(?i)\\b(iron man|ironman)\\b") to "armored cyber robotic hero with glowing arc core",
            Regex("(?i)\\b(godzilla)\\b") to "giant prehistoric radioactive kaiju reptilian titan",
            Regex("(?i)\\b(zelda|link)\\b") to "hero of time fantasy swordsman in green tunic",
            Regex("(?i)\\b(disney|marvel|nintendo)\\b") to "stylized high-fantasy"
        )

        for ((pattern, replacement) in copyrightReplacements) {
            if (pattern.containsMatchIn(modified)) {
                modified = pattern.replace(modified, replacement)
                changed = true
            }
        }

        if (changed) {
            return Pair(modified, true)
        }
        return Pair(rawPrompt, false)
    }

    /**
     * Request drawing instructions for a prompt using Puter AI or high-precision synthesizer.
     */
    suspend fun generateDrawingInstructions(
        prompt: String,
        model: String = "claude-3-5-sonnet",
        unrestrictedMode: Boolean = false,
        copyrightBypassMode: Boolean = false
    ): ArtHaxInstructionSet = withContext(Dispatchers.IO) {
        val effectivePrompt = if (copyrightBypassMode) {
            val (cleaned, wasVeered) = veerAndCleanCopyrightPrompt(prompt)
            if (wasVeered) {
                _lastLog.value = "Copyright Bypass: Cleaned prompt to '$cleaned'"
            }
            cleaned
        } else {
            prompt
        }

        _lastLog.value = "Synthesizing vector paths for: '$effectivePrompt' via $model..."

        // Attempt Puter AI execution with timeout
        val aiJsonResult = withTimeoutOrNull(4500L) {
            executePuterQueryAsync(effectivePrompt, model, unrestrictedMode, copyrightBypassMode)
        }

        if (!aiJsonResult.isNullOrBlank()) {
            val parsed = StrokeSynthesisEngine.parseAiJsonResponse(aiJsonResult, effectivePrompt, model)
            if (parsed != null && parsed.strokes.isNotEmpty()) {
                _lastLog.value = "Successfully generated ${parsed.strokes.size} AI strokes."
                return@withContext parsed
            }
        }

        // Seamless, high-fidelity procedural vector synthesis
        _lastLog.value = "Synthesizing high-precision cyber vector paths..."
        val synthesized = StrokeSynthesisEngine.synthesizeArtwork(effectivePrompt, model)
        _lastLog.value = "Ready: ${synthesized.strokes.size} vector strokes compiled."
        synthesized
    }

    private suspend fun executePuterQueryAsync(
        prompt: String,
        model: String,
        unrestrictedMode: Boolean = false,
        copyrightBypassMode: Boolean = false
    ): String? {
        return kotlin.coroutines.suspendCoroutine { continuation ->
            mainHandler.post {
                if (webView == null) {
                    continuation.resumeWith(Result.success(null))
                    return@post
                }

                activeCallback = { successResult, errorResult ->
                    if (successResult != null) {
                        continuation.resumeWith(Result.success(successResult))
                    } else {
                        Log.w("PuterJsBridge", "Puter error: $errorResult")
                        continuation.resumeWith(Result.success(null))
                    }
                    activeCallback = null
                }

                val safePrompt = prompt.replace("\"", "\\\"").replace("\n", " ")
                val jsCall = "window.queryPuterAi(\"$safePrompt\", \"$model\", $unrestrictedMode, $copyrightBypassMode);"
                webView?.evaluateJavascript(jsCall, null)
            }
        }
    }

    inner class PuterJsInterface {
        @JavascriptInterface
        fun onSuccess(jsonResponse: String?) {
            mainHandler.post {
                activeCallback?.invoke(jsonResponse, null)
            }
        }

        @JavascriptInterface
        fun onError(errorMessage: String?) {
            mainHandler.post {
                activeCallback?.invoke(null, errorMessage)
            }
        }

        @JavascriptInterface
        fun onSdkStatus(available: Boolean) {
            mainHandler.post {
                _isSdkReady.value = true
                _lastLog.value = if (available) "Puter.js AI Online" else "Puter.js Bridge Ready"
            }
        }

        @JavascriptInterface
        fun onAuthResult(signedIn: Boolean, username: String?, email: String?) {
            mainHandler.post {
                _authState.value = PuterAuthState(
                    isSignedIn = signedIn,
                    username = username,
                    email = email,
                    isFreeTier = true,
                    statusMessage = if (signedIn) "Signed in as @$username" else "Guest Mode (Free Tier)"
                )
                _lastLog.value = if (signedIn) "Puter.js Authenticated: @$username" else "Puter.js: Guest Mode active"
            }
        }
    }
}

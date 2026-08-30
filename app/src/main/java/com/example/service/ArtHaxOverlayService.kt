package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ai.PuterJsBridge
import com.example.model.AiModelOption
import com.example.model.ArtHaxInstructionSet
import com.example.model.CalibrationBounds
import com.example.model.ChatMessage
import com.example.model.ChatSender
import com.example.model.DrawingSettings
import com.example.model.ExecutionState
import com.example.ui.components.CyberCanvas
import com.example.ui.components.DraggableCutoutBox
import com.example.ui.components.FloatingButtonWidget
import com.example.ui.components.OverlayChatWindow
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground Overlay Service that provides a floating bubble to toggle
 * the AI chat assistant, model selector dropdown, and draggable canvas crop box.
 */
class ArtHaxOverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var windowManager: WindowManager
    private lateinit var puterJsBridge: PuterJsBridge

    private val overlayLifecycleOwner = OverlayLifecycleOwner()

    // Views managed by WindowManager
    private var bubbleView: View? = null
    private var chatWindowView: View? = null
    private var previewOverlayView: View? = null

    // Layout Params
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var chatWindowParams: WindowManager.LayoutParams? = null
    private var previewParams: WindowManager.LayoutParams? = null

    // Reactive States
    private val _isChatExpanded = MutableStateFlow(true)
    private val _isChatVisible = MutableStateFlow(false)
    private val _promptText = MutableStateFlow("")
    private val _selectedModel = MutableStateFlow("claude-3-5-sonnet")
    private val _currentInstructionSet = MutableStateFlow<ArtHaxInstructionSet?>(null)
    private val _executionState = MutableStateFlow<ExecutionState>(ExecutionState.Idle)
    private val _drawingSettings = MutableStateFlow(DrawingSettings())
    private val _calibrationBounds = MutableStateFlow(CalibrationBounds())
    private val _isCanvasCropMode = MutableStateFlow(false)

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = ChatSender.ASSISTANT,
                text = "Hello! I am your AI Drawing Assistant. Tap 'Canvas' top-right to crop your paint area, then ask me what to draw!",
                modelName = "Claude 3.5 Sonnet"
            )
        )
    )

    private val availableModels = listOf(
        AiModelOption("claude-3-5-sonnet", "Claude 3.5 Sonnet", "Anthropic", "FREE", "Optimal for precise vector line paths", true, true),
        AiModelOption("gemini-2.0-flash", "Gemini 2.0 Flash", "Google", "FREE", "Ultra low latency drawing instructions", false, true),
        AiModelOption("deepseek-chat", "DeepSeek Chat", "DeepSeek", "FREE", "Crisp coordinate accuracy and smoothing", false, true),
        AiModelOption("gpt-4o", "GPT-4o Drawing", "OpenAI", "AUTH", "High detail complex anime & cyber art", false, false),
        AiModelOption("puter-art-v1", "Puter Art Matrix", "Puter", "FREE", "Native procedural vector drawing engine", false, true)
    )

    override fun onCreate() {
        super.onCreate()
        currentService = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        puterJsBridge = PuterJsBridge(this)
        overlayLifecycleOwner.onCreate()

        startForegroundServiceNotification()
        setupFloatingBubble()
        setupChatWindow()
        setupPreviewOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_OVERLAY) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        currentService = null
        overlayLifecycleOwner.onDestroy()
        removeViewSafely(bubbleView)
        removeViewSafely(chatWindowView)
        removeViewSafely(previewOverlayView)
    }

    private fun removeViewSafely(view: View?) {
        view?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // View already detached
            }
        }
    }

    // ==========================================
    // NOTIFICATION
    // ==========================================

    private fun startForegroundServiceNotification() {
        val channelId = "arthax_overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Art Assistant Floating Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java)
        val pendingOpen = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Art Assistant Active")
            .setContentText("Tap the floating bubble to chat with AI and crop paint canvas.")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentIntent(pendingOpen)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    // ==========================================
    // FLOATING BUBBLE (Always Accessible)
    // ==========================================

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingBubble() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 350
        }

        val view = createOverlayComposeView(this, overlayLifecycleOwner) {
            val execState by _executionState.collectAsState()
            val isVisible by _isChatVisible.collectAsState()

            MyApplicationTheme {
                FloatingButtonWidget(
                    executionState = execState,
                    isExpanded = isVisible,
                    onClick = {
                        if (execState is ExecutionState.Drawing) {
                            abortDrawing()
                        } else {
                            toggleChatVisibility()
                        }
                    }
                )
            }
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        var snapAnimator: android.animation.ValueAnimator? = null

        view.setOnTouchListener { _, event ->
            val params = bubbleParams ?: return@setOnTouchListener false
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            val bubbleW = view.width.takeIf { it > 0 } ?: (56 * displayMetrics.density).toInt()
            val bubbleH = view.height.takeIf { it > 0 } ?: (56 * displayMetrics.density).toInt()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    snapAnimator?.cancel()
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) {
                        isDragging = true
                        val newX = (initialX + dx).coerceIn(0, (screenWidth - bubbleW).coerceAtLeast(0))
                        val newY = (initialY + dy).coerceIn(40, (screenHeight - bubbleH - 40).coerceAtLeast(40))
                        params.x = newX
                        params.y = newY
                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (e: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        view.performClick()
                    } else {
                        val settings = _drawingSettings.value
                        if (settings.edgeHugging) {
                            val currentCenterX = params.x + (bubbleW / 2)
                            val targetX = if (currentCenterX < screenWidth / 2) {
                                20 // Hug left edge
                            } else {
                                (screenWidth - bubbleW - 20).coerceAtLeast(0) // Hug right edge
                            }

                            snapAnimator = android.animation.ValueAnimator.ofInt(params.x, targetX).apply {
                                duration = 220L
                                interpolator = android.view.animation.DecelerateInterpolator()
                                addUpdateListener { anim ->
                                    params.x = anim.animatedValue as Int
                                    try {
                                        windowManager.updateViewLayout(view, params)
                                    } catch (e: Exception) {}
                                }
                                start()
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }

        bubbleView = view
        windowManager.addView(view, bubbleParams)
    }

    // ==========================================
    // FLOATING CHAT WINDOW & MODEL DROPDOWN
    // ==========================================

    private fun setupChatWindow() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        chatWindowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            y = 0
        }

        val view = createOverlayComposeView(this, overlayLifecycleOwner) {
            val isVisible by _isChatVisible.collectAsState()
            val isExpanded by _isChatExpanded.collectAsState()
            val prompt by _promptText.collectAsState()
            val model by _selectedModel.collectAsState()
            val instructionSet by _currentInstructionSet.collectAsState()
            val execState by _executionState.collectAsState()
            val messages by _chatMessages.collectAsState()
            val isCanvasCrop by _isCanvasCropMode.collectAsState()
            val isPuterReady by puterJsBridge.isSdkReady.collectAsState()
            val puterAuth by puterJsBridge.authState.collectAsState()
            val liveModels by puterJsBridge.availableModels.collectAsState()
            val settings by _drawingSettings.collectAsState()

            if (isVisible) {
                MyApplicationTheme {
                    OverlayChatWindow(
                        isExpanded = isExpanded,
                        onToggleExpand = { _isChatExpanded.value = !_isChatExpanded.value },
                        onClose = { setChatVisible(false) },
                        messages = messages,
                        currentPrompt = prompt,
                        onPromptChange = { _promptText.value = it },
                        onSendPrompt = { sendPromptFromOverlay(it) },
                        availableModels = liveModels,
                        selectedModelId = model,
                        onSelectModel = { _selectedModel.value = it },
                        instructionSet = instructionSet,
                        executionState = execState,
                        onExecuteDraw = { executeDrawing() },
                        onAbortDraw = { abortDrawing() },
                        onToggleCanvasCrop = { toggleCanvasCropMode() },
                        isCanvasCropActive = isCanvasCrop,
                        isPuterSdkReady = isPuterReady,
                        puterAuthState = puterAuth,
                        drawingSettings = settings,
                        onSignInClick = {
                            val openIntent = Intent(this@ArtHaxOverlayService, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                putExtra("open_login_dialog", true)
                            }
                            startActivity(openIntent)
                        },
                        onRefreshModels = {
                            puterJsBridge.fetchLiveModels()
                        }
                    )
                }
            }
        }

        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE && _isChatVisible.value) {
                setChatVisible(false)
                true
            } else {
                false
            }
        }

        chatWindowView = view
        view.visibility = View.GONE
        windowManager.addView(view, chatWindowParams)
    }

    private fun setChatVisible(visible: Boolean) {
        _isChatVisible.value = visible
        val cView = chatWindowView ?: return
        val cParams = chatWindowParams ?: return

        if (visible) {
            cView.visibility = View.VISIBLE
            cParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        } else {
            cView.visibility = View.GONE
            cParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }

        try {
            windowManager.updateViewLayout(cView, cParams)
        } catch (e: Exception) {
            // Layout updated
        }
    }

    private fun toggleChatVisibility() {
        setChatVisible(!_isChatVisible.value)
    }

    // ==========================================
    // PREVIEW & CALIBRATION CANVAS OVERLAY
    // ==========================================

    private fun setupPreviewOverlay() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        previewParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val view = createOverlayComposeView(this, overlayLifecycleOwner) {
            val instructionSet by _currentInstructionSet.collectAsState()
            val execState by _executionState.collectAsState()
            val bounds by _calibrationBounds.collectAsState()
            val isCanvasCrop by _isCanvasCropMode.collectAsState()

            MyApplicationTheme {
                if (isCanvasCrop) {
                    DraggableCutoutBox(
                        bounds = bounds,
                        onBoundsChange = { _calibrationBounds.value = it },
                        onConfirmAndDraw = {
                            setCanvasCropMode(false)
                            executeDrawing()
                        },
                        onClose = {
                            setCanvasCropMode(false)
                        },
                        instructionSet = instructionSet,
                        executionState = execState
                    )
                } else if (execState is ExecutionState.Drawing) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
                        CyberCanvas(
                            instructionSet = instructionSet,
                            executionState = execState,
                            bounds = null,
                            showGrid = false,
                            transparentBackground = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        previewOverlayView = view
        view.visibility = View.GONE
        windowManager.addView(view, previewParams)
    }

    fun toggleCanvasCropMode() {
        setCanvasCropMode(!_isCanvasCropMode.value)
    }

    fun setCanvasCropMode(enabled: Boolean) {
        _isCanvasCropMode.value = enabled
        val pView = previewOverlayView ?: return
        val pParams = previewParams ?: return

        if (enabled) {
            pView.visibility = View.VISIBLE
            pParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            setChatVisible(false) // Hide chat window when positioning canvas
        } else {
            val isDrawing = _executionState.value is ExecutionState.Drawing
            if (isDrawing) {
                pView.visibility = View.VISIBLE
                pParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            } else {
                pView.visibility = View.GONE
                pParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
        }

        try {
            windowManager.updateViewLayout(pView, pParams)
        } catch (e: Exception) {
            // Updated
        }
    }

    // ==========================================
    // ACTIONS & AI GENERATION
    // ==========================================

    fun sendPromptFromOverlay(prompt: String) {
        if (prompt.isBlank()) return
        val model = _selectedModel.value
        val userMsg = ChatMessage(sender = ChatSender.USER, text = prompt)
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(userMsg)
        _chatMessages.value = currentList
        _promptText.value = ""

        serviceScope.launch {
            val settings = _drawingSettings.value
            _executionState.value = ExecutionState.Generating(0.3f, "Synthesizing vector paths via $model...")
            val result = puterJsBridge.generateDrawingInstructions(
                prompt = prompt,
                model = model,
                unrestrictedMode = settings.unrestrictedMode,
                copyrightBypassMode = settings.copyrightBypassMode
            )
            _currentInstructionSet.value = result
            _executionState.value = ExecutionState.Ready(result)

            val modelName = availableModels.find { it.id == model }?.name ?: model
            val statusNote = buildString {
                append("Generated ${result.strokes.size} vector strokes for '${result.title}'. Ready to draw in canvas!")
                if (settings.unrestrictedMode) {
                    append(" [Unrestricted]")
                }
                if (settings.copyrightBypassMode) {
                    append(" [Copyright Cleaner]")
                }
            }
            val aiMsg = ChatMessage(
                sender = ChatSender.ASSISTANT,
                text = statusNote,
                modelName = modelName,
                isInstructionGenerated = true,
                instructionSet = result
            )
            val updatedList = _chatMessages.value.toMutableList()
            updatedList.add(aiMsg)
            _chatMessages.value = updatedList
        }
    }

    fun executeDrawing() {
        val instructions = _currentInstructionSet.value ?: return
        val accessibility = ArtHaxAccessibilityService.instance

        if (accessibility == null) {
            _executionState.value = ExecutionState.Error("Accessibility Service is not enabled. Open main app to enable.")
            return
        }

        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)

        if (_drawingSettings.value.autoMinimizeOnExecute) {
            setChatVisible(false)
        }

        previewOverlayView?.visibility = View.VISIBLE
        previewParams?.let { p ->
            p.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            try { windowManager.updateViewLayout(previewOverlayView, p) } catch (e: Exception) {}
        }

        accessibility.startDrawing(
            instructionSet = instructions,
            bounds = _calibrationBounds.value,
            settings = _drawingSettings.value,
            screenWidth = metrics.widthPixels,
            screenHeight = metrics.heightPixels,
            onStateUpdate = { state ->
                _executionState.value = state
                if (state is ExecutionState.Completed || state is ExecutionState.Idle || state is ExecutionState.Error) {
                    if (!_isCanvasCropMode.value) {
                        previewOverlayView?.visibility = View.GONE
                    }
                }
            }
        )
    }

    fun abortDrawing() {
        ArtHaxAccessibilityService.instance?.abortCurrentDrawing()
        _executionState.value = ExecutionState.Idle
        if (!_isCanvasCropMode.value) {
            previewOverlayView?.visibility = View.GONE
        }
    }

    companion object {
        const val ACTION_START_OVERLAY = "com.example.action.START_OVERLAY"
        const val ACTION_STOP_OVERLAY = "com.example.action.STOP_OVERLAY"

        private var currentService: ArtHaxOverlayService? = null
        fun isRunning(): Boolean = currentService != null

        fun updateSettings(settings: DrawingSettings) {
            currentService?._drawingSettings?.value = settings
        }

        fun start(context: Context) {
            val intent = Intent(context, ArtHaxOverlayService::class.java).apply {
                action = ACTION_START_OVERLAY
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ArtHaxOverlayService::class.java).apply {
                action = ACTION_STOP_OVERLAY
            }
            context.stopService(intent)
        }
    }
}

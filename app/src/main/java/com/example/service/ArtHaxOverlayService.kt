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
import com.example.model.DrawingSettings
import com.example.model.ExecutionState
import com.example.model.SekaiPreset
import com.example.ui.components.CyberCanvas
import com.example.ui.components.DraggableCutoutBox
import com.example.ui.components.FloatingButtonWidget
import com.example.ui.components.OverlayHudSheet
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground Overlay Service that provides a non-intrusive floating button
 * to toggle the AI prompt hub and draggable canvas crop box on top of drawing apps.
 */
class ArtHaxOverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var windowManager: WindowManager
    private lateinit var puterJsBridge: PuterJsBridge

    private val overlayLifecycleOwner = OverlayLifecycleOwner()

    // Views managed by WindowManager
    private var bubbleView: View? = null
    private var hudView: View? = null
    private var previewOverlayView: View? = null

    // Layout Params
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var hudParams: WindowManager.LayoutParams? = null
    private var previewParams: WindowManager.LayoutParams? = null

    // Reactive States
    private val _isHudExpanded = MutableStateFlow(false)
    private val _isHudVisible = MutableStateFlow(false)
    private val _promptText = MutableStateFlow("Chibi Anime Character")
    private val _selectedModel = MutableStateFlow("claude-3-5-sonnet")
    private val _currentInstructionSet = MutableStateFlow<ArtHaxInstructionSet?>(null)
    private val _executionState = MutableStateFlow<ExecutionState>(ExecutionState.Idle)
    private val _drawingSettings = MutableStateFlow(DrawingSettings())
    private val _calibrationBounds = MutableStateFlow(CalibrationBounds())
    private val _isCalibrationMode = MutableStateFlow(false)

    private val availableModels = listOf(
        AiModelOption("claude-3-5-sonnet", "Claude 3.5 Sonnet", "Anthropic", "POPULAR", "Optimal for precise vector line paths", true, true),
        AiModelOption("gemini-2.0-flash", "Gemini 2.0 Flash", "Google", "ULTRA FAST", "Ultra low latency drawing instructions", false, true),
        AiModelOption("deepseek-chat", "DeepSeek Chat", "DeepSeek", "BALANCED", "Structured coordinate optimization", false, true),
        AiModelOption("gpt-4o", "GPT-4o Drawing", "OpenAI", "HD VECTORS", "High detail complex anime & cyber art", false, true),
        AiModelOption("puter-art-v1", "Puter Art Matrix", "Puter", "OFFLINE READY", "Native Puter.js drawing synthesis", false, true)
    )

    private val samplePresets = listOf(
        SekaiPreset("p1", "Chibi Miku", "Anime", "Sekai Chibi Hatsune Miku with twintails", "🎤", 18, "#3B82F6"),
        SekaiPreset("p2", "Cyber Skull", "Futuristic", "Futuristic cyberpunk skull with glowing optics", "💀", 16, "#EC4899"),
        SekaiPreset("p3", "Neon Neko", "Mascot", "Cyberpunk neon cat with robotic whiskers", "🐱", 14, "#10B981"),
        SekaiPreset("p4", "Neon Dragon", "Fantasy", "Serpentine neon dragon with horns", "🐉", 20, "#3B82F6"),
        SekaiPreset("p5", "Sakura", "Nature", "Detailed sakura blossom flower with petals", "🌸", 15, "#EC4899"),
        SekaiPreset("p6", "Retro Wave", "Synthwave", "Retro synthwave sunset with horizon grid", "🌅", 22, "#F59E0B"),
        SekaiPreset("p7", "Cyber Katana", "Weapons", "Glowing cyber samurai katana blade", "⚔️", 12, "#3B82F6"),
        SekaiPreset("p8", "Oni Mask", "Traditional", "Japanese cyber oni demon mask", "👹", 17, "#EC4899")
    )

    override fun onCreate() {
        super.onCreate()
        currentService = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        puterJsBridge = PuterJsBridge(this)
        overlayLifecycleOwner.onCreate()

        startForegroundServiceNotification()
        setupFloatingBubble()
        setupHudSheet()
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
        removeViewSafely(hudView)
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
            .setContentText("Tap the floating bubble to open AI prompt & crop tools.")
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
            val isVisible by _isHudVisible.collectAsState()

            MyApplicationTheme {
                FloatingButtonWidget(
                    executionState = execState,
                    isExpanded = isVisible,
                    onClick = {
                        if (execState is ExecutionState.Drawing) {
                            abortDrawing()
                        } else {
                            toggleHudVisibility()
                        }
                    }
                )
            }
        }

        // Touch listener for smooth dragging
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        view.setOnTouchListener { _, event ->
            val params = bubbleParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
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
                    if (kotlin.math.abs(dx) > 10 || kotlin.math.abs(dy) > 10) {
                        isDragging = true
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(view, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        view.performClick()
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
    // EXPANDABLE HUD SHEET
    // ==========================================

    private fun setupHudSheet() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        hudParams = WindowManager.LayoutParams(
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
            val isVisible by _isHudVisible.collectAsState()
            val isExpanded by _isHudExpanded.collectAsState()
            val prompt by _promptText.collectAsState()
            val model by _selectedModel.collectAsState()
            val instructionSet by _currentInstructionSet.collectAsState()
            val execState by _executionState.collectAsState()
            val settings by _drawingSettings.collectAsState()
            val isCalibrationMode by _isCalibrationMode.collectAsState()
            val isPuterReady by puterJsBridge.isSdkReady.collectAsState()
            val puterAuth by puterJsBridge.authState.collectAsState()

            if (isVisible) {
                MyApplicationTheme {
                    OverlayHudSheet(
                        isExpanded = isExpanded,
                        prompt = prompt,
                        onPromptChange = { _promptText.value = it },
                        selectedModel = model,
                        onModelSelect = { _selectedModel.value = it },
                        availableModels = availableModels,
                        presets = samplePresets,
                        onSelectPreset = { preset ->
                            _promptText.value = preset.prompt
                            generateStrokes(preset.prompt, model)
                        },
                        instructionSet = instructionSet,
                        executionState = execState,
                        settings = settings,
                        onUpdateSettings = { _drawingSettings.value = it },
                        onGenerate = { generateStrokes(prompt, model) },
                        onExecuteDraw = { executeDrawing() },
                        onAbortDraw = { abortDrawing() },
                        onToggleExpand = { _isHudExpanded.value = !_isHudExpanded.value },
                        onCloseOverlay = { setHudVisible(false) },
                        onToggleCalibrationMode = {
                            toggleCalibrationMode()
                        },
                        isCalibrationMode = isCalibrationMode,
                        isPuterSdkReady = isPuterReady,
                        puterAuthState = puterAuth
                    )
                }
            }
        }

        // Outside touch listener to close HUD when tapping elsewhere
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE && _isHudVisible.value) {
                setHudVisible(false)
                true
            } else {
                false
            }
        }

        hudView = view
        view.visibility = View.GONE
        windowManager.addView(view, hudParams)
    }

    private fun setHudVisible(visible: Boolean) {
        _isHudVisible.value = visible
        val hView = hudView ?: return
        val hParams = hudParams ?: return

        if (visible) {
            hView.visibility = View.VISIBLE
            hParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        } else {
            hView.visibility = View.GONE
            hParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }

        try {
            windowManager.updateViewLayout(hView, hParams)
        } catch (e: Exception) {
            // Updated
        }
    }

    private fun toggleHudVisibility() {
        setHudVisible(!_isHudVisible.value)
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
            val isCalibrating by _isCalibrationMode.collectAsState()

            MyApplicationTheme {
                if (isCalibrating) {
                    // Interactive Cutout Box allows freely dragging and resizing canvas over target app
                    DraggableCutoutBox(
                        bounds = bounds,
                        onBoundsChange = { _calibrationBounds.value = it },
                        onConfirmAndDraw = {
                            setCalibrationMode(false)
                            executeDrawing()
                        },
                        onClose = {
                            setCalibrationMode(false)
                        },
                        instructionSet = instructionSet,
                        executionState = execState
                    )
                } else if (execState is ExecutionState.Drawing) {
                    // Pure transparent vector stroke drawing layer (touch passthrough)
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

    fun toggleCalibrationMode() {
        setCalibrationMode(!_isCalibrationMode.value)
    }

    fun setCalibrationMode(enabled: Boolean) {
        _isCalibrationMode.value = enabled
        val pView = previewOverlayView ?: return
        val pParams = previewParams ?: return

        if (enabled) {
            // Make touchable so user can drag the cutout box
            pView.visibility = View.VISIBLE
            pParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            setHudVisible(false) // Minimize HUD when dragging cutout
        } else {
            // If drawing is not active, hide preview completely
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
            // View layout updated
        }
    }

    // ==========================================
    // ACTIONS & EXECUTION
    // ==========================================

    fun generateStrokes(prompt: String, model: String) {
        serviceScope.launch {
            _executionState.value = ExecutionState.Generating(0.2f, "Connecting to Puter.js AI...")
            val result = puterJsBridge.generateDrawingInstructions(prompt, model)
            _currentInstructionSet.value = result
            _executionState.value = ExecutionState.Ready(result)
        }
    }

    fun executeDrawing() {
        val instructions = _currentInstructionSet.value ?: return
        val accessibility = ArtHaxAccessibilityService.instance

        if (accessibility == null) {
            _executionState.value = ExecutionState.Error("Accessibility Service is not enabled. Open app to enable.")
            return
        }

        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)

        if (_drawingSettings.value.autoMinimizeOnExecute) {
            setHudVisible(false)
        }

        // Show transparent stroke preview during drawing
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
                    if (!_isCalibrationMode.value) {
                        previewOverlayView?.visibility = View.GONE
                    }
                }
            }
        )
    }

    fun abortDrawing() {
        ArtHaxAccessibilityService.instance?.abortCurrentDrawing()
        _executionState.value = ExecutionState.Idle
        if (!_isCalibrationMode.value) {
            previewOverlayView?.visibility = View.GONE
        }
    }

    companion object {
        const val ACTION_START_OVERLAY = "com.example.action.START_OVERLAY"
        const val ACTION_STOP_OVERLAY = "com.example.action.STOP_OVERLAY"

        private var currentService: ArtHaxOverlayService? = null
        fun isRunning(): Boolean = currentService != null

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

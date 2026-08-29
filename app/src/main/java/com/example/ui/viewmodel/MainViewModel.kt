package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.PuterJsBridge
import com.example.model.AiModelOption
import com.example.model.ArtHaxInstructionSet
import com.example.model.CalibrationBounds
import com.example.model.ChatMessage
import com.example.model.ChatSender
import com.example.model.DrawingPoint
import com.example.model.DrawingSettings
import com.example.model.DrawingStroke
import com.example.model.ExecutionState
import com.example.model.SekaiPreset
import com.example.service.ArtHaxAccessibilityService
import com.example.service.ArtHaxOverlayService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()
    val puterBridge = PuterJsBridge(context)

    val puterAuthState = puterBridge.authState

    // Permission and Service States
    private val _isOverlayPermissionGranted = MutableStateFlow(checkOverlayPermission())
    val isOverlayPermissionGranted: StateFlow<Boolean> = _isOverlayPermissionGranted.asStateFlow()

    private val _isAccessibilityServiceEnabled = MutableStateFlow(ArtHaxAccessibilityService.isRunning())
    val isAccessibilityServiceEnabled: StateFlow<Boolean> = _isAccessibilityServiceEnabled.asStateFlow()

    private val _isOverlayServiceRunning = MutableStateFlow(ArtHaxOverlayService.isRunning())
    val isOverlayServiceRunning: StateFlow<Boolean> = _isOverlayServiceRunning.asStateFlow()

    // Prompt & Model Selection
    private val _promptText = MutableStateFlow("Sekai Chibi Hatsune Miku")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _selectedModel = MutableStateFlow("claude-3-5-sonnet")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    val availableModels = listOf(
        AiModelOption("claude-3-5-sonnet", "Claude 3.5 Sonnet", "Anthropic", "FREE TIER", "Ultra-precise vector path geometry for anime", isRecommended = true, isFree = true),
        AiModelOption("gemini-2.0-flash", "Gemini 2.0 Flash", "Google", "FREE TIER", "Sub-second stroke compilation latency", isRecommended = false, isFree = true),
        AiModelOption("deepseek-chat", "DeepSeek Chat", "DeepSeek", "FREE TIER", "Crisp coordinate accuracy and smoothing", isRecommended = false, isFree = true),
        AiModelOption("gpt-4o", "GPT-4o Drawing", "OpenAI", "AUTH UNLOCKED", "Rich detail and complex multi-color strokes", isRecommended = false, isFree = false),
        AiModelOption("puter-art-v1", "Puter Art Matrix", "Puter.js", "OFFLINE FREE", "Native procedural vector drawing engine", isRecommended = false, isFree = true)
    )

    val presets = listOf(
        SekaiPreset("p1", "Chibi Miku", "Sekai Anime", "Sekai Chibi Hatsune Miku with twintails and microphone", "🎤", 18, "#00F0FF"),
        SekaiPreset("p2", "Cyber Skull", "Cyberpunk", "Cyber skull with glowing neon optics and circuits", "💀", 16, "#FF00E5"),
        SekaiPreset("p3", "Cyber Neko", "Mascot", "Cyberpunk neon cat with robotic whiskers and cyber eyes", "🐱", 14, "#00FF88"),
        SekaiPreset("p4", "Neon Dragon", "Fantasy", "Serpentine neon dragon with horns and flame breath", "🐉", 20, "#00F0FF"),
        SekaiPreset("p5", "Sakura Blossom", "Japanese", "Detailed cyber sakura blossom flower with petals", "🌸", 15, "#FF00E5"),
        SekaiPreset("p6", "Retro Sunset", "Vaporwave", "Retro synthwave sunset with 3D perspective grid", "🌅", 22, "#FFE600"),
        SekaiPreset("p7", "Cyber Katana", "Weapons", "Glowing cyber samurai katana blade with energy slash", "⚔️", 12, "#00F0FF"),
        SekaiPreset("p8", "Oni Demon Mask", "Cyberpunk", "Japanese cyber oni demon mask with tusks", "👹", 17, "#FF00E5")
    )

    // Drawing Blueprint State
    private val _currentInstructionSet = MutableStateFlow<ArtHaxInstructionSet?>(null)
    val currentInstructionSet: StateFlow<ArtHaxInstructionSet?> = _currentInstructionSet.asStateFlow()

    private val _executionState = MutableStateFlow<ExecutionState>(ExecutionState.Idle)
    val executionState: StateFlow<ExecutionState> = _executionState.asStateFlow()

    // Settings & Calibration
    private val _calibrationBounds = MutableStateFlow(CalibrationBounds())
    val calibrationBounds: StateFlow<CalibrationBounds> = _calibrationBounds.asStateFlow()

    private val _drawingSettings = MutableStateFlow(DrawingSettings())
    val drawingSettings: StateFlow<DrawingSettings> = _drawingSettings.asStateFlow()

    // Floating AI Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = ChatSender.ASSISTANT,
                text = "Hello! I am your ArtHax AI vector assistant. Ask me to draw anything or pick a preset!",
                modelName = "Claude 3.5 Sonnet"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private var activeSimJob: Job? = null

    init {
        // Generate initial preset artwork for rich immediate preview
        generateStrokes("Sekai Chibi Hatsune Miku", "claude-3-5-sonnet")
    }

    fun sendChatMessage(prompt: String) {
        if (prompt.isBlank()) return
        val model = _selectedModel.value
        val userMsg = ChatMessage(sender = ChatSender.USER, text = prompt)
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(userMsg)
        _chatMessages.value = currentList
        _promptText.value = prompt

        viewModelScope.launch {
            _executionState.value = ExecutionState.Generating(0.3f, "Synthesizing vector paths via $model...")
            val result = puterBridge.generateDrawingInstructions(prompt, model)
            _currentInstructionSet.value = result
            _executionState.value = ExecutionState.Ready(result)

            val modelName = availableModels.find { it.id == model }?.name ?: model
            val aiMsg = ChatMessage(
                sender = ChatSender.ASSISTANT,
                text = "Synthesized ${result.strokes.size} vector stroke paths for '$prompt'. Ready to draw!",
                modelName = modelName,
                isInstructionGenerated = true,
                instructionSet = result
            )
            val updatedList = _chatMessages.value.toMutableList()
            updatedList.add(aiMsg)
            _chatMessages.value = updatedList
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    fun refreshServiceStatus() {
        _isOverlayPermissionGranted.value = checkOverlayPermission()
        _isAccessibilityServiceEnabled.value = ArtHaxAccessibilityService.isRunning()
        _isOverlayServiceRunning.value = ArtHaxOverlayService.isRunning()
    }

    fun setPrompt(prompt: String) {
        _promptText.value = prompt
    }

    fun selectModel(modelId: String) {
        _selectedModel.value = modelId
    }

    fun updateSettings(settings: DrawingSettings) {
        _drawingSettings.value = settings
    }

    fun setCalibrationBounds(bounds: CalibrationBounds) {
        _calibrationBounds.value = bounds
    }

    fun updateCalibrationBounds(bounds: CalibrationBounds) {
        _calibrationBounds.value = bounds
    }

    fun generateStrokes(prompt: String = _promptText.value, model: String = _selectedModel.value) {
        viewModelScope.launch {
            _executionState.value = ExecutionState.Generating(0.3f, "Synthesizing vector paths via $model...")
            val result = puterBridge.generateDrawingInstructions(prompt, model)
            _currentInstructionSet.value = result
            _executionState.value = ExecutionState.Ready(result)
        }
    }

    fun selectPreset(preset: SekaiPreset) {
        _promptText.value = preset.prompt
        generateStrokes(preset.prompt, _selectedModel.value)
    }

    /**
     * Runs simulated stroke drawing inside the in-app sandbox canvas with live stylus pointer!
     */
    fun startSandboxSimulation() {
        val instructions = _currentInstructionSet.value ?: return
        activeSimJob?.cancel()

        activeSimJob = viewModelScope.launch {
            val totalStrokes = instructions.strokes.size
            val totalPoints = instructions.totalEstimatedPoints
            var drawnPoints = 0
            val startTime = System.currentTimeMillis()

            for (sIdx in 0 until totalStrokes) {
                val stroke = instructions.strokes[sIdx]
                for (pIdx in stroke.points.indices) {
                    val pt = stroke.points[pIdx]
                    drawnPoints++
                    val progress = drawnPoints.toFloat() / totalPoints.coerceAtLeast(1)

                    _executionState.value = ExecutionState.Drawing(
                        currentStrokeIndex = sIdx + 1,
                        totalStrokes = totalStrokes,
                        currentPointIndex = drawnPoints,
                        totalPoints = totalPoints,
                        progress = progress,
                        activePoint = pt,
                        activeColorHex = stroke.colorHex
                    )

                    val delayMs = (24L / _drawingSettings.value.speedMultiplier).toLong().coerceAtLeast(4L)
                    delay(delayMs)
                }
                delay((_drawingSettings.value.strokeDelayMs / _drawingSettings.value.speedMultiplier).toLong().coerceAtLeast(5L))
            }

            val dur = System.currentTimeMillis() - startTime
            _executionState.value = ExecutionState.Completed(totalStrokes, dur)
        }
    }

    fun abortSimulation() {
        activeSimJob?.cancel()
        activeSimJob = null
        ArtHaxAccessibilityService.instance?.abortCurrentDrawing()
        _executionState.value = ExecutionState.Idle
    }

    fun toggleOverlayService() {
        if (!checkOverlayPermission()) {
            openOverlaySettings()
            return
        }

        if (ArtHaxOverlayService.isRunning()) {
            ArtHaxOverlayService.stop(context)
            _isOverlayServiceRunning.value = false
        } else {
            ArtHaxOverlayService.start(context)
            _isOverlayServiceRunning.value = true
        }
    }

    fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun openOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun loginToPuter() {
        puterBridge.triggerSignIn()
    }

    fun logoutFromPuter() {
        puterBridge.triggerSignOut()
    }

    fun refreshPuterAuth() {
        puterBridge.refreshAuth()
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

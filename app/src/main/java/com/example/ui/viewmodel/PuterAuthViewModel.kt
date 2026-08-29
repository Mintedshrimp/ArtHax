package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.PuterJsBridge
import com.example.model.AiModelOption
import com.example.model.PuterAuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Dedicated ViewModel to manage Puter.js authentication flow,
 * account states, and available AI model listings.
 */
class PuterAuthViewModel(application: Application) : AndroidViewModel(application) {

    private val puterBridge = PuterJsBridge(application.applicationContext)

    // Bridge status state flows
    val authState: StateFlow<PuterAuthState> = puterBridge.authState
    val isSdkReady: StateFlow<Boolean> = puterBridge.isSdkReady
    val lastLog: StateFlow<String> = puterBridge.lastLog

    // UI Loading & Error feedback
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Master list of available Puter.js AI models
    private val _allModels = MutableStateFlow(
        listOf(
            AiModelOption(
                id = "claude-3-5-sonnet",
                name = "Claude 3.5 Sonnet",
                provider = "Anthropic",
                badge = "RECOMMENDED",
                description = "Ultra-precise vector geometry and clean stroke paths for anime/manga art.",
                isRecommended = true,
                isFree = true
            ),
            AiModelOption(
                id = "gemini-2.0-flash",
                name = "Gemini 2.0 Flash",
                provider = "Google",
                badge = "FASTEST",
                description = "Sub-second stroke compilation latency with responsive live feedback.",
                isRecommended = false,
                isFree = true
            ),
            AiModelOption(
                id = "deepseek-chat",
                name = "DeepSeek Chat (V3)",
                provider = "DeepSeek",
                badge = "FREE TIER",
                description = "Crisp coordinate accuracy, smooth curves, and clean hatch lines.",
                isRecommended = false,
                isFree = true
            ),
            AiModelOption(
                id = "gpt-4o",
                name = "GPT-4o Drawing",
                provider = "OpenAI",
                badge = "AUTH UNLOCKED",
                description = "Rich detail, intricate multi-color strokes, and complex compositions.",
                isRecommended = false,
                isFree = false
            ),
            AiModelOption(
                id = "gpt-4o-mini",
                name = "GPT-4o Mini",
                provider = "OpenAI",
                badge = "FREE TIER",
                description = "Lightweight OpenAI model optimized for quick geometric sketching.",
                isRecommended = false,
                isFree = true
            ),
            AiModelOption(
                id = "claude-3-haiku",
                name = "Claude 3 Haiku",
                provider = "Anthropic",
                badge = "FAST",
                description = "Compact Claude model for rapid vector stroke synthesis.",
                isRecommended = false,
                isFree = true
            ),
            AiModelOption(
                id = "puter-art-v1",
                name = "Puter Art Matrix (Offline)",
                provider = "Puter.js",
                badge = "NATIVE",
                description = "Native procedural vector engine running locally on device.",
                isRecommended = false,
                isFree = true
            )
        )
    )
    val allModels: StateFlow<List<AiModelOption>> = _allModels.asStateFlow()

    // Active Search Query for filtering models
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Active Provider filter (null = all)
    private val _selectedProviderFilter = MutableStateFlow<String?>(null)
    val selectedProviderFilter: StateFlow<String?> = _selectedProviderFilter.asStateFlow()

    // Filtered models stream
    val filteredModels: StateFlow<List<AiModelOption>> = combine(
        _allModels,
        _searchQuery,
        _selectedProviderFilter
    ) { models, query, provider ->
        models.filter { model ->
            val matchesQuery = query.isBlank() ||
                    model.name.contains(query, ignoreCase = true) ||
                    model.provider.contains(query, ignoreCase = true) ||
                    model.description.contains(query, ignoreCase = true)

            val matchesProvider = provider.isNullOrBlank() || model.provider.equals(provider, ignoreCase = true)

            matchesQuery && matchesProvider
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _allModels.value
    )

    // Currently selected AI model
    private val _selectedModel = MutableStateFlow(_allModels.value.first())
    val selectedModel: StateFlow<AiModelOption> = _selectedModel.asStateFlow()

    /**
     * Trigger Puter.js sign-in popup flow via WebBridge
     */
    fun signIn() {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                puterBridge.triggerSignIn()
            } catch (e: Exception) {
                _authError.value = "Sign in failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Trigger Puter.js sign-out flow
     */
    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                puterBridge.triggerSignOut()
            } catch (e: Exception) {
                _authError.value = "Sign out failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh the Puter.js user auth state
     */
    fun refreshAuthStatus() {
        puterBridge.refreshAuth()
    }

    /**
     * Select a model by ID
     */
    fun selectModel(modelId: String) {
        val found = _allModels.value.find { it.id == modelId }
        if (found != null) {
            _selectedModel.value = found
        }
    }

    /**
     * Update model search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Set provider filter (e.g., "Anthropic", "Google", "OpenAI", "DeepSeek", "Puter.js")
     */
    fun setProviderFilter(provider: String?) {
        _selectedProviderFilter.value = provider
    }

    /**
     * Dismiss error state
     */
    fun clearError() {
        _authError.value = null
    }

    /**
     * Get instance of underlying bridge for direct queries
     */
    fun getBridge(): PuterJsBridge = puterBridge
}

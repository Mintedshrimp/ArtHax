package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PuterAuthViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val puterAuthViewModel: PuterAuthViewModel by viewModels()
    private var openLoginDialogState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openLoginDialogState = intent?.getBooleanExtra("open_login_dialog", false) == true
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(
                    viewModel = viewModel,
                    puterAuthViewModel = puterAuthViewModel,
                    initialShowLoginDialog = openLoginDialogState
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("open_login_dialog", false)) {
            openLoginDialogState = true
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshServiceStatus()
        puterAuthViewModel.refreshAuthStatus()
    }
}



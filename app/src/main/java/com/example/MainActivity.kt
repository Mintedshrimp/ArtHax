package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PuterAuthViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val puterAuthViewModel: PuterAuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(
                    viewModel = viewModel,
                    puterAuthViewModel = puterAuthViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshServiceStatus()
        puterAuthViewModel.refreshAuthStatus()
    }
}



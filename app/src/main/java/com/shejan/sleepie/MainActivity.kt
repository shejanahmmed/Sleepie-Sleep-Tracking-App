package com.shejan.sleepie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.shejan.sleepie.ui.screens.MainScreen
import com.shejan.sleepie.ui.theme.SleepieTheme
import com.shejan.sleepie.ui.viewmodel.SleepViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val viewModel = ViewModelProvider(this)[SleepViewModel::class.java]
        
        setContent {
            SleepieTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
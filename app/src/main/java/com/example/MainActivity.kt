package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.FastToSwapApp
import com.example.viewmodel.MotdViewModel

class MainActivity : ComponentActivity() {
    private val motdViewModel: MotdViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FastToSwapApp(viewModel = motdViewModel)
        }
    }
}

package com.example.nextstoprealtime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.nextstoprealtime.ui.MainViewModel
import com.example.nextstoprealtime.ui.NextStopApp
import com.example.nextstoprealtime.ui.NextStopTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NextStopTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NextStopApp(viewModel = viewModel)
                }
            }
        }
    }
}

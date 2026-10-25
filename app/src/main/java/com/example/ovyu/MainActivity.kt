package com.example.ovyu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ovyu.compose.OvyuApp
import com.example.ovyu.ui.theme.OvyuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OvyuTheme {
                OvyuApp()
            }
        }
    }
}

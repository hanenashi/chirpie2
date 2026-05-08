package com.hanenashi.chirpie2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.hanenashi.chirpie2.ui.screens.BirdListScreen
import com.hanenashi.chirpie2.ui.theme.ChirpieTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChirpieTheme {
                Surface {
                    BirdListScreen()
                }
            }
        }
    }
}

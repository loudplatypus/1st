package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.CouplesAppUi
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CoupleViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CoupleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(themeName = viewModel.themeMode.value) {
                CouplesAppUi(viewModel = viewModel)
            }
        }
    }
}

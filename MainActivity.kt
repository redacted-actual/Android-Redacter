package com.redactedactual.redacter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.redactedactual.redacter.ui.DocumentRedactorScreen
import com.redactedactual.redacter.ui.theme.RedacterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RedacterTheme {
                DocumentRedactorScreen(context = this)
            }
        }
    }
}

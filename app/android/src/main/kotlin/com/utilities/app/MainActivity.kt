package com.utilities.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.utilities.app.ui.navigation.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UtilitiesApp()
                }
            }
        }
    }
}

@Composable
fun UtilitiesApp() {
    val navController = rememberNavController()
    NavGraph(navController = navController)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MaterialTheme {
        UtilitiesApp()
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.CalculatorDatabase
import com.example.data.repository.HistoryRepository
import com.example.ui.MainApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.CalculatorViewModelFactory
import android.graphics.Color as AndroidColor

// Force rebuild to refresh emulator
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    window.statusBarColor = AndroidColor.parseColor("#6366F1")
    window.navigationBarColor = AndroidColor.parseColor("#6366F1")
    WindowCompat.getInsetsController(window, window.decorView)?.apply {
      isAppearanceLightStatusBars = false
      isAppearanceLightNavigationBars = false
    }

    // Initialize database and repository
    val database = CalculatorDatabase.getDatabase(this)
    val repository = HistoryRepository(database.historyDao())
    
    // Create ViewModel
    val viewModelFactory = CalculatorViewModelFactory(repository, this)
    val viewModel = ViewModelProvider(this, viewModelFactory)[CalculatorViewModel::class.java]

    setContent {
      MyApplicationTheme {
        MainApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}

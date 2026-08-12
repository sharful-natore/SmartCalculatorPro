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
import android.content.Intent
import android.graphics.Color as AndroidColor

// Force rebuild to refresh emulator
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: CalculatorViewModel

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
        viewModel = ViewModelProvider(this, viewModelFactory)[CalculatorViewModel::class.java]

        handleShortcutIntent(intent)

        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent?) {
        val targetTab = intent?.getStringExtra("target_tab")
        if (targetTab != null) {
            when (targetTab) {
                "dashboard" -> {
                    viewModel.activeTab = 0
                }
                "converter" -> {
                    viewModel.activeTab = 1
                }
                "calculator" -> {
                    viewModel.changeActiveTab(
                        2,
                        "Opened via Launcher Shortcut Intent",
                        "লঞ্চার শর্টকাট ইন্টেন্ট থেকে ক্যালকুলেটর খোলা হয়েছে"
                    )
                }
                "history" -> {
                    viewModel.activeTab = 3
                }
            }
            intent.removeExtra("target_tab")
            setIntent(Intent())
        } else {
            // ALWAYS default to Dashboard (0) on app launch
            viewModel.activeTab = 0
            intent?.removeExtra("target_tab")
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

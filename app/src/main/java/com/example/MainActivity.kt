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

        // Explicitly set default active tab to 0 (Dashboard) on activity creation
        viewModel.activeTab = 0

        android.util.Log.d("MainActivity", "Intent: ${intent.action}, data: ${intent.dataString}, extras: ${intent.extras}")
        if (savedInstanceState == null) {
            handleShortcutIntent(intent)
        } else {
            viewModel.activeTab = 0
        }

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
        val navigateTo = intent?.getStringExtra("NAVIGATE_TO")
        val notificationSurahNumber = intent?.getIntExtra("SURAH_NUMBER", -1) ?: -1
        if (navigateTo == "HOLY_QURAN") {
            viewModel.activeTab = 0
            viewModel.openTool(com.example.data.model.ToolType.HOLY_QURAN)
            if (notificationSurahNumber > 0) {
                try {
                    val quranViewModel = ViewModelProvider(this)[com.example.ui.quran.QuranViewModel::class.java]
                    quranViewModel.selectSurahByNumber(notificationSurahNumber)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            intent.removeExtra("NAVIGATE_TO")
            intent.removeExtra("SURAH_NUMBER")
            setIntent(Intent())
            return
        }

        val targetTool = intent?.getStringExtra("target_tool")
        val targetConverter = intent?.getStringExtra("target_converter")
        val targetTab = intent?.getStringExtra("target_tab")

        if (targetTool != null) {
            try {
                val toolType = com.example.data.model.ToolType.valueOf(targetTool)
                viewModel.activeTab = 0
                viewModel.openTool(toolType)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            intent.removeExtra("target_tool")
            setIntent(Intent())
        } else if (targetConverter != null) {
            try {
                val converterType = com.example.data.model.ConverterType.valueOf(targetConverter)
                viewModel.activeTab = 1
                viewModel.openConverter(converterType)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            intent.removeExtra("target_converter")
            setIntent(Intent())
        } else if (targetTab != null) {
            when (targetTab) {
                "dashboard" -> {
                    viewModel.activeTab = 0
                }
                "converter" -> {
                    viewModel.activeTab = 1
                }
                "calculator" -> {
                    viewModel.activeTab = 0
                    viewModel.showCalculatorDialog = true
                }
                "calendar" -> {
                    viewModel.activeTab = 0
                    viewModel.showCalendarDialog = true
                }
                "market", "bazaar" -> {
                    viewModel.activeTab = 0
                    viewModel.showMarketDialog = true
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

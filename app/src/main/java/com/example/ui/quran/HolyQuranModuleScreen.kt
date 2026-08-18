package com.example.ui.quran

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.CalculatorThemeColors

@Composable
fun HolyQuranModuleScreen(
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit,
    quranViewModel: QuranViewModel = viewModel(),
    isBn: Boolean = true
) {
    val selectedSurah by quranViewModel.selectedSurah.collectAsStateWithLifecycle()

    if (selectedSurah != null) {
        SurahDetailScreen(
            surah = selectedSurah!!,
            viewModel = quranViewModel,
            themeColors = themeColors,
            onBackClick = {
                quranViewModel.clearSelectedSurah()
            },
            isBn = isBn
        )
    } else {
        QuranScreen(
            viewModel = quranViewModel,
            themeColors = themeColors,
            onBackClick = onBackClick,
            onSurahClick = { surah ->
                quranViewModel.selectSurah(surah)
            },
            isBn = isBn
        )
    }
}

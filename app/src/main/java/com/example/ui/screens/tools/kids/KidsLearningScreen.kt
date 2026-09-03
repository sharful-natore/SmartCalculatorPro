package com.example.ui.screens.tools.kids

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CalculatorThemeColors
import com.example.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsLearningScreen(
    viewModel: CalculatorViewModel,
    themeColors: CalculatorThemeColors,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val audioPlayer = remember { KidsAudioPlayer(context) }
    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("kids_learning_stars_v1", Context.MODE_PRIVATE) }
    var totalStars by remember { mutableStateOf(prefs.getInt("total_stars", 25)) }

    fun addStars(amount: Int) {
        val newTotal = totalStars + amount
        totalStars = newTotal
        prefs.edit().putInt("total_stars", newTotal).apply()
        audioPlayer.playCelebrationSound()
    }

    var activeTab by remember { mutableStateOf(KidsSectionTab.ALPHABET) }
    var slateInitialLetter by remember { mutableStateOf<String?>(null) }

    // Sub-Category States for each learning section
    var alphabetCategory by remember { mutableStateOf(AlphabetCategory.BANGLA_VOWEL) }
    var isAlphabetRandom by remember { mutableStateOf(false) }
    var alphabetShuffleSeed by remember { mutableStateOf(0) }

    var spellingMode by remember { mutableStateOf(SpellingTabMode.WORDS) }
    var spellingWordCategory by remember { mutableStateOf(SpellingCategory.BANGLA_TWO_LETTER) }
    var phonicsSubCategory by remember { mutableStateOf(PhonicsSubCategory.ALPHABET_SOUNDS) }

    var mathSubTab by remember { mutableStateOf(0) }
    var natureCategory by remember { mutableStateOf(NatureCategory.ANIMALS) }
    var rhymesShowEnglish by remember { mutableStateOf(false) }

    // Scroll visibility state for Top Bar, Top Tabs, and Bottom Switcher
    var isControlsVisible by remember { mutableStateOf(true) }

    val scrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (available.y < -12f) {
                    // Scrolling down into content (gesture upwards) -> Hide headers and chips for full-screen content
                    isControlsVisible = false
                } else if (available.y > 12f) {
                    // Scrolling up (gesture downwards) -> Show switcher chips to allow switching content
                    isControlsVisible = true
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    // Child Lock State
    var isChildLocked by remember { mutableStateOf(false) }
    var showUnlockHintDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Scroll Behavior for auto-collapsing header on scroll
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Android System Back Button Handler
    BackHandler(enabled = true) {
        if (isChildLocked) {
            showUnlockHintDialog = true
            audioPlayer.speak("চাইল্ড লক অন রয়েছে। আনলক করতে লাল তালার বাটনটি ৩ সেকেন্ড চেপে রাখুন।", isBn = true)
        } else if (activeTab != KidsSectionTab.ALPHABET) {
            audioPlayer.playClickSound()
            activeTab = KidsSectionTab.ALPHABET
        } else {
            audioPlayer.playClickSound()
            onBackClick()
        }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .nestedScroll(scrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Column {
                        Text(
                            text = "কিডস লার্নিং",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.titleBarText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activeTab.titleBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.titleBarText.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isChildLocked) {
                                showUnlockHintDialog = true
                                audioPlayer.speak("চাইল্ড লক অন রয়েছে। আনলক করতে বাটনটি ৩ সেকেন্ড চেপে রাখুন।", isBn = true)
                            } else {
                                audioPlayer.playClickSound()
                                onBackClick()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = themeColors.titleBarText
                        )
                    }
                },
                actions = {
                    // Star Counter Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⭐", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$totalStars",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Audio Mute/Unmute Toggle
                    IconButton(
                        modifier = Modifier.size(38.dp),
                        onClick = {
                            audioPlayer.isMuted = !audioPlayer.isMuted
                            if (!audioPlayer.isMuted) {
                                audioPlayer.playSuccessChime()
                                audioPlayer.speak("সাউন্ড চালু হয়েছে", isBn = true)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (audioPlayer.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Toggle Mute",
                            tint = if (audioPlayer.isMuted) Color(0xFFFF5252) else themeColors.titleBarText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Child Lock Button
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .pointerInput(isChildLocked) {
                                detectTapGestures(
                                    onTap = {
                                        if (!isChildLocked) {
                                             isChildLocked = true
                                             audioPlayer.playSuccessChime()
                                             audioPlayer.speak("চাইল্ড লক চালু হয়েছে", isBn = true)
                                        } else {
                                             showUnlockHintDialog = true
                                        }
                                    },
                                    onLongPress = {
                                        if (isChildLocked) {
                                             isChildLocked = false
                                             audioPlayer.playCelebrationSound()
                                             audioPlayer.speak("চাইল্ড লক আনলক হয়েছে", isBn = true)
                                        }
                                    }
                                )
                            }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isChildLocked) Color(0xFFD32F2F) else Color.White.copy(alpha = 0.22f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isChildLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Child Lock",
                                    tint = if (isChildLocked) Color.White else themeColors.titleBarText,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    // Info Button
                    IconButton(
                        modifier = Modifier.size(38.dp),
                        onClick = { showInfoDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Kids Info",
                            tint = themeColors.titleBarText.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColors.titleBarBg,
                    scrolledContainerColor = themeColors.titleBarBg,
                    navigationIconContentColor = themeColors.titleBarText,
                    titleContentColor = themeColors.titleBarText,
                    actionIconContentColor = themeColors.titleBarText
                )
            )
        },
        containerColor = themeColors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Tab Navigation (Hides when scrolling down into content)
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeColors.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(KidsSectionTab.values()) { tab ->
                        val isSelected = activeTab == tab
                        val tabBg = if (isSelected) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.45f)
                        val contentColor = if (isSelected) themeColors.onAccent else themeColors.onSurface

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = tabBg,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .height(42.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    audioPlayer.playClickSound()
                                    activeTab = tab
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.titleBn,
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.titleBn,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.5.sp
                                    ),
                                    color = contentColor
                                )
                            }
                        }
                    }
                }
            }

            if (isControlsVisible) {
                Divider(color = themeColors.onSurface.copy(alpha = 0.08f), thickness = 1.dp)
            }

            // Dynamic Tab Content with Floating Bottom Sub-Switcher
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (activeTab) {
                    KidsSectionTab.ALPHABET -> KidsAlphabetTab(
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        selectedCategory = alphabetCategory,
                        onCategoryChange = { alphabetCategory = it },
                        isRandomOrder = isAlphabetRandom,
                        onToggleRandomOrder = {
                            isAlphabetRandom = !isAlphabetRandom
                            if (isAlphabetRandom) {
                                alphabetShuffleSeed++
                                audioPlayer.speak("এলোমেলো প্র্যাকটিস চালু হয়েছে।", isBn = true)
                            } else {
                                audioPlayer.speak("ধারাবাহিক ক্রম চালু হয়েছে।", isBn = true)
                            }
                        },
                        onReshuffle = {
                            alphabetShuffleSeed++
                            audioPlayer.speak("বর্ণমালা আবার এলোমেলো করা হয়েছে!", isBn = true)
                        },
                        shuffleSeed = alphabetShuffleSeed,
                        onTraceLetter = { letter ->
                            slateInitialLetter = letter
                            activeTab = KidsSectionTab.SLATE
                        }
                    )
                    KidsSectionTab.SPELLING -> KidsSpellingTab(
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        mainMode = spellingMode,
                        onMainModeChange = { spellingMode = it },
                        selectedCategory = spellingWordCategory,
                        onCategoryChange = { spellingWordCategory = it },
                        selectedPhonicsCategory = phonicsSubCategory,
                        onPhonicsCategoryChange = { phonicsSubCategory = it },
                        onRewardStars = { amount -> addStars(amount) }
                    )
                    KidsSectionTab.MATH -> KidsMathTab(
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        subTab = mathSubTab,
                        onSubTabChange = { mathSubTab = it },
                        onRewardStars = { amount -> addStars(amount) }
                    )
                    KidsSectionTab.RHYMES -> KidsRhymesTab(
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        showEnglish = rhymesShowEnglish,
                        onShowEnglishChange = { rhymesShowEnglish = it },
                        onRewardStars = { amount -> addStars(amount) }
                    )
                    KidsSectionTab.NATURE -> KidsNatureTab(
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        selectedCategory = natureCategory,
                        onCategoryChange = { natureCategory = it },
                        onRewardStars = { amount -> addStars(amount) }
                    )
                    KidsSectionTab.SLATE -> KidsSlateTab(
                        initialTraceLetter = slateInitialLetter,
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onRewardStars = { amount -> addStars(amount) }
                    )
                    KidsSectionTab.QUIZ -> KidsQuizTab(
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onRewardStars = { amount -> addStars(amount) }
                    )
                }

                // Floating Bottom Content Switcher Bar (Visible on scroll up, hidden on scroll down)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isControlsVisible && (activeTab != KidsSectionTab.SLATE && activeTab != KidsSectionTab.QUIZ),
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    KidsBottomSubSwitcherBar(
                        activeTab = activeTab,
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        alphabetCategory = alphabetCategory,
                        onAlphabetCategoryChange = { alphabetCategory = it },
                        isAlphabetRandom = isAlphabetRandom,
                        onToggleAlphabetRandom = {
                            isAlphabetRandom = !isAlphabetRandom
                            if (isAlphabetRandom) {
                                alphabetShuffleSeed++
                                audioPlayer.speak("এলোমেলো প্র্যাকটিস চালু হয়েছে।", isBn = true)
                            } else {
                                audioPlayer.speak("ধারাবাহিক ক্রম চালু হয়েছে।", isBn = true)
                            }
                        },
                        onAlphabetReshuffle = {
                            alphabetShuffleSeed++
                            audioPlayer.speak("বর্ণমালা আবার এলোমেলো করা হয়েছে!", isBn = true)
                        },
                        spellingMode = spellingMode,
                        onSpellingModeChange = { spellingMode = it },
                        spellingWordCategory = spellingWordCategory,
                        onSpellingWordCategoryChange = { spellingWordCategory = it },
                        phonicsSubCategory = phonicsSubCategory,
                        onPhonicsSubCategoryChange = { phonicsSubCategory = it },
                        mathSubTab = mathSubTab,
                        onMathSubTabChange = { mathSubTab = it },
                        rhymesShowEnglish = rhymesShowEnglish,
                        onRhymesShowEnglishChange = { rhymesShowEnglish = it },
                        natureCategory = natureCategory,
                        onNatureCategoryChange = { natureCategory = it }
                    )
                }
            }
        }
    }

    // Child Lock Help Dialog
    if (showUnlockHintDialog) {
        Dialog(onDismissRequest = { showUnlockHintDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = themeColors.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color(0xFFD32F2F).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFD32F2F), modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "চাইল্ড লক সক্রিয় রয়েছে 🔒",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "বাচ্চাদের অনিচ্ছাকৃত ব্যাক প্রেস রোধ করতে লক করা হয়েছে। আনলক করতে উপরের লাল তালার আইকনটিতে ৩ সেকেন্ড চেপে ধরে রাখুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = themeColors.onSurface.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { showUnlockHintDialog = false },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.accent,
                            contentColor = themeColors.onAccent
                        )
                    ) {
                        Text(text = "ঠিক আছে", fontWeight = FontWeight.Bold, color = themeColors.onAccent)
                    }
                }
            }
        }
    }

    // Info & Parental Guidance Dialog
    if (showInfoDialog) {
        Dialog(onDismissRequest = { showInfoDialog = false }) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = themeColors.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "অভিভাবক নির্দেশিকা 👶",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.onSurface
                        )
                        IconButton(onClick = { showInfoDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "কিডস লার্নিং কেন শিশুদের জন্য সেরা?\n\n" +
                               "• শতভাগ অফলাইন ও নিরাপদ: কোনো ইন্টারনেট কানেকশন বা বিজ্ঞাপন নেই।\n" +
                               "• বানান করে পড়া ও ফনিক্স: বাংলা ও ইংরেজি প্রতিটি শব্দ বর্ণ, কারচিহ্ন ও Phonics সাউন্ড ভেঙ্গে ভেঙ্গে উচ্চারণ করে শেখায়।\n" +
                               "• টেক্সট-টু-স্পিচ উচ্চারণ: প্রতিটি বর্ণ, শব্দ, নামতা ও ছড়ায় শিশুতোষ স্পষ্ট উচ্চারণ।\n" +
                               "• ম্যাজিক স্লেট: ডটেড লাইনের ওপর হাত ঘুরিয়ে নিজে লেখার অভ্যাস।\n" +
                               "• চাইল্ড লক: বাচ্চা খেলতে খেলতে ভুলে যাতে অ্যাপ কেটে না ফেলে সেজন্য ১-ট্যাপ লক।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.onSurface.copy(alpha = 0.85f),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { showInfoDialog = false },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.accent,
                            contentColor = themeColors.onAccent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "ধন্যবাদ", fontWeight = FontWeight.Bold, color = themeColors.onAccent)
                    }
                }
            }
        }
    }
}

@Composable
fun KidsBottomSubSwitcherBar(
    activeTab: KidsSectionTab,
    themeColors: CalculatorThemeColors,
    audioPlayer: KidsAudioPlayer,
    alphabetCategory: AlphabetCategory,
    onAlphabetCategoryChange: (AlphabetCategory) -> Unit,
    isAlphabetRandom: Boolean,
    onToggleAlphabetRandom: () -> Unit,
    onAlphabetReshuffle: () -> Unit,
    spellingMode: SpellingTabMode,
    onSpellingModeChange: (SpellingTabMode) -> Unit,
    spellingWordCategory: SpellingCategory,
    onSpellingWordCategoryChange: (SpellingCategory) -> Unit,
    phonicsSubCategory: PhonicsSubCategory,
    onPhonicsSubCategoryChange: (PhonicsSubCategory) -> Unit,
    mathSubTab: Int,
    onMathSubTabChange: (Int) -> Unit,
    rhymesShowEnglish: Boolean,
    onRhymesShowEnglishChange: (Boolean) -> Unit,
    natureCategory: NatureCategory,
    onNatureCategoryChange: (NatureCategory) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = themeColors.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, themeColors.accent.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
    ) {
        when (activeTab) {
            KidsSectionTab.ALPHABET -> {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(AlphabetCategory.values()) { cat ->
                        val isSelected = alphabetCategory == cat
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.5f),
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)) else null,
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    audioPlayer.playClickSound()
                                    onAlphabetCategoryChange(cat)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                                Text(
                                    text = cat.titleBn,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    item {
                        // Random / Sequential Mode Chip
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isAlphabetRandom) Color(0xFF673AB7) else themeColors.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    audioPlayer.playClickSound()
                                    onToggleAlphabetRandom()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isAlphabetRandom) "🔀 এলোমেলো" else "📑 ধারাবাহিক",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isAlphabetRandom) Color.White else themeColors.onSurface
                                )
                            }
                        }
                    }

                    if (isAlphabetRandom) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF673AB7).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF673AB7)),
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        audioPlayer.playClickSound()
                                        onAlphabetReshuffle()
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                    Text(text = "🔄 শাফল", color = Color(0xFF673AB7), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }
            }
            KidsSectionTab.SPELLING -> {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mode Toggle (Words vs Phonics)
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = themeColors.accent.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, themeColors.accent),
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    audioPlayer.playClickSound()
                                    val nextMode = if (spellingMode == SpellingTabMode.WORDS) SpellingTabMode.PHONICS else SpellingTabMode.WORDS
                                    onSpellingModeChange(nextMode)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (spellingMode == SpellingTabMode.WORDS) "📖 শব্দ ⇄ 🗣️ ফনিক্স" else "🗣️ ফনিক্স ⇄ 📖 শব্দ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = themeColors.accent
                                )
                            }
                        }
                    }

                    if (spellingMode == SpellingTabMode.WORDS) {
                        items(SpellingCategory.values()) { cat ->
                            val isSelected = spellingWordCategory == cat
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.5f),
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)) else null,
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        audioPlayer.playClickSound()
                                        onSpellingWordCategoryChange(cat)
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                                    Text(
                                        text = cat.titleBn,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.5.sp
                                        ),
                                        color = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    } else {
                        items(PhonicsSubCategory.values()) { subCat ->
                            val isSelected = phonicsSubCategory == subCat
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.5f),
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)) else null,
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        audioPlayer.playClickSound()
                                        onPhonicsSubCategoryChange(subCat)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = subCat.icon, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = subCat.titleBn,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.5.sp
                                        ),
                                        color = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
            KidsSectionTab.MATH -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("🔢 সংখ্যা ও গণনা (১-২০)", "✖️ নামতার পাঠশালা (১-১০)")
                    tabs.forEachIndexed { idx, title ->
                        val isSelected = mathSubTab == idx
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.5f),
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.15f)) else null,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    audioPlayer.playClickSound()
                                    onMathSubTabChange(idx)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSelected) themeColors.onAccent else themeColors.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            KidsSectionTab.RHYMES -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isBnSelected = !rhymesShowEnglish
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isBnSelected) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.5f),
                        border = if (!isBnSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.15f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                audioPlayer.playClickSound()
                                onRhymesShowEnglishChange(false)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🎶 বাংলা ছড়া ও গান",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isBnSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp
                                ),
                                color = if (isBnSelected) themeColors.onAccent else themeColors.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (rhymesShowEnglish) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.5f),
                        border = if (!rhymesShowEnglish) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.15f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                audioPlayer.playClickSound()
                                onRhymesShowEnglishChange(true)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🇬🇧 English Rhymes",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (rhymesShowEnglish) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp
                                ),
                                color = if (rhymesShowEnglish) themeColors.onAccent else themeColors.onSurface
                            )
                        }
                    }
                }
            }
            KidsSectionTab.NATURE -> {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(NatureCategory.values()) { cat ->
                        val isSelected = natureCategory == cat
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) themeColors.accent else themeColors.surfaceVariant.copy(alpha = 0.5f),
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.onSurface.copy(alpha = 0.12f)) else null,
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    audioPlayer.playClickSound()
                                    onNatureCategoryChange(cat)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = cat.emoji, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cat.titleBn,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSelected) themeColors.onAccent else themeColors.onSurface
                                )
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

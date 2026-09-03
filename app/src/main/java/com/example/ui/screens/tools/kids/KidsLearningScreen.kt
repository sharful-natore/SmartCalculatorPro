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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Column {
                        Text(
                            text = "কিডস লার্নিং",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activeTab.titleBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.accent,
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
                            tint = themeColors.onSurface
                        )
                    }
                },
                actions = {
                    // Star Counter Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFB300).copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
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
                                color = Color(0xFFE65100),
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
                            tint = if (audioPlayer.isMuted) Color(0xFFD32F2F) else themeColors.accent,
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
                            color = if (isChildLocked) Color(0xFFD32F2F) else themeColors.surfaceVariant,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isChildLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Child Lock",
                                    tint = if (isChildLocked) Color.White else themeColors.onSurface.copy(alpha = 0.7f),
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
                            tint = themeColors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.surface)
            )
        },
        containerColor = themeColors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Tab Navigation
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

            Divider(color = themeColors.onSurface.copy(alpha = 0.08f), thickness = 1.dp)

            // Dynamic Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (activeTab) {
                    KidsSectionTab.ALPHABET -> KidsAlphabetTab(
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onTraceLetter = { letter ->
                            slateInitialLetter = letter
                            activeTab = KidsSectionTab.SLATE
                        }
                    )
                    KidsSectionTab.SPELLING -> KidsSpellingTab(
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onRewardStars = { amount -> addStars(amount) }
                    )
                    KidsSectionTab.MATH -> KidsMathTab(
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onRewardStars = { amount -> addStars(amount) }
                    )
                    KidsSectionTab.RHYMES -> KidsRhymesTab(
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
                        onRewardStars = { amount -> addStars(amount) }
                    )
                    KidsSectionTab.NATURE -> KidsNatureTab(
                        themeColors = themeColors,
                        audioPlayer = audioPlayer,
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

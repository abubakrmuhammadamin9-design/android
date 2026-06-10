package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Report
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = applicationContext as Application
            val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(app))
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    val currentTab by viewModel.currentTab.collectAsState()
    val reports by viewModel.filteredReports.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isRecentsPanelOpen by viewModel.isRecentsPanelOpen.collectAsState()
    val activeGameScreen by viewModel.activeGameScreen.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // High-Fidelity Samsung One UI Top System Status Bar Simulator
        SamsungStatusBar(viewModel)

        Box(modifier = Modifier.weight(1f)) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (currentTab != "launcher") {
                        NavigationBar(
                            modifier = Modifier.navigationBarsPadding(),
                            containerColor = Color(0xFFF3EDF7),
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == "reports",
                                onClick = { viewModel.selectTab("reports") },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Asosiy") },
                                label = { Text("Asosiy") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1D1B20),
                                    selectedTextColor = Color(0xFF1D1B20),
                                    indicatorColor = Color(0xFFE8DEF8),
                                    unselectedIconColor = Color(0xFF1D1B20).copy(alpha = 0.6f),
                                    unselectedTextColor = Color(0xFF1D1B20).copy(alpha = 0.6f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentTab == "playmarket",
                                onClick = { viewModel.selectTab("playmarket") },
                                icon = { Icon(Icons.Default.Storefront, contentDescription = "Play Market") },
                                label = { Text("Play Market") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1D1B20),
                                    selectedTextColor = Color(0xFF1D1B20),
                                    indicatorColor = Color(0xFFE8DEF8),
                                    unselectedIconColor = Color(0xFF1D1B20).copy(alpha = 0.6f),
                                    unselectedTextColor = Color(0xFF1D1B20).copy(alpha = 0.6f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentTab == "settings",
                                onClick = { viewModel.selectTab("settings") },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Sozlamalar") },
                                label = { Text("Sozlamalar") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1D1B20),
                                    selectedTextColor = Color(0xFF1D1B20),
                                    indicatorColor = Color(0xFFE8DEF8),
                                    unselectedIconColor = Color(0xFF1D1B20).copy(alpha = 0.6f),
                                    unselectedTextColor = Color(0xFF1D1B20).copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        "launcher" -> {
                            SamsungHomeScreen(viewModel = viewModel)
                        }
                        "reports" -> {
                            ReportsTabContent(
                                viewModel = viewModel,
                                reports = reports,
                                searchText = searchText,
                                selectedCategory = selectedCategory,
                                onSearchChange = { viewModel.updateSearchText(it) },
                                onCategorySelect = { viewModel.selectCategory(it) },
                                onDeleteReport = { viewModel.deleteReport(it) },
                                onAddClick = { showAddDialog = true }
                            )
                        }
                        "playmarket" -> {
                            PlayMarketTabContent(viewModel = viewModel)
                        }
                        "settings" -> {
                            SettingsTabContent(viewModel = viewModel)
                        }
                    }
                }
            }

            // Recents dashboard task switcher overlay
            if (isRecentsPanelOpen) {
                SamsungRecentsOverlay(
                    viewModel = viewModel,
                    onClose = { viewModel.setRecentsPanelOpen(false) }
                )
            }

            // High-Fidelity Notification Shade (Parda)
            val isNotificationShadeOpen by viewModel.isNotificationShadeOpen.collectAsState()
            Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isNotificationShadeOpen,
                    enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }) + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }) + androidx.compose.animation.fadeOut()
                ) {
                    SamsungNotificationShadeOverlay(
                        viewModel = viewModel,
                        onClose = { viewModel.setNotificationShadeOpen(false) }
                    )
                }
            }
        }

        // High-Fidelity Samsung virtual bottom navigation bar
        SamsungBottomNavigationBar(
            viewModel = viewModel,
            onBackPress = {
                if (viewModel.isNotificationShadeOpen.value) {
                    viewModel.setNotificationShadeOpen(false)
                } else if (isRecentsPanelOpen) {
                    viewModel.setRecentsPanelOpen(false)
                } else if (activeGameScreen != null) {
                    viewModel.selectGameScreen(null)
                } else if (showAddDialog) {
                    showAddDialog = false
                } else if (currentTab != "launcher") {
                    viewModel.selectTab("launcher")
                }
            }
        )
    }

    if (showAddDialog) {
        AddReportDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun SamsungBottomNavigationBar(
    viewModel: MainViewModel,
    onBackPress: () -> Unit
) {
    val isRecentsPanelOpen by viewModel.isRecentsPanelOpen.collectAsState()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = Color(0xFFCAC4D0).copy(alpha = 0.3f), thickness = 1.dp)
        Surface(
            color = Color(0xFFFEF7FF),
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recents key (Triple vertical bars)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        viewModel.setRecentsPanelOpen(!isRecentsPanelOpen)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(3.2.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(Color(0xFF1D1B20).copy(alpha = 0.85f))
                        )
                    }
                }
            }

            // Home key (Outer ring + solid circle inside)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        viewModel.setRecentsPanelOpen(false)
                        viewModel.selectGameScreen(null)
                        viewModel.selectTab("launcher")
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(19.dp)
                        .border(2.dp, Color(0xFF1D1B20).copy(alpha = 0.85f), CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF1D1B20).copy(alpha = 0.85f))
                    )
                }
            }

            // Back key (Custom drawn Triangle on Canvas, pointing left)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        onBackPress()
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(13.dp)) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width, 0f)
                        lineTo(0f, size.height / 2f)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path = path, color = Color(0xFF1D1B20).copy(alpha = 0.85f))
                }
            }
        }
    }
}
}

@Composable
fun SamsungRecentsOverlay(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeGameScreen by viewModel.activeGameScreen.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Horizontal list of task sheets
    val recentApps = remember {
        mutableStateListOf(
            Triple("reports", "Hisobotlar (Asosiy)", Icons.Default.Home),
            Triple("playmarket", "Play Market", Icons.Default.Storefront),
            Triple("settings", "Qurilma Sozlamalari", Icons.Default.Settings)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Faol Ilovalar & Vazifalar",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tezkor o'tish yoki yopish uchun bosing",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }

            // Cards carousel list
            if (recentApps.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hozirgi vaqtda faol vazifalar yo'q",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    recentApps.forEach { item ->
                        val (tabId, tabName, icon) = item
                        val isCurrent = currentTab == tabId && activeGameScreen == null

                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .height(320.dp)
                                .clickable {
                                    scope.launch {
                                        viewModel.selectTab(tabId)
                                        viewModel.selectGameScreen(null)
                                        onClose()
                                    }
                                }
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) Color(0xFF6750A4) else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E1E1E)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Task Top Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = tabName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            recentApps.remove(item)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Yopish",
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                // Interactive Mini App Preview Illustration Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(vertical = 12.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2E2E2E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        when (tabId) {
                                            "reports" -> {
                                                Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(40.dp))
                                                Text("Hisobotlar & Tahlil", color = Color.Gray, fontSize = 10.sp)
                                            }
                                            "playmarket" -> {
                                                Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(40.dp))
                                                Text("Mini o'yinlar zali", color = Color.Gray, fontSize = 10.sp)
                                            }
                                            else -> {
                                                Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(40.dp))
                                                Text("Qurilma parvarishi", color = Color.Gray, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }

                                // Open Button
                                Button(
                                    onClick = {
                                        viewModel.selectTab(tabId)
                                        viewModel.selectGameScreen(null)
                                        onClose()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCurrent) Color(0xFF6750A4) else Color(0xFF333333)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isCurrent) "Faol" else "O'tish",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Footer control
            Button(
                onClick = {
                    recentApps.clear()
                    viewModel.selectTab("reports")
                    viewModel.selectGameScreen(null)
                    onClose()
                    android.widget.Toast.makeText(context, "Hamma faol vazifalar o'chirildi", android.widget.Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Barchasini Yopish",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ----------------------------------------------------
// REPORTS MAIN SCREEN VIEW
// ----------------------------------------------------
@Composable
fun ReportsTabContent(
    viewModel: MainViewModel,
    reports: List<Report>,
    searchText: String,
    selectedCategory: String,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onDeleteReport: (Report) -> Unit,
    onAddClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val categories = listOf("Barchasi", "Ish", "Moliya", "Sog'liq", "Kundalik")
    var showStatsPane by remember { mutableStateOf(false) }

    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsState()
    val googleAccountName by viewModel.googleAccountName.collectAsState()
    val googleAccountEmail by viewModel.googleAccountEmail.collectAsState()

    // Show Scroll to Top button when list has been scrolled down
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Immersive Header (Xayrli kun! + Dynamic UserName + Profile Circle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "XAYRLI KUN!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF49454F),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isGoogleSignedIn && googleAccountName.isNotEmpty()) googleAccountName else "Alijon Valiyev",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                }
                
                // Profile Circle Avatar showing monogram if signed in, or guest icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEADDFF))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGoogleSignedIn && googleAccountName.isNotEmpty()) {
                        Text(
                            text = googleAccountName.first().uppercaseChar().toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF21005D)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil",
                            tint = Color(0xFF21005D),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Weekly Report Gradient Card (Haftalik hisobot 84% + Progress)
            val pctCompleted = remember(reports) {
                if (reports.isEmpty()) 84 else (70 + (reports.size * 3).coerceAtMost(30))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6750A4), Color(0xFF4F378B))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Haftalik hisobot",
                            color = Color(0xFFEADDFF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$pctCompleted%",
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "+12% o'tgan haftadan",
                            color = Color(0xFFD0BCFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.width(120.dp)
                    ) {
                        // Slim White progress bar
                        LinearProgressIndicator(
                            progress = { pctCompleted / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "${reports.size} ta hisobot tugallandi",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // Quick Actions Grid in 2 Columns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button 1: Rasmga Olish (Camera Simulator creator)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(112.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFE8DEF8))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD0BCFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = Color(0xFF21005D),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rasmga olish",
                            color = Color(0xFF1D1B20),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Button 2: Statistika (toggles active statistics bar charts)
                val activeStatsBg = if (showStatsPane) Color(0xFFD0BCFF) else Color(0xFFF3EDF7)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(112.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(activeStatsBg)
                        .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp))
                        .clickable { showStatsPane = !showStatsPane },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = Color(0xFF6750A4),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Statistika",
                            color = Color(0xFF49454F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Interactive Statistics Pane (Bo'limlar Tahlili)
            AnimatedVisibility(
                visible = showStatsPane,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2FA)),
                    border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bo'limlar Statistika Tahlili",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1D1B20)
                            )
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF6750A4))
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val cats = listOf("Ish", "Moliya", "Sog'liq", "Kundalik")
                        cats.forEach { cat ->
                            val catReports = reports.filter { it.category == cat }
                            val total = reports.size.coerceAtLeast(1)
                            val fraction = catReports.size.toFloat() / total
                            
                            val catCol = when (cat) {
                                "Ish" -> Color(0xFF3F51B5)
                                "Moliya" -> Color(0xFF4CAF50)
                                "Sog'liq" -> Color(0xFFE91E63)
                                else -> Color(0xFF9C27B0)
                            }
                            
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = cat, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(text = "${catReports.size} ta (${(fraction * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = catCol)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = catCol,
                                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = { Text("Hisobotlardan qidirish...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Tozalash")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null
                    )
                }
            }

            // Reports List or Empty State
            if (reports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Assignment,
                            contentDescription = "Hisobot topilmadi",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Hozircha hisobotlar yo'q",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Yangi hisobot qo'shish uchun quyidagi qizil tugmani bosing va rasmini muhrlang.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reports, key = { it.id }) { report ->
                        ReportCardItem(
                            report = report,
                            onDelete = { onDeleteReport(report) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Action Buttons overlay (Floating buttons)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Elegant Scroll to Top Button (Tepaga chiqish)
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Tepaga Chiqish (Scroll to top)"
                    )
                }
            }

            // Main FAB for adding report
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Yangi Hisobot")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Qo'shish", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReportCardItem(
    report: Report,
    onDelete: () -> Unit
) {
    val formattedDate = remember(report.timestamp) {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(report.timestamp))
    }

    val catColor = when (report.category) {
        "Ish" -> Color(0xFF3F51B5)
        "Moliya" -> Color(0xFF4CAF50)
        "Sog'liq" -> Color(0xFFE91E63)
        else -> Color(0xFF9C27B0) // Kundalik
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(catColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = report.category,
                        color = catColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "O'chirish",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = report.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Description
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Star rating indicators
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= report.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (i <= report.rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Captured Picture Indicator Label / Image drawing
                if (report.photoPath != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Attached photo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = when (report.photoPath) {
                                "hujjat" -> "Hujjat surati"
                                "analitika" -> "Analitika grafik"
                                "ish_joyi" -> "Loyiha xonasi"
                                else -> "Eslatma surati"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Simulated or real attached Image visual block if exists
            if (report.photoPath != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val photoType = report.photoPath
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoType.startsWith("/")) {
                        AsyncImage(
                            model = File(photoType),
                            contentDescription = "Attached photo preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 1.5f.dp.toPx()
                            val col = Color.Gray.copy(alpha = 0.6f)
                            
                            // draw viewfinder decorative borders
                            drawRect(
                                color = col,
                                style = Stroke(width = strokeWidth)
                            )
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.7f),
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 25),
                                strokeWidth = 1f.dp.toPx()
                            )
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.7f),
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height),
                                strokeWidth = 1f.dp.toPx()
                            )
                        }

                        // Draw custom visual based on photo snap type
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (photoType) {
                                "hujjat" -> {
                                    Icon(Icons.Default.TextSnippet, contentDescription = null, modifier = Modifier.size(36.dp), tint = catColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("📄 YAIM & HUJJAT SNAPPED", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                "analitika" -> {
                                    Icon(Icons.Default.InsertChart, contentDescription = null, modifier = Modifier.size(36.dp), tint = catColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("📊 STATISTIKA & TREND SNAPPED", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                "ish_joyi" -> {
                                    Icon(Icons.Default.Dvr, contentDescription = null, modifier = Modifier.size(36.dp), tint = catColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("🏢 OFIS KAMERA SCANNER", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                else -> {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(36.dp), tint = catColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("💡 G'OYA VA MUAMMOLAR", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// ADD REPORT & SIMULATED CAMERA DIALOG
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddReportDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Ish") }
    var rating by remember { mutableStateOf(3) }
    var attachedPhotoPath by remember { mutableStateOf<String?>(null) }
    var openCameraView by remember { mutableStateOf(false) }

    // Launcher for taking real camera photo preview
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "cap_${System.currentTimeMillis()}.png")
                file.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                attachedPhotoPath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Launcher for picking real photo from gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "pick_${System.currentTimeMillis()}.png")
                file.outputStream().use { out ->
                    inputStream?.copyTo(out)
                }
                attachedPhotoPath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val categories = listOf("Ish", "Moliya", "Sog'liq", "Kundalik")

    if (openCameraView) {
        VirtualCameraViewfinder(
            viewModel = viewModel,
            onClose = { openCameraView = false },
            onImageCaptured = { path ->
                attachedPhotoPath = path
                openCameraView = false
            }
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Yangi Hisobot Yozish",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Hisobot Sarlavhasi") },
                        placeholder = { Text("Mavzuni qisqa yozing...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Batafsil Hisobot Izohi") },
                        placeholder = { Text("Barcha tafsilotlarni bu yerda yoriting...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selector
                    Text("Bo'limni Tanlang:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = category == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    // Rating / Priority
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ahamiyati (1-5):", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (i <= rating) Color(0xFFFFB300) else Color.LightGray,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { rating = i }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Photo attachment status
                    if (attachedPhotoPath != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Rasm biriktirildi",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = { attachedPhotoPath = null }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Rasm o'chirish", tint = MaterialTheme.colorScheme.error)
                                }
                            }

                            // Render preview of selected photo or icon template
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                val path = attachedPhotoPath
                                if (path != null && path.startsWith("/")) {
                                    AsyncImage(
                                        model = File(path),
                                        contentDescription = "Rasm",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = when (path) {
                                                "hujjat" -> Icons.Default.TextSnippet
                                                "analitika" -> Icons.Default.BarChart
                                                "ish_joyi" -> Icons.Default.Computer
                                                else -> Icons.Default.Lightbulb
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = when (path) {
                                                "hujjat" -> "YAIM Hujjat Simulyator"
                                                "analitika" -> "Moliya Analitika Simulyator"
                                                "ish_joyi" -> "Loyiha Ofisi Simulyator"
                                                else -> "Kunlik Eslatma Simulyator"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Rasm biriktirish kanali:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Take from real camera
                                Button(
                                    onClick = { cameraLauncher.launch(null) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Kamera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Pick from real galleries
                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Galereya", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Take from simulator
                                Button(
                                    onClick = { openCameraView = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.FlipToBack, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Simulyator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dialog Footers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Bekor Qilish")
                        }
                        
                        var isSaving by remember { mutableStateOf(false) }
                        
                        Button(
                            enabled = !isSaving,
                            onClick = {
                                val trimmedTitle = title.trim()
                                if (trimmedTitle.isEmpty()) {
                                    android.widget.Toast.makeText(context, "Iltimos, hisobot sarlavhasini kiriting!", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (viewModel.isReportTitleDuplicate(trimmedTitle)) {
                                    android.widget.Toast.makeText(context, "Bunday nomli hisobot allaqachon mavjud! Iltimos, boshqa nom kiriting.", android.widget.Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                isSaving = true
                                viewModel.addReport(trimmedTitle, description, category, attachedPhotoPath, rating)
                                android.widget.Toast.makeText(context, "Hisobot muvaffaqiyatli saqlandi!", android.widget.Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isSaving) "Saqlanmoqda..." else "Saqlash")
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// VIRTUAL CAMERA VIEWFINDER OVERLAY
// ----------------------------------------------------
@Composable
fun VirtualCameraViewfinder(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    onImageCaptured: (String) -> Unit
) {
    val flashOn by viewModel.cameraFlashOn.collectAsState()
    val sceneIndex by viewModel.cameraSceneIndex.collectAsState()
    val isCapturing by viewModel.isCapturing.collectAsState()

    val scenesText = listOf(
        "📄 YAIM Hisobot hujjati (Document Style)",
        "📊 Kompaniya Moliya Analitikasi (Financial Graph)",
        "🏢 Konstruksiya & Bosh Ofis (Workplace Capture)",
        "💡 Kunlik Yig'ilish & Innovatsiya (Sticky Note)"
    )

    val scenesIntro = listOf(
        "Kamera ob'ektivi: Hujjat matnlarini skanerlash rejimi. Optimal kadr.",
        "Kamera ob'ektivi: Statistik diagramma va visual doiralarni aniqlash rejimi.",
        "Kamera ob'ektivi: Ish xonasi va jamoaviy stend-up muhrlash rejimi.",
        "Kamera ob'ektivi: Ofis doskasidagi stikerlar va muhim eslatmalar rejimi."
    )

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            modifier = Modifier
                .fillMaxWidth()
                .height(510.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top control bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleCameraFlash() }) {
                        Icon(
                            imageVector = if (flashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash Toggle",
                            tint = if (flashOn) Color(0xFFFFD600) else Color.White
                        )
                    }

                    Text(
                        text = "KAMERA SIMULYATORI",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Camera Viewfinder Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.5.dp, Color(0xFF333333), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Lines and grid
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val col = Color.White.copy(alpha = 0.25f)
                        // Vertical Grid Lines
                        drawLine(col, Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), 1f.dp.toPx())
                        drawLine(col, Offset(size.width * 2 / 3f, 0f), Offset(size.width * 2 / 3f, size.height), 1f.dp.toPx())
                        // Horizontal Grid Lines
                        drawLine(col, Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), 1f.dp.toPx())
                        drawLine(col, Offset(0f, size.height * 2 / 3f), Offset(size.width, size.height * 2 / 3f), 1f.dp.toPx())

                        // Outer bracket corners
                        val margin = 20f
                        val length = 50f
                        val strokeW = 4f.dp.toPx()
                        // top-left
                        drawLine(Color.White, Offset(margin, margin), Offset(margin + length, margin), strokeW, StrokeCap.Round)
                        drawLine(Color.White, Offset(margin, margin), Offset(margin, margin + length), strokeW, StrokeCap.Round)
                        // top-right
                        drawLine(Color.White, Offset(size.width - margin, margin), Offset(size.width - margin - length, margin), strokeW, StrokeCap.Round)
                        drawLine(Color.White, Offset(size.width - margin, margin), Offset(size.width - margin, margin + length), strokeW, StrokeCap.Round)
                        // bottom-left
                        drawLine(Color.White, Offset(margin, size.height - margin), Offset(margin + length, size.height - margin), strokeW, StrokeCap.Round)
                        drawLine(Color.White, Offset(margin, size.height - margin), Offset(margin, size.height - margin - length), strokeW, StrokeCap.Round)
                        // bottom-right
                        drawLine(Color.White, Offset(size.width - margin, size.height - margin), Offset(size.width - margin - length, size.height - margin), strokeW, StrokeCap.Round)
                        drawLine(Color.White, Offset(size.width - margin, size.height - margin), Offset(size.width - margin, size.height - margin - length), strokeW, StrokeCap.Round)
                    }

                    // Flashes white if shutter clicked
                    if (isCapturing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        )
                    }

                    // Animated Scenic Mockup Visual inside Box
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        AnimatedContent(
                            targetState = sceneIndex,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "sceneChange"
                        ) { index ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                when (index) {
                                    0 -> Icon(Icons.Default.TextSnippet, contentDescription = null, modifier = Modifier.size(76.dp), tint = Color.LightGray)
                                    1 -> Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(76.dp), tint = Color(0xFF4CAF50))
                                    2 -> Icon(Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(76.dp), tint = Color(0xFF2196F3))
                                    else -> Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(76.dp), tint = Color(0xFFFFEB3B))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = scenesText[index],
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = scenesIntro[index],
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    // Shutter countdown visual
                    if (isCapturing) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "CHEK!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                // Scene Switcher Arrow Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Foto burchakni almashtirish:",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { viewModel.nextCameraScene() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E2E2E),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Keyingi 🔄", fontSize = 11.sp)
                    }
                }

                // Shutter trigger button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222222))
                        .clickable {
                            viewModel.snapPhoto { code ->
                                onImageCaptured(code)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, Color.LightGray, CircleShape)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// PLAY MARKET TAB VIEW (HIGH-FIDELITY GOOGLE PLAY SIMULATOR)
// ----------------------------------------------------
@Composable
fun PlayMarketTabContent(
    viewModel: MainViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activeGameScreen by viewModel.activeGameScreen.collectAsState()
    val scope = rememberCoroutineScope()

    // Play Market sub-navigation states
    var activePlayCategory by remember { mutableStateOf("Games") } // "Games", "Apps", "Movies", "Books"
    var activePlaySubTab by remember { mutableStateOf("For you") } // "For you", "Top charts", "Categories"
    var selectedAppId by remember { mutableStateOf<String?>(null) }
    var customSearchApp by remember { mutableStateOf<Map<String, String>?>(null) }

    // Simulated download percentage map: Null / 0f = Not installed, 0.01f to 0.99f = Downloading, 1f = Ready/Installed
    val downloadStates = remember { mutableStateMapOf<String, Float>() }

    // Hardcoded high-fidelity apps database
    val appDb = remember {
        listOf(
            mapOf(
                "id" to "climber",
                "title" to "Tepaga Chiqish! (Endless Climb)",
                "dev" to "AI Studio Games",
                "desc" to "Ekrandagi platformalardan sakrab baland cho'qqiga chiqing. Bu haqiqiy tezlik va reaksiyalar simulyatsiyasi!",
                "icon" to "navigation",
                "color" to "0xFF673AB7",
                "size" to "1.8 MB",
                "rating" to "4.9 ★",
                "downloads" to "1M+",
                "category" to "Action",
                "type" to "Games"
            ),
            mapOf(
                "id" to "ttt",
                "title" to "Tic-Tac-Toe (Xol Nol Bot)",
                "dev" to "AI Studio Brain Systems",
                "desc" to "Klassik mantiqiy to'fon. Bizning robotimizga qarshi bellashing yoki do'stingiz bilan o'ynang.",
                "icon" to "grid",
                "color" to "0xFF3F51B5",
                "size" to "850 KB",
                "rating" to "4.7 ★",
                "downloads" to "500K+",
                "category" to "Puzzle",
                "type" to "Games"
            ),
            mapOf(
                "id" to "memory",
                "title" to "Xotira Mashlatgich (Matrix Match)",
                "dev" to "AI Studio Mind Gym",
                "desc" to "Miyani charxlovchi ajoyib xotira sinovi o'yini. Shoshiling, harakatlar sonini kamaytirib o'ynang!",
                "icon" to "extension",
                "color" to "0xFFE91E63",
                "size" to "1.2 MB",
                "rating" to "4.8 ★",
                "downloads" to "100K+",
                "category" to "Arcade",
                "type" to "Games"
            ),
            mapOf(
                "id" to "calc",
                "title" to "Valyuta Kursi Kalkulyatori",
                "dev" to "AI Studio Finance Toolset",
                "desc" to "Hisobotlardagi moliyaviy hisob-kitoblarga ko'maklashuvchi tezkor valyuta konvertori barqaror va tezkor.",
                "icon" to "exchange",
                "color" to "0xFF4CAF50",
                "size" to "400 KB",
                "rating" to "4.6 ★",
                "downloads" to "2M+",
                "category" to "Tools",
                "type" to "Apps"
            ),
            mapOf(
                "id" to "instagram",
                "title" to "Instagram",
                "dev" to "Instagram, Inc.",
                "desc" to "Sizga yoqqan insonlar hamda narsalarga yaqinroq bo'ling. Do'stlaringiz bilan bog'laning, yangiliklarni bahamlashing.",
                "icon" to "camera",
                "color" to "0xFFE1306C",
                "size" to "46 MB",
                "rating" to "3.7 ★",
                "downloads" to "5B+",
                "category" to "Social",
                "type" to "Apps"
            ),
            mapOf(
                "id" to "telegram",
                "title" to "Telegram",
                "dev" to "Telegram FZ-LLC",
                "desc" to "Oddiy, tezkor, xavfsiz va barcha qurilmalaringizda sinxronlanadigan tezkor xabar almashish dasturi.",
                "icon" to "send",
                "color" to "0xFF24A1DE",
                "size" to "36 MB",
                "rating" to "4.6 ★",
                "downloads" to "1B+",
                "category" to "Communication",
                "type" to "Apps"
            ),
            mapOf(
                "id" to "snapchat",
                "title" to "Snapchat",
                "dev" to "Snap Inc.",
                "desc" to "Hayotingizdagi guvohi bo'lgan qiziqarli lahzalarni do'stlaringiz bilan lahzada ulashing va qisqa video ko'ring.",
                "icon" to "chat",
                "color" to "0xFFFFFC00",
                "size" to "55 MB",
                "rating" to "4.4 ★",
                "downloads" to "1B+",
                "category" to "Social",
                "type" to "Apps"
            ),
            mapOf(
                "id" to "whatsapp",
                "title" to "WhatsApp Messenger",
                "dev" to "WhatsApp LLC",
                "desc" to "Butun dunyodagi yaqinlaringiz va oilangiz bilan butunlay bepul va xavfsiz shaxsiy xat-xabarlarni almashing.",
                "icon" to "call",
                "color" to "0xFF25D366",
                "size" to "32 MB",
                "rating" to "4.3 ★",
                "downloads" to "5B+",
                "category" to "Social",
                "type" to "Apps"
            ),
            mapOf(
                "id" to "roblox",
                "title" to "Roblox",
                "dev" to "Roblox Corporation",
                "desc" to "Roblox - bu sizga ijod qilish, tajribalar almashish va tasavvur qilishingiz mumkin bo'lgan har qanday narsa bo'lish imkonini beruvchi eng so'nggi virtual koinotdir.",
                "icon" to "gamepad",
                "color" to "0xFF212121",
                "size" to "142 MB",
                "rating" to "4.5 ★",
                "downloads" to "500M+",
                "category" to "Adventure",
                "type" to "Games"
            ),
            mapOf(
                "id" to "ludo",
                "title" to "Ludo King (4 kishilik o'yin)",
                "dev" to "Gametion Global",
                "desc" to "Do'stlar va oila a'zolari bilan o'ynaladigan klassik stol usti o'yini. 2, 3 yoki 4 kishilik mahalliy va uzoq masofali multiplayer rejimi mavjud!",
                "icon" to "sports",
                "color" to "0xFFD32F2F",
                "size" to "52 MB",
                "rating" to "4.3 ★",
                "downloads" to "500M+",
                "category" to "Board",
                "type" to "Games"
            ),
            mapOf(
                "id" to "bombsquad",
                "title" to "BombSquad (4 kishilik o'yin)",
                "dev" to "Eric Froemling",
                "desc" to "Do'stlaringizni mini-o'yinlar turkumida portlatib yuboring! 4 kishilik va undan ko'p o'yinchilar ishtirokidagi ajoyib sarguzasht.",
                "icon" to "sports",
                "color" to "0xFF388E3C",
                "size" to "61 MB",
                "rating" to "4.4 ★",
                "downloads" to "50M+",
                "category" to "Action",
                "type" to "Games"
            ),
            mapOf(
                "id" to "monopoly",
                "title" to "Monopoly (4 kishilik o'yin)",
                "dev" to "Marmalade Game Studio",
                "desc" to "Klassik mulk savdosi o'yini. Do'stlaringiz va oilangiz bilan 4 kishilik rejimda qatnashing, shaharlarni sotib oling va ijaraga bering!",
                "icon" to "sports",
                "color" to "0xFF1976D2",
                "size" to "120 MB",
                "rating" to "4.2 ★",
                "downloads" to "10M+",
                "category" to "Board",
                "type" to "Games"
            ),
            mapOf(
                "id" to "uno",
                "title" to "Uno! Cards (4 kishilik o'yin)",
                "dev" to "Mattel163 Limited",
                "desc" to "Dunyoga mashhur Uno qog'oz o'yini endi mobil qurilmada. 4 kishilik do'stona va raqobatbardosh jamoaviy bahsda ishtirok eting!",
                "icon" to "sports",
                "color" to "0xFFF57C00",
                "size" to "74 MB",
                "rating" to "4.5 ★",
                "downloads" to "100M+",
                "category" to "Card",
                "type" to "Games"
            ),
            mapOf(
                "id" to "fourplayer",
                "title" to "4 Player Games (Meyve)",
                "dev" to "Meyve Games",
                "desc" to "4 kishilik mahalliy oflayn mini-o'yinlar to'plami. Bir nechta o'yinchi uchun bitta ekranda o'ynash uchun qulay boshqaruv va ajoyib musobaqalar!",
                "icon" to "sports",
                "color" to "0xFF7B1FA2",
                "size" to "24 MB",
                "rating" to "4.6 ★",
                "downloads" to "50M+",
                "category" to "Arcade",
                "type" to "Games"
            ),
            mapOf(
                "id" to "asphalt",
                "title" to "Asphalt Legends: Poyga",
                "dev" to "Gameloft SE",
                "desc" to "Haqiqiy super-karlar bilan dunyoning eng chiroyli joylarida yuqori tezlikda drayv va poyga qilish hayajoni!",
                "icon" to "navigation",
                "color" to "0xFF1565C0",
                "size" to "1.9 GB",
                "rating" to "4.7 ★",
                "downloads" to "10M+",
                "category" to "Racing",
                "type" to "Games"
            )
        )
    }

    if (activeGameScreen != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            // High fidelity Back header to close games
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF7FF))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectGameScreen(null) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Chiqish",
                        tint = Color(0xFF1D1B20)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (activeGameScreen) {
                        "climber" -> "Tepaga Chiqish"
                        "ttt" -> "Xol va Nol Bot"
                        "memory" -> "Xotira Mashqlari"
                        "calc" -> "Valyuta Kursi"
                        "roblox" -> "Roblox Simulator"
                        "ludo" -> "Ludo King (4 kishilik)"
                        "bombsquad" -> "BombSquad (4 kishilik)"
                        "monopoly" -> "Monopoly (4 kishilik)"
                        "uno" -> "Uno Card (4 kishilik)"
                        "fourplayer" -> "4 Player Games"
                        "instagram" -> "Instagram"
                        "telegram" -> "Telegram"
                        else -> "Multiplayer O'yin"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (activeGameScreen) {
                    "climber" -> VerticalClimberGameView(viewModel = viewModel)
                    "ttt" -> TicTacToeGameView(viewModel = viewModel)
                    "memory" -> MemoryGameView(viewModel = viewModel)
                    "calc" -> CurrencyCalcView()
                    "roblox" -> RobloxSimulatorGameView(onBack = { viewModel.selectGameScreen(null) })
                    "ludo", "bombsquad", "monopoly", "uno", "fourplayer", "custom_generated" -> MultiPlayer4GamesView(gameType = activeGameScreen ?: "fourplayer", onBack = { viewModel.selectGameScreen(null) })
                    "instagram" -> InstagramSimulatorView()
                    "telegram" -> TelegramSimulatorView(viewModel = viewModel)
                }
            }
        }
    } else if (selectedAppId != null) {
        // ====================================================================
        // GOOGLE PLAY APP DETAIL PAGE (IMAGE 4 REPLICA)
        // ====================================================================
        val app = appDb.find { it["id"] == selectedAppId } ?: customSearchApp ?: appDb.first()
        val appTitle = app["title"] ?: ""
        val appDev = app["dev"] ?: ""
        val appDesc = app["desc"] ?: ""
        val appSize = app["size"] ?: ""
        val appRating = app["rating"] ?: ""
        val appDownloads = app["downloads"] ?: ""
        val appCategory = app["category"] ?: ""
        val iconType = app["icon"] ?: "navigation"
        val iconColorString = app["color"] ?: "0xFF673AB7"
        val iconColor = Color(android.graphics.Color.parseColor(iconColorString.replace("0x", "#")))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { selectedAppId = null }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Orqaga", tint = Color.Black)
                }
                Row {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Qidirish", tint = Color.Black)
                    }
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Yana", tint = Color.Black)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Header Block: Icon & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (iconType) {
                                "grid" -> Icons.Default.GridOn
                                "extension" -> Icons.Default.Extension
                                "exchange" -> Icons.Default.CurrencyExchange
                                "camera" -> Icons.Default.CameraAlt
                                "chat" -> Icons.Default.Chat
                                "call" -> Icons.Default.Call
                                "gamepad" -> Icons.Default.Gamepad
                                "sports" -> Icons.Default.SportsEsports
                                "casino" -> Icons.Default.Casino
                                else -> Icons.Default.Navigation
                            },
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F)
                        )
                        Text(
                            text = appDev,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF01875F) // Authentic play green
                        )
                        Text(
                            text = "Reklamalar mavjud • Ilova ichidagi xaridlar",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayStatsItem(topText = appRating, bottomText = "88K sharhlar")
                    PlayStatsDivider()
                    PlayStatsItem(topText = appSize, bottomText = "Hajm")
                    PlayStatsDivider()
                    PlayStatsItem(topText = appDownloads, bottomText = "Yuklashlar")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Green Call To Action button with download animations
                val dState = downloadStates[selectedAppId ?: ""] ?: 0f

                if (dState > 0f && dState < 1f) {
                    // Image 6 style progress bar block
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Yuklab olinmoqda: ${(dState * 100).toInt()}% • ${appSize}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF01875F)
                            )
                            Text(
                                text = "Play Protect tekshirdi ✔️",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { dState },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = Color(0xFF01875F),
                            trackColor = Color(0xFFE0E0E0)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { downloadStates[selectedAppId ?: ""] = 0f }
                        ) {
                            Text("Bekor qilish", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                } else {
                    val isInstalled = dState == 1f
                    Button(
                        onClick = {
                            if (isInstalled) {
                                // Real launch of mini games
                                val currentId = selectedAppId ?: ""
                                if (currentId in listOf("climber", "ttt", "memory", "calc", "roblox", "ludo", "bombsquad", "monopoly", "uno", "fourplayer", "custom_generated")) {
                                    if (currentId == "climber") viewModel.startClimbingGame()
                                    if (currentId == "ttt") viewModel.resetTicTacToe()
                                    if (currentId == "memory") viewModel.startMemoryGame()
                                    viewModel.selectGameScreen(currentId)
                                } else {
                                    android.widget.Toast.makeText(context, "$appTitle ochildi!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Simulate download loop exactly matching Image 6
                                scope.launch {
                                    downloadStates[selectedAppId ?: ""] = 0.05f
                                    var currentPct = 0.05f
                                    while (currentPct < 1f) {
                                        kotlinx.coroutines.delay(200)
                                        currentPct += 0.12f
                                        if (currentPct > 1f) currentPct = 1f
                                        downloadStates[selectedAppId ?: ""] = currentPct
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isInstalled) {
                                val currentId = selectedAppId ?: ""
                                if (currentId == "climber" || currentId == "ttt" || currentId == "memory" || currentId == "calc") "O'YNASH 🚀" else "OCHISH"
                            } else "YUKLAB OLISH / O'RNATISH",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // About game
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Ilova haqida", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = appDesc,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // High fidelity reviews section
                Text(text = "Reyting va sharhlar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = appRating.replace(" ★", ""), fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        Row {
                            repeat(5) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                            }
                        }
                        Text(text = "142,525", fontSize = 10.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("5", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(10.dp))
                            LinearProgressIndicator(progress = { 0.85f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = Color(0xFF01875F), trackColor = Color(0xFFF1F1F1))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("4", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(10.dp))
                            LinearProgressIndicator(progress = { 0.1f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = Color(0xFF01875F), trackColor = Color(0xFFF1F1F1))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("3", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(10.dp))
                            LinearProgressIndicator(progress = { 0.03f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = Color(0xFF01875F), trackColor = Color(0xFFF1F1F1))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("2", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(10.dp))
                            LinearProgressIndicator(progress = { 0.01f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = Color(0xFF01875F), trackColor = Color(0xFFF1F1F1))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("1", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(10.dp))
                            LinearProgressIndicator(progress = { 0.01f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = Color(0xFF01875F), trackColor = Color(0xFFF1F1F1))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    } else {
        // ====================================================================
        // GOOGLE PLAY STORE HOME SCREEN REPLICA (IMAGE 1 & 2 & 5)
        // ====================================================================
        var searchText by remember { mutableStateOf("") }
        var selectedPlayGenre by remember { mutableStateOf("Barchasi") }
        val displayedApps = remember(activePlayCategory, activePlaySubTab, searchText, selectedPlayGenre) {
            val lowercaseQuery = searchText.lowercase().trim()
            if (lowercaseQuery.isEmpty()) {
                val base = appDb.filter { it["type"] == activePlayCategory }
                if (activePlayCategory == "Games" && selectedPlayGenre != "Barchasi") {
                    base.filter { app ->
                        val cat = (app["category"] ?: "").lowercase()
                        when (selectedPlayGenre) {
                            "Sarguzasht" -> cat == "adventure" || cat == "action"
                            "Jumboq" -> cat == "puzzle" || cat == "card" || cat == "arcade"
                            "Poyga" -> cat == "racing"
                            "Stol usti" -> cat == "board" || cat == "card"
                            else -> true
                        }
                    }
                } else {
                    base
                }
            } else {
                val matches = appDb.filter { app ->
                    val title = (app["title"] ?: "").lowercase()
                    val desc = (app["desc"] ?: "").lowercase()
                    val category = (app["category"] ?: "").lowercase()
                    
                    title.contains(lowercaseQuery) || 
                    desc.contains(lowercaseQuery) || 
                    category.contains(lowercaseQuery) ||
                    (lowercaseQuery.contains("4") && app["id"] in listOf("ludo", "bombsquad", "monopoly", "uno", "fourplayer")) ||
                    ((lowercaseQuery.contains("kishi") || lowercaseQuery.contains("user") || lowercaseQuery.contains("player")) && app["id"] in listOf("ludo", "bombsquad", "monopoly", "uno", "fourplayer"))
                }.toMutableList()

                // Special handling for 4-player request
                if (lowercaseQuery.contains("4") || lowercaseQuery.contains("to'rt") || lowercaseQuery.contains("tort") || lowercaseQuery.contains("kishi") || lowercaseQuery.contains("player")) {
                    val players4 = appDb.filter { it["id"] in listOf("ludo", "bombsquad", "monopoly", "uno", "fourplayer") }
                    players4.forEach { p ->
                        if (!matches.any { it["id"] == p["id"] }) {
                            matches.add(p)
                        }
                    }
                }

                // If nothing matches and they typed something, we dynamically generate a beautifully customized app matching their query
                if (matches.isEmpty() && searchText.length >= 2) {
                    val capName = searchText.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    val isSgGame = lowercaseQuery.contains("o'yin") || lowercaseQuery.contains("game") || lowercaseQuery.contains("play") || lowercaseQuery.contains("run") || lowercaseQuery.contains("clash") || lowercaseQuery.contains("pubg") || lowercaseQuery.contains("brawl") || lowercaseQuery.contains("subway")
                    val capCategory = if (isSgGame) "Action" else "Tools"
                    val capType = if (isSgGame) "Games" else "Apps"
                    
                    val parsedIcon = when {
                        isSgGame -> "gamepad"
                        lowercaseQuery.contains("chat") || lowercaseQuery.contains("telegr") || lowercaseQuery.contains("whatsapp") || lowercaseQuery.contains("sms") -> "chat"
                        lowercaseQuery.contains("foto") || lowercaseQuery.contains("camera") || lowercaseQuery.contains("kamera") || lowercaseQuery.contains("insta") -> "camera"
                        lowercaseQuery.contains("qo'sh") || lowercaseQuery.contains("musiq") || lowercaseQuery.contains("music") || lowercaseQuery.contains("mp3") -> "extension"
                        lowercaseQuery.contains("pul") || lowercaseQuery.contains("calc") || lowercaseQuery.contains("hisob") || lowercaseQuery.contains("bank") -> "exchange"
                        else -> "navigation"
                    }

                    val generatedItem = mapOf(
                        "id" to "custom_generated",
                        "title" to capName,
                        "dev" to "$capName Mobile Studio",
                        "desc" to "$capName uchun maxsus loyihalashtirilgan ultra-zamonaviy, bepul va tezkor ilova. Do'stona interfeys va yuqori darajadagi barqarorlik barcha foydalanuvchilarimizga!",
                        "icon" to parsedIcon,
                        "color" to "0xFF02875F",
                        "size" to "14 MB",
                        "rating" to "4.8 ★",
                        "downloads" to "10M+",
                        "category" to capCategory,
                        "type" to capType
                    )
                    customSearchApp = generatedItem
                    matches.add(generatedItem)
                }
                matches
            }
        }

        var showAccountDialog by remember { mutableStateOf(false) }
        val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsState()
        val googleAccountName by viewModel.googleAccountName.collectAsState()
        val googleAccountEmail by viewModel.googleAccountEmail.collectAsState()

        if (showAccountDialog) {
            Dialog(onDismissRequest = { showAccountDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(54.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isGoogleSignedIn && googleAccountName.isNotEmpty()) googleAccountName.take(1) else "P",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = if (isGoogleSignedIn) googleAccountName else "Google Mehmoni", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = if (isGoogleSignedIn) googleAccountEmail else "Ulanmagan parda", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showAccountDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F))) {
                            Text("Yopish")
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // 1. Google Search Bar Capsule (Image 1 style)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.DarkGray)
                    Spacer(modifier = Modifier.width(10.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchText.isEmpty()) {
                                Text("O'yinlar va ilovalarni qidirish", color = Color.Gray, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Round Circle avatar
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6750A4))
                            .clickable { showAccountDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isGoogleSignedIn && googleAccountName.isNotEmpty()) googleAccountName.take(1).uppercase() else "A",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 2. Google Play Category sliding header (For you, Top charts, Categories)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf("For you", "Top charts", "Categories").forEach { tab ->
                    val isActive = activePlaySubTab == tab
                    Column(
                        modifier = Modifier.clickable { activePlaySubTab = tab },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tab,
                            fontSize = 14.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) Color(0xFF01875F) else Color.DarkGray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(if (isActive) Color(0xFF01875F) else Color.Transparent)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFCAC4D0).copy(alpha = 0.3f))

            if (activePlayCategory == "Games") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val genres = listOf(
                        "Barchasi" to "Barchasi 🎮",
                        "Sarguzasht" to "Sarguzasht 🗺️",
                        "Jumboq" to "Jumboq 🧩",
                        "Poyga" to "Poyga 🏎️",
                        "Stol usti" to "Stol usti 🎲"
                    )
                    genres.forEach { (genreKey, genreLabel) ->
                        val isSelected = selectedPlayGenre == genreKey
                        val chipBgColor = if (isSelected) Color(0xFFE6F4EA) else Color(0xFFF1F3F4)
                        val chipTextColor = if (isSelected) Color(0xFF01875F) else Color(0xFF49454F)
                        val chipBorderColor = if (isSelected) Color(0xFF137333) else Color.Transparent
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(chipBgColor)
                                .border(
                                    width = 1.dp,
                                    color = chipBorderColor,
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .clickable { selectedPlayGenre = genreKey }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = genreLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipTextColor
                            )
                        }
                    }
                }
            }

            // Body lists according to subtab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (activePlaySubTab == "Categories") {
                    // ====================================================================
                    // CATEGORIES LAYOUT (IMAGE 5 REPLICA)
                    // ====================================================================
                    listOf("Action", "Adventure", "Arcade", "Board", "Casino", "Casual", "Education", "Music", "Puzzle").forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activePlaySubTab = "For you"
                                    android.widget.Toast.makeText(context, "$cat bo'limi tanlandi!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when(cat) {
                                        "Action" -> Icons.Default.Navigation
                                        "Puzzle" -> Icons.Default.GridOn
                                        "Arcade" -> Icons.Default.Extension
                                        else -> Icons.Default.Gamepad
                                    },
                                    contentDescription = null,
                                    tint = Color(0xFF01875F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(cat, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                        }
                        HorizontalDivider(color = Color(0xFFF1F1F1))
                    }
                } else if (activePlaySubTab == "Top charts") {
                    // ====================================================================
                    // TOP CHARTS LIST LAYOUT (IMAGE 2 REPLICA)
                    // ====================================================================
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Top bepul o'yin va ilovalar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    // Display static top chart cards with numbers 1 to 7
                    val chartApps = remember {
                        listOf(
                            Triple("1. Brain Story: Tricky Puzzle", "Puzzle • 3.6 ★ • 111 MB", "memory"),
                            Triple("2. Oil Tanker Truck Driving Games", "Strategy • 4.0 ★ • 57 MB", "climber"),
                            Triple("3. DIY Keyboard Builder Pro", "Simulation • 4.2 ★ • 109 MB", "calc"),
                            Triple("4. Rope Savior 3D Runner", "Puzzle • 4.4 ★ • 75 MB", "memory"),
                            Triple("5. Squid Sniper 456 Challenge", "Action • 4.5 ★ • 35 MB", "ttt"),
                            Triple("6. Subway Surfers Runner", "Arcade • 4.4 ★ • 133 MB", "climber"),
                            Triple("7. Antistress - relaxation toys", "Puzzle • 4.1 ★ • 49 MB", "calc")
                        )
                    }

                    chartApps.forEach { (title, subtitle, appMapId) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAppId = appMapId }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE4F3ED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (appMapId == "climber") Icons.Default.Navigation else Icons.Default.GridOn,
                                    contentDescription = null,
                                    tint = Color(0xFF01875F)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                        }
                    }
                } else {
                    // ====================================================================
                    // DEFAULT "FOR YOU" RECOMMENDATIONS (IMAGE 1 REPLICA)
                    // ====================================================================

                    // A promo Free Fire banner with high fidelity background exactly matching Image 1!
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFB71C1C), Color(0xFFE65100))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                "STARTER PACKS CHEGIRMASI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Yellow
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "FREE FIRE: Eng qaynoq aksiyalar!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                "Barcha to'plamlarga 85% gacha yirik chegirmalar barcha foydalanuvchilarimizga.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.82f)
                            )
                        }
                    }

                    Text(
                        text = "Tavsiya etiladigan Ilovalar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )

                    // Horizontal loop of recommended apps (Image 1 replica width)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        listOf("instagram", "snapchat", "whatsapp").forEach { appKey ->
                            val app = appDb.find { it["id"] == appKey } ?: appDb.last()
                            PlayCompactHorizCard(
                                title = app["title"] ?: "",
                                rating = app["rating"] ?: "4.0 ★",
                                size = app["size"] ?: "",
                                colHex = app["color"] ?: "0xFF25D366"
                            ) {
                                selectedAppId = appKey
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Bizning To'plam o'yinlar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )

                    // Display list of main fully functional tools/games
                    displayedApps.forEach { appMap ->
                        val appId = appMap["id"] ?: ""
                        val appTitle = appMap["title"] ?: ""
                        val appDev = appMap["dev"] ?: ""
                        val appSize = appMap["size"] ?: ""
                        val appRating = appMap["rating"] ?: ""
                        val colHex = appMap["color"] ?: "0xFF673AB7"
                        val iconCode = appMap["icon"] ?: "navigation"
                        val parsedColor = Color(android.graphics.Color.parseColor(colHex.replace("0x", "#")))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAppId = appId }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(parsedColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when(iconCode) {
                                        "grid" -> Icons.Default.GridOn
                                        "extension" -> Icons.Default.Extension
                                        "exchange" -> Icons.Default.CurrencyExchange
                                        else -> Icons.Default.Navigation
                                    },
                                    contentDescription = null,
                                    tint = parsedColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = appTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = "$appDev • $appSize", fontSize = 11.sp, color = Color.Gray)
                            }

                            Text(text = appRating, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                    }
                }
            }

            // 3. Bottom Play Store Navigation Bar (Image 1 & 2 styles)
            NavigationBar(
                containerColor = Color(0xFFF1F3F4),
                tonalElevation = 0.dp,
                modifier = Modifier.height(56.dp)
            ) {
                listOf(
                    Pair("Games", Icons.Default.Gamepad),
                    Pair("Apps", Icons.Default.AppShortcut),
                    Pair("Movies", Icons.Default.VideoLibrary),
                    Pair("Books", Icons.Default.Book)
                ).forEach { (catName, icon) ->
                    val isSelected = activePlayCategory == catName
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activePlayCategory = catName },
                        icon = { Icon(imageVector = icon, contentDescription = catName, modifier = Modifier.size(20.dp)) },
                        label = { Text(catName, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF01875F),
                            selectedTextColor = Color(0xFF01875F),
                            indicatorColor = Color(0xFFE4F3ED),
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.Gray
                        )
                    )
                }
            }
        }
    }
}

// Stats item column
@Composable
fun PlayStatsItem(topText: String, bottomText: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = topText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = bottomText, fontSize = 10.sp, color = Color.Gray)
    }
}

// Stats item divider
@Composable
fun PlayStatsDivider() {
    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.Gray.copy(alpha = 0.2f)))
}

// Compact horizontal recommended app cards
@Composable
fun PlayCompactHorizCard(
    title: String,
    rating: String,
    size: String,
    colHex: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() }
            .padding(bottom = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(android.graphics.Color.parseColor(colHex.replace("0x", "#"))).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = Color(android.graphics.Color.parseColor(colHex.replace("0x", "#")))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "$rating • $size", fontSize = 9.sp, color = Color.Gray)
        }
    }
}

// Custom google play store list line design
@Composable
fun PlayStoreAppCard(
    title: String,
    desc: String,
    icon: ImageVector,
    iconTint: Color,
    sizeText: String,
    ratingText: String,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2FA)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App visual image icon with solid round corner background
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1D1B20),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = Color(0xFF49454F),
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Detail indicators row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ratingText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB300)
                    )
                    Text(
                        text = sizeText,
                        fontSize = 11.sp,
                        color = Color(0xFF49454F).copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Ilova",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action button using the direct primary theme color
            Button(
                onClick = onOpen,
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6750A4),
                    contentColor = Color.White
                ),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("O'ynash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------------------------------------------
// MINI GAME 1: "TEPAGA CHIQISH" (CLIMBER PLATFORM GAME)
// ----------------------------------------------------
@Composable
fun VerticalClimberGameView(viewModel: MainViewModel) {
    val highScore by viewModel.climbHighScore.collectAsState()
    val currentScore by viewModel.climbCurrentScore.collectAsState()
    val gameState by viewModel.climbGameState.collectAsState()
    val playerY by viewModel.playerY.collectAsState()
    val platforms by viewModel.platforms.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E24))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // High scores panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Balon Rekord: $highScore",
                color = Color(0xFFFFC107),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "Hozirgi Score: $currentScore",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        // Active game render zone or screen states
        when (gameState) {
            "START" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.5.dp, Color(0xFF3F51B5), RoundedCornerShape(16.dp))
                        .background(Color(0xFF26262B)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "TEPAGA CHIQISH",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bu foydalanuvchining 'tepaga chiqsin' talabiga binoan tuzilgan sakrash simulyatoridir.\n\nTugmani barcha tezlikda bosib eng baland ko'rsatkichlarni belgilang!",
                            textAlign = TextAlign.Center,
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.startClimbingGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Boshlash 🚀", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            "PLAYING" -> {
                // Interactive climbing board using clean canvas visuals
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0D0D11))
                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    // Sky visual styling with stars
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color(0xFF3F51B5).copy(alpha = 0.15f), radius = 180f, center = Offset(size.width / 2, size.height / 3))
                        
                        // draw cloud lines mock
                        for (i in 1..4) {
                            val cloudY = (i * 200f) % size.height
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(500f, cloudY),
                                end = Offset(20f, cloudY),
                                strokeWidth = 3f.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Interactive player sprite drawing inside game bounds
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier
                                    .size(60.dp)
                                    .animateContentSize()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ALPINIST balandligi: ${(playerY / 10).toInt()} m",
                                color = Color.Cyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Tap Instruction floating label at top
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Tezlikda Sakrash!",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Core Jump Trigger action
                Button(
                    onClick = { viewModel.tapClimbJump(200f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("TEPAGA SAKRASH! (TAP) ⬆️", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            else -> {
                // GAMEOVER state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(2.dp, Color.Red.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .background(Color(0xFF331A1D)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "O'YIN TUGADI",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Slippery muzlikdan yiqilib sirpandiz!\nYig'ilgan balandlik: $currentScore m",
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { viewModel.startClimbingGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                        ) {
                            Text("Qaytadan urinish 🔄", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

// ----------------------------------------------------
// MINI GAME 2: "XOL VE NOL" (TIC-TAC-TOE GAME)
// ----------------------------------------------------
@Composable
fun TicTacToeGameView(viewModel: MainViewModel) {
    val board by viewModel.ticTacToeBoard.collectAsState()
    val turn by viewModel.ticTacToeTurn.collectAsState()
    val winner by viewModel.ticTacToeWinner.collectAsState()
    val tttUser by viewModel.tttScoreUser.collectAsState()
    val tttCpu by viewModel.tttScoreCpu.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101820))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Score row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Siz (X)", color = Color.White, fontWeight = FontWeight.Bold)
                Text("$tttUser", color = Color.Cyan, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("VS", color = Color.Gray, fontWeight = FontWeight.Bold)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Smart Bot (O)", color = Color.White, fontWeight = FontWeight.Bold)
                Text("$tttCpu", color = Color.Magenta, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        // Active Status
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = when {
                    winner == "X" -> "G'ALABA! Siz yutdingiz 🎉"
                    winner == "O" -> "AFSUS! Aqlli robot g'olib bo'ldi 🤖"
                    winner == "DURANG" -> "Durang natija! Hech kim yutgandur"
                    turn == "X" -> "Sizning navbatingiz (X)"
                    else -> "Bot o'ylamoqda... (O)"
                },
                color = when {
                    winner == "X" -> Color(0xFF4CAF50)
                    winner == "O" -> Color(0xFFFF5252)
                    else -> Color.White
                },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Grid board
        Column(
            modifier = Modifier
                .size(310.dp)
                .border(2.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .background(Color(0xFF1B222C))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val cellValue = board[index]

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF263238))
                                .clickable { viewModel.playTicTacToeCell(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cellValue,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (cellValue == "X") Color.Cyan else Color.Magenta
                            )
                        }
                    }
                }
            }
        }

        // Control board buttons
        Button(
            onClick = { viewModel.resetTicTacToe() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Yangi O'yin Boshlash", fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// MINI GAME 3: "XOTIRA CHARXI" (MEMORY MATCHER GAME)
// ----------------------------------------------------
@Composable
fun MemoryGameView(viewModel: MainViewModel) {
    val cards by viewModel.memoryCards.collectAsState()
    val moves by viewModel.memoryMoves.collectAsState()
    val score by viewModel.memoryScore.collectAsState()
    val isGameOver by viewModel.memoryGameOver.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1219))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Tracker details status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Urinishlar: $moves", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Ochko: $score", color = Color.Green, fontWeight = FontWeight.Bold)
        }

        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Stars, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(70.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("TABRIKLAYMIZ!", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Barcha xotira xaritalarini muvaffaqiyatli ochdingiz!", color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.startMemoryGame() }) {
                        Text("Yangi raund")
                    }
                }
            }
        } else {
            // Memory grids logic
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                for (row in 0..3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0..3) {
                            val index = row * 4 + col
                            if (index < cards.size) {
                                val card = cards[index]
                                val isOpen = card.isFlipped || card.isMatched

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isOpen) Color(0xFF1E2638) else Color(0xFF673AB7))
                                        .clickable { viewModel.clickMemoryCard(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isOpen) {
                                        Text(text = card.symbol, fontSize = 28.sp)
                                    } else {
                                        Text(text = "?", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.startMemoryGame() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aralashtirish & Qaytadan ornatish")
        }
    }
}

// ----------------------------------------------------
// BONUS PLAY STORE TOOL: CURRENCY CALCULATOR
// ----------------------------------------------------
@Composable
fun CurrencyCalcView() {
    var uzsText by remember { mutableStateOf("1") }
    val usdRate = 12750.0  // Simulated conversion rate (1 USD = 12,750 UZS)
    val convertedUsd = remember(uzsText) {
        val value = uzsText.toDoubleOrNull() ?: 0.0
        String.format("%.2f", value / usdRate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Yarim Avtomat Kurs Kalkulyatori",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Eslatma: Moliyaviy hisobotlarni tezkor hisoblash maqsadida qo'shimcha Play Market uskunasi integratsiya qilingan.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Konvertatsiya Kursi:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "1 USD 💵 = 12,750.00 SO'M (UZS)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        OutlinedTextField(
            value = uzsText,
            onValueChange = { uzsText = it },
            label = { Text("Summani yozing (SO'M)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "AQSH Dollari Qiymati:", fontSize = 12.sp, color = Color.DarkGray)
                Text(
                    text = "$convertedUsd USD",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E7D32)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ============================================================================
// HIGH-FIDELITY SAMSUNG ONE UI SYSTEM COMPONENTS
// ============================================================================

@Composable
fun SamsungStatusBar(viewModel: MainViewModel) {
    // Collect system time dynamically
    val customClockTime by viewModel.customClockTime.collectAsState()
    var systemTime by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            systemTime = sdf.format(Date())
            delay(1000)
        }
    }
    
    val displayTime = customClockTime ?: systemTime

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFEF7FF)) // Light background corresponding to Immersive Theme
            .clickable {
                // Clicking status bar toggles notification shade
                viewModel.setNotificationShadeOpen(!viewModel.isNotificationShadeOpen.value)
            }
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Side: Time & Notifications
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = displayTime,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20)
            )
            // Sparkly simulated indicator
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6750A4))
            )
        }

        // Center Punch-Hole Camera Simulator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(0xFF030303))
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
        )

        // Right Side: WiFi, High Signal, Battery gauge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SignalCellularAlt,
                contentDescription = null,
                tint = Color(0xFF1D1B20),
                modifier = Modifier.size(15.dp)
            )
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                tint = Color(0xFF1D1B20),
                modifier = Modifier.size(15.dp)
            )
            // High fidelity simulated battery icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(1.dp, Color(0xFF1D1B20).copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                    .padding(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 9.dp)
                        .background(Color(0xFF2E7D32), RoundedCornerShape(1.dp))
                )
            }
            Text(
                text = "97%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20)
            )
        }
    }
}

@Composable
fun SettingsTabContent(viewModel: MainViewModel) {
    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsState()
    val googleAccountName by viewModel.googleAccountName.collectAsState()
    val googleAccountEmail by viewModel.googleAccountEmail.collectAsState()

    val deviceOptimizationPercent by viewModel.deviceOptimizationPercent.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()
    val rStatus by viewModel.deviceRamStatus.collectAsState()
    val sStatus by viewModel.deviceStorageStatus.collectAsState()

    var showGoogleDialog by remember { mutableStateOf(false) }

    if (showGoogleDialog) {
        GoogleSignInDialog(
            onDismiss = { showGoogleDialog = false },
            onSignIn = { name, email ->
                viewModel.signInWithGoogle(name, email)
                showGoogleDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3EDF7)) // Standard Samsung settings canvas
            .verticalScroll(rememberScrollState())
    ) {
        // One UI oversized title header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sozlamalar",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF1D1B20),
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Samsung One UI 6.1 (Android 14)",
                fontSize = 12.sp,
                color = Color(0xFF49454F),
                fontWeight = FontWeight.Medium
            )
        }

        // ====================================================================
        // Google Account Section (SAMSUNG STYLE ACCOUNTS BOARD)
        // ====================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(26.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isGoogleSignedIn) {
                            showGoogleDialog = true
                        }
                    }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored Google G Icon Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F3F4)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color(0xFF4285F4) // Google Blue
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (isGoogleSignedIn) {
                        Text(
                            text = googleAccountName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = googleAccountEmail,
                            fontSize = 12.sp,
                            color = Color(0xFF49454F)
                        )
                    } else {
                        Text(
                            text = "Google akkauntni qo'shish",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "Hisobot va tahlil ma'lumotlarini sinxronlash",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                }

                if (isGoogleSignedIn) {
                    IconButton(onClick = { viewModel.signOutGoogle() }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFB3261E)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFCAC4D0)
                    )
                }
            }
        }

        // ====================================================================
        // DARK MODE INTERACTIVE CARD (TUNGI REJIM TOGGLE)
        // ====================================================================
        val isDarkMode by viewModel.isDarkMode.collectAsState()
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(26.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleDarkMode() }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF6750A4).copy(alpha = 0.15f) else Color(0xFFFFECE0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = if (isDarkMode) Color(0xFF6750A4) else Color(0xFFFF8F00),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Tungi rejim (Dark Mode)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = if (isDarkMode) "Hozir qorong'u mavzu faol 🌌" else "Hozir yorug'u mavzu faol ☀️",
                            fontSize = 11.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                }
                
                androidx.compose.material3.Switch(
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode() },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF01875F),
                        uncheckedThumbColor = Color(0xFF49454F),
                        uncheckedTrackColor = Color(0xFFF1F3F4)
                    )
                )
            }
        }

        // ====================================================================
        // WALLPAPER SELECTION CARD ("FONNI ALMASHTIRISH")
        // ====================================================================
        val selectedWallpaper by viewModel.selectedWallpaper.collectAsState()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Ekran fonini o'zgartirish (Wallpaper) 🎨",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1D1B20)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bosh ekran uchun zamonaviy rang gradientlarini tanlang.",
                    fontSize = 11.sp,
                    color = Color(0xFF49454F)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val wallpapers = listOf(
                        Triple("aurora", "Aurora ✨", listOf(Color(0xFF8E24AA), Color(0xFF3F51B5))),
                        Triple("sunset", "Sunset 🌅", listOf(Color(0xFFFF4081), Color(0xFFFF5722))),
                        Triple("neon", "Neon ⚡", listOf(Color(0xFF00F5D4), Color(0xFF7B2CBF))),
                        Triple("ocean", "Ocean 🌊", listOf(Color(0xFF00E5FF), Color(0xFF006064))),
                        Triple("forest", "Forest 🌲", listOf(Color(0xFF69F0AE), Color(0xFF1B5E20))),
                        Triple("darkness", "Night 🌌", listOf(Color(0xFF455A64), Color(0xFF101214)))
                    )
                    
                    wallpapers.forEach { (wpKey, wpName, wpColors) ->
                        val isSel = selectedWallpaper == wpKey
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.selectWallpaper(wpKey) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Brush.sweepGradient(wpColors))
                                    .border(
                                        width = if (isSel) 3.dp else 1.dp,
                                        color = if (isSel) Color(0xFF01875F) else Color.LightGray,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Tanlangan",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = wpName,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color(0xFF01875F) else Color.DarkGray
                            )
                        }
                    }
                }
            }
        }

        // ====================================================================
        // DEVICE CARE SECTION (SAMSUNG DEVICE CARE GRAPHICS)
        // ====================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF3F51B5),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Qurilma parvarishi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1D1B20)
                        )
                    }
                    Text(
                        text = "$deviceOptimizationPercent / 100",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (deviceOptimizationPercent == 100) Color(0xFF2E7D32) else Color(0xFF6750A4)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gauge meter and optimize key
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Xotira (RAM): $rStatus",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F)
                        )
                        Text(
                            text = "Doimiy xotira: $sStatus",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F)
                        )
                        Text(
                            text = "Xavfsizlik: Knox Himoyalangan V3.10",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing circles manually
                        CircularProgressIndicator(
                            progress = { deviceOptimizationPercent / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = if (deviceOptimizationPercent == 100) Color(0xFF2E7D32) else Color(0xFF6750A4),
                            strokeWidth = 6.dp,
                            trackColor = Color(0xFFE8DEF8)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$deviceOptimizationPercent%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                            Text(
                                text = "A'lo",
                                fontSize = 9.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isOptimizing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF6750A4)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Keshlarni tozalash va RAM tezlashtirilmoqda...", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Button(
                        onClick = { viewModel.optimizeDevice() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Text(text = "Hozir optimallashtirish", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // ====================================================================
        // ADDITIONAL SAMSUNG SETTINGS CATEGORIES
        // ====================================================================
        Text(
            text = "TIZIM SOZLAMALARI",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF49454F),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp),
            letterSpacing = 1.2.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column {
                SettingsCategoryRow(
                    icon = Icons.Default.Wifi,
                    iconTint = Color(0xFF1E88E5),
                    title = "Ulanishlar",
                    summary = "Wi-Fi, Bluetooth, Sim karta ko'rsatmalari"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F3F4))
                SettingsCategoryRow(
                    icon = Icons.Default.VolumeUp,
                    iconTint = Color(0xFFEF5350),
                    title = "Tovushlar va tebranish",
                    summary = "Tebranish rejimi, Ovoz darajasi, Qo'ng'iroq ohangi"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F3F4))
                SettingsCategoryRow(
                    icon = Icons.Default.SettingsBrightness,
                    iconTint = Color(0xFFFFB300),
                    title = "Displey va Yorug'lik",
                    summary = "Ekran yorug'ligi, Qorong'u rejim, Shrift o'lchami"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F3F4))
                SettingsCategoryRow(
                    icon = Icons.Default.Shield,
                    iconTint = Color(0xFF4CAF50),
                    title = "Xavfsizlik va maxfiylik",
                    summary = "Biometrik ma'lumotlar, ruxsatnomalar, Knox tahlili"
                )
            }
        }

        // ====================================================================
        // TIME AND CLOCK ADJUSTMENT CARD
        // ====================================================================
        Text(
            text = "SANA VA VAQT SOZLAMASI",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF49454F),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp),
            letterSpacing = 1.2.sp
        )

        val customClockTime by viewModel.customClockTime.collectAsState()
        var hoursField by remember { mutableStateOf("12") }
        var minutesField by remember { mutableStateOf("00") }
        var isCustomUsed by remember(customClockTime) { mutableStateOf(customClockTime != null) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Avtomatik vaqt tizimi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "Haqiqiy internet vaqt ko'rsatkichi",
                            fontSize = 11.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                    Switch(
                        checked = !isCustomUsed,
                        onCheckedChange = { isChecked ->
                            isCustomUsed = !isChecked
                            if (isChecked) {
                                viewModel.updateCustomClockTime(null)
                            } else {
                                viewModel.updateCustomClockTime("$hoursField:$minutesField")
                            }
                        }
                    )
                }

                if (isCustomUsed) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Vaqtni sozlang (00:00 - 23:59)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF6750A4)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = hoursField,
                            onValueChange = { newVal ->
                                if (newVal.length <= 2 && newVal.all { it.isDigit() }) {
                                    hoursField = newVal
                                    val h = newVal.toIntOrNull() ?: 0
                                    if (h in 0..23) {
                                        viewModel.updateCustomClockTime(String.format("%02d:%02d", h, minutesField.toIntOrNull() ?: 0))
                                    }
                                }
                            },
                            label = { Text("Soat") },
                            placeholder = { Text("12") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Text(":", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                        OutlinedTextField(
                            value = minutesField,
                            onValueChange = { newVal ->
                                if (newVal.length <= 2 && newVal.all { it.isDigit() }) {
                                    minutesField = newVal
                                    val m = newVal.toIntOrNull() ?: 0
                                    if (m in 0..59) {
                                        viewModel.updateCustomClockTime(String.format("%02d:%02d", hoursField.toIntOrNull() ?: 0, m))
                                    }
                                }
                            },
                            label = { Text("Daqiqa") },
                            placeholder = { Text("00") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Vaqt darhol tizim status barida va vidjetlarda yangilanadi.",
                        fontSize = 10.sp,
                        color = Color(0xFF49454F)
                    )
                }
            }
        }

        // ====================================================================
        // ABOUT DEVICE INFO BOARD (SAMSUNG AUTHENTIC FEEL)
        // ====================================================================
        Text(
            text = "QURILMA HAQIDA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF49454F),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp),
            letterSpacing = 1.2.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .padding(bottom = 30.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF7E57C2),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Telefon haqida ma'lumotlar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1D1B20)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                RowAndVal("Qurilma nomi", "Samsung Galaxy S24 Ultra")
                RowAndVal("Model raqami", "SM-S928B/DS")
                RowAndVal("Seriya raqami", "R58XA0MKZLPZ")
                RowAndVal("IMEI raqami", "354157 / 44 / 881264 / 9")
                RowAndVal("Dasturiy versiya", "One UI 6.1 (Android 14)")
                RowAndVal("Knox himoyasi", "Knox 3.10 API 34")
            }
        }
    }
}

@Composable
fun SettingsCategoryRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    summary: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Simulate configuration toggle */ }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1D1B20)
            )
            Text(
                text = summary,
                fontSize = 11.sp,
                color = Color(0xFF49454F)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFCAC4D0).copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun RowAndVal(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Normal)
        Text(text = value, fontSize = 12.sp, color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GoogleSignInDialog(
    onDismiss: () -> Unit,
    onSignIn: (String, String) -> Unit
) {
    var nameField by remember { mutableStateOf("") }
    var emailField by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Customized Google Multi-color Logo visual
                Row(
                    modifier = Modifier.padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("G", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                    Text("o", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFFEA4335))
                    Text("o", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFFFBBC05))
                    Text("g", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                    Text("l", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF34A853))
                    Text("e", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFFEA4335))
                }

                Text(
                    text = "Google Akkauntga Kirish",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1D1B20)
                )

                Text(
                    text = "One UI bilan xavfsiz sinxronlash uchun hisob ma'lumotlarini kiriting.",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color(0xFF49454F),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("Ismingiz") },
                    placeholder = { Text("Masalan: Rasulov Abubakir") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = emailField,
                    onValueChange = { emailField = it },
                    label = { Text("Google E-pochta manzili") },
                    placeholder = { Text("username@gmail.com") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Bekor qilish", color = Color(0xFFB3261E))
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SAMSUNG MAIN RUNNING LAUNCHER (DESKTOP HOME SCREEN)
// ----------------------------------------------------
@Composable
fun SamsungHomeScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var showClockDialog by remember { mutableStateOf(false) }

    // Collect wallpaper state
    val selectedWallpaper by viewModel.selectedWallpaper.collectAsState()
    val recentGames by viewModel.recentGames.collectAsState()

    val wallpaperGradient = when (selectedWallpaper) {
        "sunset" -> listOf(Color(0xFFFF4081), Color(0xFFFF5722), Color(0xFFFFC107))
        "neon" -> listOf(Color(0xFF00F5D4), Color(0xFF7B2CBF), Color(0xFF10002B))
        "ocean" -> listOf(Color(0xFF00E5FF), Color(0xFF006064), Color(0xFF00363A))
        "forest" -> listOf(Color(0xFF69F0AE), Color(0xFF1B5E20), Color(0xFF0D3212))
        "darkness" -> listOf(Color(0xFF455A64), Color(0xFF101214), Color(0xFF000000))
        else -> listOf(Color(0xFF8E24AA), Color(0xFF3F51B5), Color(0xFF1A237E)) // aurora
    }

    // Collect clock state
    val customClockTime by viewModel.customClockTime.collectAsState()
    var systemTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            systemTime = sdf.format(Date())
            delay(1000)
        }
    }
    val currentDisplayTime = customClockTime ?: systemTime

    // Date formatting
    val currentDisplayDate = remember(currentDisplayTime) {
        val sdf = SimpleDateFormat("EEEE, d-MMMM", Locale("uz"))
        sdf.format(Date())
    }

    if (showClockDialog) {
        QuickClockSetDialog(
            viewModel = viewModel,
            onDismiss = { showClockDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = wallpaperGradient,
                    radius = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp, top = 24.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // ====================================================================
            // SAMSUNG GALAXY CLASSIC CLOCK & WEATHER WIDGET (CLICKABLE)
            // ====================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .clickable { showClockDialog = true }
                    .padding(horizontal = 14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentDisplayTime,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentDisplayDate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Toshkent • 28°C Quyoshli",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vaqtni sozlash uchun bu yerga bosing ⏱️",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Light
                    )
                }
            }

            // ====================================================================
            // CURRENTLY PLAYED RECENT GAMES ("SO'NGGI O'YINLAR")
            // ====================================================================
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = "SO'NGGI O'YINLAR 🎮",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 14.dp, end = 14.dp, bottom = 8.dp)
            )

            if (recentGames.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Hali o'yin o'ynalmadi. Play Market orqali mitti o'yinlarni boshlang!",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    recentGames.forEach { gId ->
                        val info = when (gId) {
                            "climber" -> mapOf("title" to "Endless Climb", "color" to "0xFF673AB7", "icon" to "navigation")
                            "ttt" -> mapOf("title" to "Xol Nol", "color" to "0xFF3F51B5", "icon" to "grid")
                            "memory" -> mapOf("title" to "Matrix Match", "color" to "0xFFE91E63", "icon" to "extension")
                            "calc" -> mapOf("title" to "Valyuta Kursi", "color" to "0xFF4CAF50", "icon" to "exchange")
                            "roblox" -> mapOf("title" to "Roblox Sim", "color" to "0xFF212121", "icon" to "gamepad")
                            "ludo" -> mapOf("title" to "Ludo 4-Player", "color" to "0xFFD32F2F", "icon" to "sports")
                            "bombsquad" -> mapOf("title" to "BombSquad 4", "color" to "0xFF388E3C", "icon" to "sports")
                            "monopoly" -> mapOf("title" to "Monopoly 4", "color" to "0xFF1976D2", "icon" to "sports")
                            "uno" -> mapOf("title" to "Uno! Cards", "color" to "0xFFF57C00", "icon" to "sports")
                            "fourplayer" -> mapOf("title" to "4 Pl Arena", "color" to "0xFF7B1FA2", "icon" to "sports")
                            "instagram" -> mapOf("title" to "Instagram", "color" to "0xFFE1306C", "icon" to "camera")
                            else -> mapOf("title" to "O'yin", "color" to "0xFF01875F", "icon" to "gamepad")
                        }

                        val cardColor = Color(android.graphics.Color.parseColor((info["color"] ?: "0xFF01875F").replace("0x", "#")))
                        val iconVec = when (info["icon"]) {
                            "navigation" -> Icons.Default.Navigation
                            "grid" -> Icons.Default.GridOn
                            "extension" -> Icons.Default.Extension
                            "exchange" -> Icons.Default.CurrencyExchange
                            "camera" -> Icons.Default.CameraAlt
                            "gamepad" -> Icons.Default.Gamepad
                            "sports" -> Icons.Default.SportsEsports
                            else -> Icons.Default.SportsEsports
                        }

                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (gId == "climber") viewModel.startClimbingGame()
                                    if (gId == "ttt") viewModel.resetTicTacToe()
                                    if (gId == "memory") viewModel.startMemoryGame()
                                    viewModel.selectGameScreen(gId)
                                },
                            colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.82f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color.White.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconVec,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = info["title"] ?: "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ====================================================================
            // INSTALLED CORE APPS GRID SCREEN (LAUNCHER APPLICATIONS)
            // ====================================================================
            Text(
                text = "ILOVAlAR RO'YXATI",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Application 1: Reports
                LauncherIconItem(
                    title = "Hisobotlar",
                    icon = Icons.Default.Assessment,
                    iconGradient = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)),
                    onClick = { viewModel.selectTab("reports") }
                )

                // Application 2: Play Market (Google Play Replica!)
                LauncherIconItem(
                    title = "Play Market",
                    icon = Icons.Default.Storefront,
                    iconGradient = listOf(Color(0xFF1565C0), Color(0xFF00B0FF)),
                    onClick = { viewModel.selectTab("playmarket") }
                )

                // Application 3: Settings (One UI Config)
                LauncherIconItem(
                    title = "Sozlamalar",
                    icon = Icons.Default.Settings,
                    iconGradient = listOf(Color(0xFF37474F), Color(0xFF78909C)),
                    onClick = { viewModel.selectTab("settings") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Application 4: Instagram Simulator
                LauncherIconItem(
                    title = "Instagram",
                    icon = Icons.Default.CameraAlt,
                    iconGradient = listOf(Color(0xFF833AB4), Color(0xFFF56040)),
                    onClick = { viewModel.selectGameScreen("instagram") }
                )

                // Application 5: Telegram Simulator
                LauncherIconItem(
                    title = "Telegram",
                    icon = Icons.Default.Send,
                    iconGradient = listOf(Color(0xFF24A1DE), Color(0xFF179CDE)),
                    onClick = { viewModel.selectGameScreen("telegram") }
                )

                // Application 6: Roblox Simulator
                LauncherIconItem(
                    title = "Roblox",
                    icon = Icons.Default.Gamepad,
                    iconGradient = listOf(Color(0xFF212121), Color(0xFF424242)),
                    onClick = { viewModel.selectGameScreen("roblox") }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Helper notification line
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Samsung One UI 6.1 Simulator Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Yuqoridan pastga tortib parda Quick Panel sozlamalarini boshqaring.",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

// Small Composable for custom launchers
@Composable
fun LauncherIconItem(
    title: String,
    icon: ImageVector,
    iconGradient: List<Color>,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(82.dp)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(iconGradient)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ----------------------------------------------------
// SAMSUNG QUICK PANEL & NOTIFICATION SHADE (PARDA) OVERLAY
// ----------------------------------------------------
@Composable
fun SamsungNotificationShadeOverlay(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val customClockTime by viewModel.customClockTime.collectAsState()
    var systemTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            systemTime = sdf.format(Date())
            delay(1000)
        }
    }
    val activeTime = customClockTime ?: systemTime

    val activeFlashlight by viewModel.cameraFlashOn.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    var isWifiOn by remember { mutableStateOf(true) }
    var isBluetoothOn by remember { mutableStateOf(false) }
    var isSoundOn by remember { mutableStateOf(true) }
    var isAutoRotate by remember { mutableStateOf(true) }
    var brightnessPercentage by remember { mutableStateOf(0.75f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6121212)) // Dark translucent One UI panel
            .padding(horizontal = 16.dp)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Upper panel header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = activeTime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
                Text(
                    text = "Lola, 8-iyun",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        onClose()
                        viewModel.selectTab("settings")
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Sozlamalar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onClose() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Yopish",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ====================================================================
        // ONE UI DYNAMIC QUICK SETTINGS TOGGLES (CIRCLE RUNNERS)
        // ====================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickSettingsToggleItem(
                label = "Wi-Fi",
                icon = Icons.Default.Wifi,
                isActive = isWifiOn,
                onClick = {
                    isWifiOn = !isWifiOn
                    android.widget.Toast.makeText(context, if (isWifiOn) "Wi-Fi yoqildi" else "Wi-Fi o'chirildi", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
            QuickSettingsToggleItem(
                label = "Bluetooth",
                icon = Icons.Default.Bluetooth,
                isActive = isBluetoothOn,
                onClick = {
                    isBluetoothOn = !isBluetoothOn
                    android.widget.Toast.makeText(context, if (isBluetoothOn) "Bluetooth ulandi" else "Bluetooth uzildi", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
            QuickSettingsToggleItem(
                label = "Tovush",
                icon = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                isActive = isSoundOn,
                onClick = {
                    isSoundOn = !isSoundOn
                    android.widget.Toast.makeText(context, if (isSoundOn) "Tovush jiringlash rejimida" else "Tovush tebranish rejimida" , android.widget.Toast.LENGTH_SHORT).show()
                }
            )
            QuickSettingsToggleItem(
                label = "Chiroq",
                icon = Icons.Default.FlashlightOn,
                isActive = activeFlashlight,
                onClick = {
                    viewModel.toggleCameraFlash()
                    android.widget.Toast.makeText(context, if (!activeFlashlight) "Orqa chiroq (Flashlight) yoqildi!" else "Chiroq o'chirildi", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickSettingsToggleItem(
                label = "Burish",
                icon = Icons.Default.ScreenRotation,
                isActive = isAutoRotate,
                onClick = { isAutoRotate = !isAutoRotate }
            )
            QuickSettingsToggleItem(
                label = "GPS",
                icon = Icons.Default.LocationOn,
                isActive = true,
                onClick = {}
            )
            QuickSettingsToggleItem(
                label = "Tungi rejim",
                icon = Icons.Default.DarkMode,
                isActive = isDarkMode,
                onClick = {
                    viewModel.toggleDarkMode()
                    android.widget.Toast.makeText(context, if (!isDarkMode) "Tungi rejim yoqildi" else "Tungi rejim o'chirildi", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
            QuickSettingsToggleItem(
                label = "Profil",
                icon = Icons.Default.AccountCircle,
                isActive = false,
                onClick = {
                    onClose()
                    viewModel.selectTab("settings")
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ====================================================================
        // PROGRESS BRIGHTNESS SLIDER
        // ====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Slider(
                value = brightnessPercentage,
                onValueChange = { brightnessPercentage = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00B0FF),
                    activeTrackColor = Color(0xFF00B0FF),
                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${(brightnessPercentage * 100).toInt()}%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Notification center list
        Text(
            text = "BILDIRISHNOMALAR",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Notif 1
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF34A853),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Google Play Protect xabari",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Qurilmangiz to'liq himoyalangan va xavfsiz holatda.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(14.dp))

                // Notif 2
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFFE8DEF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Vaqtni sozlash imkoni mavjud",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Sozlamalar bo'limidan yoki Clock widgetidan soatni qo'lda o'zgartiring.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ====================================================================
        // "PASGA TUSHIRADIGANI": PULL-DOWN / COLLAPSE INDICATOR HANDLE AT BOTTOM
        // ====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClose() }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.45f))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Yopish uchun bosing yoki yuqoriga suring",
                    color = Color.White.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// Small Composable helper for Circle panel icon
@Composable
fun QuickSettingsToggleItem(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFF00B0FF) else Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}


// ----------------------------------------------------
// QUICK CLOCK SET POPUP DIALOG
// ----------------------------------------------------
@Composable
fun QuickClockSetDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hourVal by remember { mutableStateOf("12") }
    var minVal by remember { mutableStateOf("00") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tezkor Soat O'zgartiruvchi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )
                Text(
                    text = "Status bar va asosiy soat ko'rsatkichini tezkor sozlang.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = hourVal,
                        onValueChange = { newVal ->
                            if (newVal.length <= 2 && newVal.all { it.isDigit() }) {
                                hourVal = newVal
                            }
                        },
                        label = { Text("Soat") },
                        placeholder = { Text("12") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Text(":", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                    OutlinedTextField(
                        value = minVal,
                        onValueChange = { newVal ->
                            if (newVal.length <= 2 && newVal.all { it.isDigit() }) {
                                minVal = newVal
                            }
                        },
                        label = { Text("Daqiqa") },
                        placeholder = { Text("00") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.updateCustomClockTime(null) // Reset to auto
                            android.widget.Toast.makeText(context, "Avtomatik real soat o'rnatildi", android.widget.Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Real soat", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val h = hourVal.toIntOrNull() ?: 12
                            val m = minVal.toIntOrNull() ?: 0
                            if (h in 0..23 && m in 0..59) {
                                viewModel.updateCustomClockTime(String.format("%02d:%02d", h, m))
                                android.widget.Toast.makeText(context, "Soat sozlangandi: $hourVal:$minVal", android.widget.Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                android.widget.Toast.makeText(context, "Noto'g'ri soat yoki daqiqa kiritildi!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Text("Sozlash", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}


// ====================================================================
// ROBLOX SANDBOX SIMULATOR WORKSPACE
// ====================================================================
@Composable
fun RobloxSimulatorGameView(onBack: () -> Unit) {
    var playerColor by remember { mutableStateOf(Color(0xFFFFD54F)) }
    var hatStyle by remember { mutableStateOf("No Hat") }
    var faceStyle by remember { mutableStateOf("Smile") }
    var backItem by remember { mutableStateOf("None") }
    var charOffsetX by remember { mutableStateOf(0f) }
    var charOffsetY by remember { mutableStateOf(0f) }
    var pointsCount by remember { mutableStateOf(100) }
    val builtBlocks = remember { mutableStateListOf<Pair<Float, Float>>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ROBLOX SANDBOX SIMULATOR",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Robux: $$pointsCount  •  G'ishtlar soni: ${builtBlocks.size}",
            fontSize = 12.sp,
            color = Color.Yellow,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF121212))
                .border(2.dp, Color.DarkGray, RoundedCornerShape(16.dp))
        ) {
            builtBlocks.forEach { block ->
                Box(
                    modifier = Modifier
                        .offset(x = block.first.dp, y = block.second.dp)
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE53935))
                        .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = charOffsetX.dp, y = charOffsetY.dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(playerColor)
                    .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (faceStyle) {
                            "Smile" -> "☺"
                            "Cool" -> "😎"
                            "Wink" -> "😉"
                            else -> "🐱"
                        },
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    if (backItem != "None") {
                        Text(
                            text = when (backItem) {
                                "Wings" -> "Ƹ̵̡Ӝ̵̨̄Ʒ"
                                "Cape" -> "██"
                                else -> "⚔"
                            },
                            fontSize = 8.sp,
                            color = Color.White
                        )
                    }
                }

                if (hatStyle != "No Hat") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-10).dp)
                            .background(Color.Black)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = hatStyle.take(3).uppercase(),
                            fontSize = 6.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { charOffsetY = (charOffsetY - 12f).coerceIn(-70f, 70f) },
                    modifier = Modifier.background(Color.DarkGray, CircleShape).size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Tepaga", tint = Color.White)
                }
                Row {
                    IconButton(
                        onClick = { charOffsetX = (charOffsetX - 12f).coerceIn(-120f, 120f) },
                        modifier = Modifier.background(Color.DarkGray, CircleShape).size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Chapga", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(36.dp))
                    IconButton(
                        onClick = { charOffsetX = (charOffsetX + 12f).coerceIn(-120f, 120f) },
                        modifier = Modifier.background(Color.DarkGray, CircleShape).size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "O'ngga", tint = Color.White)
                    }
                }
                IconButton(
                    onClick = { charOffsetY = (charOffsetY + 12f).coerceIn(-70f, 70f) },
                    modifier = Modifier.background(Color.DarkGray, CircleShape).size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Pastga", tint = Color.White)
                }
            }

            Column(modifier = Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        builtBlocks.add(Pair(charOffsetX, charOffsetY))
                        pointsCount += 25
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Blok Qo'shish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        builtBlocks.clear()
                        charOffsetX = 0f
                        charOffsetY = 0f
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Tozalash", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("AVATAR JIXOZLARI", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Teri rangi:  ", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(80.dp))
                listOf(Color(0xFFFFD54F), Color(0xFF29B6F6), Color(0xFF66BB6A), Color(0xFFEF5350), Color(0xFFAB47BC)).forEach { col ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(col)
                            .border(if (playerColor == col) 2.dp else 0.dp, Color.White, CircleShape)
                            .clickable { playerColor = col }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bosh kiyimi:  ", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(80.dp))
                listOf("No Hat", "Crown", "Cap", "Halo").forEach { style ->
                    val isSel = hatStyle == style
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF01875F) else Color.DarkGray)
                            .clickable { hatStyle = style }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(style, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Yuz ko'rinishi:  ", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(80.dp))
                listOf("Smile", "Cool", "Wink", "Kitty").forEach { style ->
                    val isSel = faceStyle == style
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF01875F) else Color.DarkGray)
                            .clickable { faceStyle = style }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(style, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Orqasida:  ", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(80.dp))
                listOf("None", "Wings", "Cape", "Sword").forEach { style ->
                    val isSel = backItem == style
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF01875F) else Color.DarkGray)
                            .clickable { backItem = style }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(style, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Chiqish", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}


// ====================================================================
// MULTIPLAYER 4-PLAYER GAMES ARENA SIMULATOR
// ====================================================================
@Composable
fun MultiPlayer4GamesView(gameType: String, onBack: () -> Unit) {
    val gameTitle = when(gameType) {
        "ludo" -> "Ludo King (4 kishilik)"
        "bombsquad" -> "BombSquad (4 kishilik)"
        "monopoly" -> "Monopoly (4 kishilik)"
        "uno" -> "Uno! Cards (4 kishilik)"
        else -> "4 Player Arena"
    }

    var name1 by remember { mutableStateOf("Sardor") }
    var name2 by remember { mutableStateOf("Dilshod") }
    var name3 by remember { mutableStateOf("Lola") }
    var name4 by remember { mutableStateOf("Bekzod") }

    var p1Score by remember { mutableStateOf(0) }
    var p2Score by remember { mutableStateOf(0) }
    var p3Score by remember { mutableStateOf(0) }
    var p4Score by remember { mutableStateOf(0) }

    var activePlayerIndex by remember { mutableStateOf(0) } // 0, 1, 2, 3
    val gameLogs = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        gameLogs.add("?? O'yin boshlandi! Navbat: Sardor")
    }

    var isBombActive by remember { mutableStateOf(false) }
    var bombCountdown by remember { mutableStateOf(3) }
    var winningTaps by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFEF7FF))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = gameTitle.uppercase(),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFB71C1C)
        )
        Text(
            text = "Yuzma-yuz 4 kishilik raqobatli stol o'yini simulyatori",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(if (activePlayerIndex == 0) 3.dp else 0.dp, Color(0xFFD32F2F), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(name1, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    Text("$p1Score Pts", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(if (activePlayerIndex == 1) 3.dp else 0.dp, Color(0xFF1976D2), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(name2, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    Text("$p2Score Pts", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(if (activePlayerIndex == 2) 3.dp else 0.dp, Color(0xFF2E7D32), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(name3, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text("$p3Score Pts", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(if (activePlayerIndex == 3) 3.dp else 0.dp, Color(0xFFEF6C00), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(name4, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD84315))
                    Text("$p4Score Pts", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val currentTurnUser = when(activePlayerIndex) {
                    0 -> name1
                    1 -> name2
                    2 -> name3
                    else -> name4
                }

                Text(
                    text = "Hozirgi navbat: $currentTurnUser".uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                when(gameType) {
                    "ludo", "monopoly" -> {
                        var rolledDiceValue by remember { mutableStateOf(1) }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFB71C1C))
                                    .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (rolledDiceValue) {
                                        1 -> "1"
                                        2 -> "2"
                                        3 -> "3"
                                        4 -> "4"
                                        5 -> "5"
                                        else -> "6"
                                    },
                                    fontSize = 24.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Button(
                                onClick = {
                                    val roll = kotlin.random.Random.nextInt(1, 7)
                                    rolledDiceValue = roll
                                    when (activePlayerIndex) {
                                        0 -> p1Score += roll
                                        1 -> p2Score += roll
                                        2 -> p3Score += roll
                                        3 -> p4Score += roll
                                    }
                                    gameLogs.add("🎲 $currentTurnUser zarni tashladi: $roll ochko qo'shildi!")
                                    activePlayerIndex = (activePlayerIndex + 1) % 4
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F))
                            ) {
                                Text("Zar Tashlash 🎲", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    "bombsquad" -> {
                        if (!isBombActive) {
                            Button(
                                onClick = {
                                    isBombActive = true
                                    winningTaps = 0
                                    bombCountdown = 3
                                    gameLogs.add("💣 $currentTurnUser bombani ishga tushirdi! Tezroq bosing!")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Text("Bombani yoqish 💣", fontWeight = FontWeight.Bold, color = Color.Yellow)
                            }
                        } else {
                            Text(
                                text = "Bomba portlashiga: $bombCountdown soniya! 💥",
                                fontSize = 14.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    winningTaps += 1
                                    if (winningTaps >= 5) {
                                        isBombActive = false
                                        when (activePlayerIndex) {
                                            0 -> p1Score += 15
                                            1 -> p2Score += 15
                                            2 -> p3Score += 15
                                            3 -> p4Score += 15
                                        }
                                        gameLogs.add("🛡️ $currentTurnUser bombani muvaffaqiyatli zarasizlantirdi! (+15 Pts)")
                                        activePlayerIndex = (activePlayerIndex + 1) % 4
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("TEZ BOSING ($winningTaps / 5) ⚡", fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        LaunchedEffect(isBombActive) {
                            if (isBombActive) {
                                while (bombCountdown > 0 && isBombActive) {
                                    delay(1000)
                                    bombCountdown -= 1
                                }
                                if (isBombActive) {
                                    isBombActive = false
                                    when (activePlayerIndex) {
                                        0 -> p1Score = (p1Score - 10).coerceAtLeast(0)
                                        1 -> p2Score = (p2Score - 10).coerceAtLeast(0)
                                        2 -> p3Score = (p3Score - 10).coerceAtLeast(0)
                                        3 -> p4Score = (p4Score - 10).coerceAtLeast(0)
                                    }
                                    gameLogs.add("💥 BOOM! Bomba portlab ketdi, $currentTurnUser zarar ko'rdi (-10 Pts)!")
                                    activePlayerIndex = (activePlayerIndex + 1) % 4
                                }
                            }
                        }
                    }

                    else -> {
                        val unoColors = listOf("Qizil (Red)", "Sariq (Yellow)", "Yashil (Green)", "Ko'k (Blue)")
                        val unoNumbers = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+2", "+4", "Reverse", "Skip")

                        Button(
                            onClick = {
                                val ranCol = unoColors.random()
                                val ranNum = unoNumbers.random()
                                when (activePlayerIndex) {
                                    0 -> p1Score += 10
                                    1 -> p2Score += 10
                                    2 -> p3Score += 10
                                    3 -> p4Score += 10
                                }
                                gameLogs.add("🃏 $currentTurnUser o'ynadi: $ranCol $ranNum (+10 Pts)")
                                activePlayerIndex = (activePlayerIndex + 1) % 4
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                        ) {
                            Text("Qog'oz o'ynash 🃏", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("O'YIN JURNALI (LOGS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(gameLogs.reversed()) { log ->
                    Text(log, fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    p1Score = 0
                    p2Score = 0
                    p3Score = 0
                    p4Score = 0
                    gameLogs.clear()
                    gameLogs.add("🔄 O'yin ochkolari nollandi va boshlandi!")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Nollash", color = Color.Black)
            }

            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Chiqish", color = Color.White)
            }
        }
    }
}

// ====================================================================
// HIGH-FIDELITY COMFORTABLE INSTAGRAM SIMULATOR VIEW (DYNAMIC & WORKING!)
// ====================================================================
@Composable
fun InstagramSimulatorView() {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Story database
    val stories = remember {
        listOf(
            mapOf("username" to "rasulov_01", "avatar" to "💻", "storyImg" to "Siz o'z hisobot dasturingizni deyarli bitirdingiz! ✨ Shovqinli ish kabi davom etamiz. Muvaffaqiyat yo'lida doim olg'a! 💻🚀"),
            mapOf("username" to "messi_10", "avatar" to "⚽", "storyImg" to "Winning with Team Inter Miami! ⚽🔥 Vamos! 🏆 We play to make everyone proud of us! 💪"),
            mapOf("username" to "tech_insider", "avatar" to "📱", "storyImg" to "Exclusive info: Galaxy S27 Ultra concept leaks with under-screen camera, triple fold and infinite battery style! 🔋🤩"),
            mapOf("username" to "samarqand_today", "avatar" to "🏰", "storyImg" to "Registon maydonida chiroyli tun manzaralari! 🌌 Kelajak sayohatchilari uchun sehrgarlik dunyosining o'zi. 🏰🕌")
        )
    }

    var selectedStory by remember { mutableStateOf<Map<String, String>?>(null) }

    // Interactive Post States
    var p1Liked by remember { mutableStateOf(false) }
    var p1LikesCount by remember { mutableStateOf(342) }
    val p1Comments = remember {
        mutableStateListOf(
            "nodir" to "Dastur dizayni ajoyib chiqibdi! 👍",
            "shaxzod_dev" to "Samsung One UI 6.1 qismlari zo'r o'xshabdi"
        )
    }
    var p1NewComment by remember { mutableStateOf("") }

    var p2Liked by remember { mutableStateOf(false) }
    var p2LikesCount by remember { mutableStateOf(9214) }
    val p2Comments = remember {
        mutableStateListOf(
            "leo_fan" to "True G.O.A.T! 🐐⚽",
            "cr7_king" to "Respekt Leo, har doimgidek ajoyib o'yin!"
        )
    }
    var p2NewComment by remember { mutableStateOf("") }

    var p3Liked by remember { mutableStateOf(false) }
    var p3LikesCount by remember { mutableStateOf(65) }
    val p3Comments = remember {
        mutableStateListOf(
            "ai_engineer" to "Android with Gemini is insanely powerful! 🧠⚡"
        )
    }
    var p3NewComment by remember { mutableStateOf("") }

    // Draw main screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Pitch Black dark theme
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. Instagram Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mimicking Serif instagram logo
                Text(
                    text = "Instagram Sim",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Bildirishnomalar", tint = Color.White)
                    Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Xabarlar", tint = Color.White)
                }
            }

            // Outer scrollable Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                
                // 2. Stories Section (Bubble lists!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 14.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current User's custom story bubble
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            android.widget.Toast.makeText(context, "Mening tarixim qo'shish!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Sizning tarixingiz", fontSize = 10.sp, color = Color.Gray, maxLines = 1)
                    }

                    // Friends' story bubble list
                    stories.forEach { story ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedStory = story }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .border(
                                        width = 2.5.dp,
                                        brush = Brush.linearGradient(
                                            listOf(Color(0xFFF56040), Color(0xFFC13584), Color(0xFF833AB4))
                                        ),
                                        shape = CircleShape
                                    )
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1E1E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(story["avatar"] ?: "🔥", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(story["username"] ?: "", fontSize = 10.sp, color = Color.LightGray, maxLines = 1)
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                // 3. Posts list Section
                // POST 1: rasulov_01
                InstagramPostItem(
                    username = "rasulov_01",
                    location = "Toshkent, O'zbekiston",
                    desc = "Android One UI 6.1 simulyatori tayyor! So'nggi o'yinlar hamda chiroyli dynamic fon almashtirgichlarni qo'shdik. Sinab ko'ring va fikringizni bildiring! 💻⚡🏆 #uzb #android #kotlin",
                    avatarEmoji = "💻",
                    postColors = listOf(Color(0xFF003049), Color(0xFFD62828)),
                    patternEmoji = "⚡",
                    likesCount = p1LikesCount,
                    isLiked = p1Liked,
                    onLikeToggle = {
                        p1Liked = !p1Liked
                        if (p1Liked) p1LikesCount++ else p1LikesCount--
                    },
                    commentsList = p1Comments,
                    newCommentText = p1NewComment,
                    onCommentTextChange = { p1NewComment = it },
                    onPostComment = {
                        if (p1NewComment.isNotBlank()) {
                            p1Comments.add("Siz" to p1NewComment.trim())
                            p1NewComment = ""
                        }
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

                // POST 2: messi_10
                InstagramPostItem(
                    username = "messi_10",
                    location = "Key Biscayne, Florida",
                    desc = "Haqiqiy g'alabalar kurashlarda chiniqadi! Keyingi o'yinlarga tayyormiz. Hammani qo'llab-quvvatlagani uchun rahmat! ⚽🏆🔥 #messi #football #mls",
                    avatarEmoji = "⚽",
                    postColors = listOf(Color(0xFF4361EE), Color(0xFF7209B7)),
                    patternEmoji = "👑",
                    likesCount = p2LikesCount,
                    isLiked = p2Liked,
                    onLikeToggle = {
                        p2Liked = !p2Liked
                        if (p2Liked) p2LikesCount++ else p2LikesCount--
                    },
                    commentsList = p2Comments,
                    newCommentText = p2NewComment,
                    onCommentTextChange = { p2NewComment = it },
                    onPostComment = {
                        if (p2NewComment.isNotBlank()) {
                            p2Comments.add("Siz" to p2NewComment.trim())
                            p2NewComment = ""
                        }
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

                // POST 3: tech_insider
                InstagramPostItem(
                    username = "tech_insider",
                    location = "San Francisco, CA",
                    desc = "Yangi Android va Sun'iy Idrok aloqalari borgan sari rivojlanmoqda. Mobil ilova yozish jarayonlarining tubburilish davri keldi unutmang! 🤖🦾🧠 #ai #tech #galaxy",
                    avatarEmoji = "📱",
                    postColors = listOf(Color(0xFF0F9D58), Color(0xFF4285F4)),
                    patternEmoji = "🛰️",
                    likesCount = p3LikesCount,
                    isLiked = p3Liked,
                    onLikeToggle = {
                        p3Liked = !p3Liked
                        if (p3Liked) p3LikesCount++ else p3LikesCount--
                    },
                    commentsList = p3Comments,
                    newCommentText = p3NewComment,
                    onCommentTextChange = { p3NewComment = it },
                    onPostComment = {
                        if (p3NewComment.isNotBlank()) {
                            p3Comments.add("Siz" to p3NewComment.trim())
                            p3NewComment = ""
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Story View Overlay block
    if (selectedStory != null) {
        Dialog(onDismissRequest = { selectedStory = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                border = BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFFF56040), Color(0xFFC13584), Color(0xFF833AB4))))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Story top header (avatar + name + close)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(selectedStory!!["avatar"] ?: "😎", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = selectedStory!!["username"] ?: "",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(onClick = { selectedStory = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Yopish", tint = Color.White)
                        }
                    }

                    // Simulated Story Screen visual
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFE1306C), Color(0xFFC13584), Color(0xFF405DE6))
                                )
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(selectedStory!!["avatar"] ?: "🌟", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = selectedStory!!["storyImg"] ?: "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    // Story bottom action: "Xabar yuborish..."
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("Tezkor javob...", color = Color.Gray, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            ),
                            enabled = false // Mock text box but visually complete!
                        )
                        IconButton(onClick = {
                            android.widget.Toast.makeText(context, "Reaksiya yuborildi! 🔥", android.widget.Toast.LENGTH_SHORT).show()
                            selectedStory = null
                        }) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// Sub Composable representing an individual Interactive Instagram Post Item
@Composable
fun InstagramPostItem(
    username: String,
    location: String,
    desc: String,
    avatarEmoji: String,
    postColors: List<Color>,
    patternEmoji: String,
    likesCount: Int,
    isLiked: Boolean,
    onLikeToggle: () -> Unit,
    commentsList: List<Pair<String, String>>,
    newCommentText: String,
    onCommentTextChange: (String) -> Unit,
    onPostComment: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.Black)
    ) {
        // A. Post Top Profile row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .border(
                            1.5.dp,
                            Brush.linearGradient(listOf(Color(0xFFF56040), Color(0xFFC13584))),
                            CircleShape
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF212121)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(avatarEmoji, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = location, color = Color.Gray, fontSize = 11.sp)
                }
            }
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Yana", tint = Color.LightGray)
            }
        }

        // B. Post Media Canvas (Stylized Image drawing!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Brush.radialGradient(postColors))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(patternEmoji, fontSize = 84.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = username.uppercase(),
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 4.sp
                )
            }
        }

        // C. Interactive Control row (Liked/Comment buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Heart button (loved/unloved state toggling!)
                IconButton(onClick = onLikeToggle) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFFFF2F54) else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // D. Likes Display Area
        Text(
            text = "$likesCount kishi yoqtirdi",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
        )

        // E. Caption Row
        Text(
            text = androidx.compose.ui.text.buildAnnotatedString {
                append("$username ")
                addStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = Color.White), start = 0, end = username.length)
                append(desc)
            },
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
        )

        // F. Comments section container
        if (commentsList.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .background(Color(0xFF121212), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Barcha sharhlar:",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                commentsList.forEach { (cUser, cText) ->
                    Text(
                        text = androidx.compose.ui.text.buildAnnotatedString {
                            append("@$cUser: ")
                            addStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = Color.White), start = 0, end = cUser.length + 2)
                            append(cText)
                        },
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        // G. Add live commenting input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newCommentText,
                onValueChange = onCommentTextChange,
                placeholder = { Text("Fikr bildirish...", color = Color.Gray, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF121212),
                    unfocusedContainerColor = Color(0xFF121212),
                    focusedBorderColor = Color.White.copy(alpha = 0.4f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                ),
                singleLine = true
            )
            
            Button(
                onClick = onPostComment,
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C))
            ) {
                Text("Chop etish", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

// ====================================================================
// ELEGANT TELEGRAM MESSENGER HIGH-FIDELITY SIMULATOR CARD
// ====================================================================

data class TelegramChatItem(
    val username: String,
    val fullName: String,
    val avatarEmoji: String,
    val bio: String,
    val status: String,
    val isVerified: Boolean = false,
    val isBot: Boolean = false
)

@Composable
fun TelegramSimulatorView(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    // Obtain Google user state to configure self-account dynamically
    val googleAccountName by viewModel.googleAccountName.collectAsState()
    val googleAccountEmail by viewModel.googleAccountEmail.collectAsState()
    
    val selfName = googleAccountName.ifBlank { "Abubakir Rasulov 💻" }
    val selfEmail = googleAccountEmail.ifBlank { "rasulovabubakir11@gmail.com" }
    val selfUsername = "rasulov_01"

    // Search and routing states
    var searchQuery by remember { mutableStateOf("") }
    var activeChatUsername by remember { mutableStateOf<String?>(null) } // null means list view, else matching username
    
    // Message database stored reactively in memory
    val chatsMessages = remember {
        mutableStateMapOf<String, List<TelegramMessage>>(
            "saved_messages" to listOf(
                TelegramMessage("me", "Hisobotlarni yangilash va Dark Made tugmalari to'liq imtihon qilindi!", "12:30"),
                TelegramMessage("me", "Telegram modul ham ish kutyapti. Juda qulay dizayn!", "12:32")
            ),
            "durov" to listOf(
                TelegramMessage("durov", "Salom! Men Pavel Durov. Telegram simulyatoriga xush kelibsiz! 🛡️", "11:15"),
                TelegramMessage("me", "Salom Pavel! Telegram dizayni ajoyib ishlanyapti.", "11:18"),
                TelegramMessage("durov", "Rahmat! Shaxsiy xavfsizlik va tezlikni ta'minlash bizning ustuvor vazifamizdir.", "11:20")
            ),
            "messi_10" to listOf(
                TelegramMessage("messi_10", "Muchas gracias por escribirme, un saludo grande a todos en Uzbekistán! ⚽🇦🇷", "Kecha")
            ),
            "aistudio_bot" to listOf(
                TelegramMessage("aistudio_bot", "Salom! Men AI Studio Virtual Assistant botiman. Menga istalgan fikrni yuborishingiz mumkin! 🤖", "09:00")
            )
        )
    }

    var isTyping by remember { mutableStateOf(false) }
    var inputMessageText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Configured list of searchable users
    val databaseUsers = remember(selfName, selfEmail) {
        listOf(
            TelegramChatItem("saved_messages", "Saqlangan xabarlar (Saved Messages)", "💾", "O'zingiz uchun shaxsiy eslatmalar va fayllar buluti", "bulut xizmati"),
            TelegramChatItem(selfUsername, "$selfName (Siz)", "👤", "Samsung One UI ishqibozi | $selfEmail", "online"),
            TelegramChatItem("durov", "Pavel Durov", "🦅", "Privacy is not for sale.", "online", isVerified = true),
            TelegramChatItem("messi_10", "Leo Messi", "⚽", "Campeón del Mundo 🇦🇷", "yaqinda bo'lgan"),
            TelegramChatItem("aistudio_bot", "AI Studio Helper Bot", "🤖", "Sun'iy intellekt bo'yicha simulyator bot", "bot", isBot = true)
        )
    }

    // Interactive adaptive colors based on our Tungi Rejim value!
    val tgPrimaryColor = if (isDarkMode) Color(0xFF18222D) else Color(0xFF5082B1)
    val tgBgColor = if (isDarkMode) Color(0xFF0F1721) else Color(0xFFE7EBF0)
    val tgSurfaceColor = if (isDarkMode) Color(0xFF17212B) else Color(0xFFFFFFFF)
    val tgDividerColor = if (isDarkMode) Color(0xFF111C28) else Color(0xFFE5E5E5)
    val tgTextColor = if (isDarkMode) Color.White else Color.Black
    val tgSubTextColor = if (isDarkMode) Color(0xFF7B8D9F) else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tgBgColor)
    ) {
        if (activeChatUsername == null) {
            // ====================================================================
            // TELEGRAM MAIN VIEW: LISTS AND LIVE SEARCH
            // ====================================================================
            
            // Telegram Header with Search controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tgPrimaryColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Telegram",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Beautiful Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { 
                        Text(
                            text = "Ism yoki @username qidiring (Masalan: rasulov, durov)...", 
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = if (isDarkMode) Color(0xFF24303F) else Color(0x33FFFFFF),
                        unfocusedContainerColor = if (isDarkMode) Color(0xFF1C2735) else Color(0x1BFFFFFF),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Tozalash",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                )
            }

            // Results Container
            val filterResults = remember(searchQuery, databaseUsers) {
                if (searchQuery.isBlank()) {
                    databaseUsers
                } else {
                    databaseUsers.filter {
                        it.fullName.contains(searchQuery, ignoreCase = true) ||
                        it.username.contains(searchQuery, ignoreCase = true) ||
                        it.bio.contains(searchQuery, ignoreCase = true)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tgSurfaceColor)
            ) {
                if (searchQuery.isNotBlank()) {
                    item {
                        Text(
                            text = "GLOBAL QIDIRUV / FOYDALANUVCHILAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color(0xFF64B5F6) else Color(0xFF5082B1),
                            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 6.dp)
                        )
                    }
                }

                if (filterResults.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Hech qanday akkaunt topilmadi 🔍",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = tgSubTextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "O'zingizni yoki 'durov' deb qidirib ko'ring.",
                                fontSize = 12.sp,
                                color = tgSubTextColor.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filterResults) { user ->
                        val isSelfRecord = user.username == selfUsername || user.username == "saved_messages"
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeChatUsername = user.username
                                    searchQuery = "" // reset search on entering chat
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circle with emoji
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelfRecord) Color(0xFF00B0FF)
                                            else if (user.isBot) Color(0xFF78909C)
                                            else Color(0xFFE1306C).copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = user.avatarEmoji, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = tgTextColor
                                        )
                                        if (user.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Verified",
                                                tint = Color(0xFF24A1DE),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        if (isSelfRecord) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFE8F5E9))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("AKKAUNT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(3.dp))
                                    
                                    Text(
                                        text = "@${user.username} • ${user.bio}",
                                        fontSize = 12.sp,
                                        color = tgSubTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = user.status,
                                        fontSize = 10.sp,
                                        color = if (user.status == "online") Color(0xFF4CAF50) else tgSubTextColor
                                    )
                                }
                            }
                            
                            Divider(
                                color = tgDividerColor,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 82.dp)
                            )
                        }
                    }
                }

                // Friendly Tip banner at the bottom
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1F2D3D) else Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Akkaunt qidirish bo'limi 🔍",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = tgTextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "O'zingizning shaxsiy profilingizni qidirish uchun '@$selfUsername' yoki ismi sharifingizni ('$selfName') yozing va shaxsiy 'saved' chatni oching!",
                                fontSize = 11.sp,
                                color = tgSubTextColor,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        } else {
            // ====================================================================
            // TELEGRAM CHAT SCREEN VIEWER
            // ====================================================================
            val currUsername = activeChatUsername!!
            val userDetails = databaseUsers.find { it.username == currUsername } 
                ?: TelegramChatItem(currUsername, "Xabar qutisi", "💬", "Shaxsiy muloqot xonasi", "online")

            val messageList = chatsMessages[currUsername] ?: emptyList()

            // Chat Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tgPrimaryColor)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeChatUsername = null }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to list",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = userDetails.avatarEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userDetails.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        if (userDetails.isVerified) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isTyping) "yozmoqda (typing)..." else userDetails.status,
                        fontSize = 11.sp,
                        color = if (isTyping) Color(0xFFA5D6A7) else Color.White.copy(alpha = 0.75f)
                    )
                }

                IconButton(onClick = {
                    android.widget.Toast.makeText(context, "${userDetails.fullName} ga qo'ng'iroq simulyatsiya qilinmoqda...", android.widget.Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Qo'ng'iroq",
                        tint = Color.White
                    )
                }
            }

            // Message Bubble list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = tgPrimaryColor.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (currUsername == "saved_messages") "Bu shaxsiy saved messages buluti." else "Xabarlashuv xavfsiz shifrlangan tarmoqda amalga oshirilmoqda",
                                    fontSize = 10.sp,
                                    color = tgTextColor.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    items(messageList) { msg ->
                        val isMe = msg.sender == "me"
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 2.dp,
                                    bottomEnd = if (isMe) 2.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) {
                                        if (isDarkMode) Color(0xFF2B5278) else Color(0xFFEFFDDE)
                                    } else {
                                        if (isDarkMode) Color(0xFF182533) else Color(0xFFFFFFFF)
                                    }
                                ),
                                border = if (!isDarkMode && !isMe) BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)) else null,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 14.sp,
                                        color = tgTextColor
                                    )
                                    
                                    Spacer(modifier = Modifier.height(2.dp))
                                    
                                    Row(
                                        modifier = Modifier.align(Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = msg.timestamp,
                                            fontSize = 9.sp,
                                            color = tgSubTextColor.copy(alpha = 0.8f)
                                        )
                                        if (isMe) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = "Read status",
                                                tint = if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF4CAF50),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Row at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tgSurfaceColor)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputMessageText,
                    onValueChange = { inputMessageText = it },
                    placeholder = { Text("Xabar yozing...", color = tgSubTextColor, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = tgTextColor,
                        unfocusedTextColor = tgTextColor,
                        focusedContainerColor = if (isDarkMode) Color(0xFF17212B) else Color(0xFFF1F5F9),
                        unfocusedContainerColor = if (isDarkMode) Color(0xFF17212B) else Color(0xFFF1F5F9),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = false,
                    maxLines = 4
                )

                IconButton(
                    onClick = {
                        if (inputMessageText.isNotBlank()) {
                            val activeText = inputMessageText
                            inputMessageText = ""
                            
                            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val timeStr = sdf.format(Date())
                            
                            // 1. Save user msg
                            val currentList = chatsMessages[currUsername] ?: emptyList()
                            chatsMessages[currUsername] = currentList + TelegramMessage("me", activeText, timeStr)
                            
                            // 2. Schedule Bot reply if applicable
                            if (currUsername != "saved_messages") {
                                isTyping = true
                                coroutineScope.launch {
                                    delay(1200) // typing simulation delay
                                    isTyping = false
                                    val botReplyText = getTelegramBotResponse(currUsername, activeText)
                                    val replyTime = sdf.format(Date())
                                    val updatedList = chatsMessages[currUsername] ?: emptyList()
                                    chatsMessages[currUsername] = updatedList + TelegramMessage(currUsername, botReplyText, replyTime)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (isDarkMode) Color(0xFF527DA3) else Color(0xFF24A1DE), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Yuborish",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Global Help function to return automated responses
fun getTelegramBotResponse(user: String, msg: String): String {
    val q = msg.lowercase().trim()
    return when (user) {
        "durov" -> {
            when {
                "salom" in q || "hello" in q -> "Salom! Men Pavel Durov. Telegram mutlaqo mustaqil, tez va xavfsiz dunyodagi eng zo'r messenjerdir! 🛡️"
                "xavfsiz" in q || "privacy" in q -> "Telegramda shaxsiy daxlsizlik muqaddasdir. Biz foydalanuvchilar maxfiyligini hech kimga sotmaymiz."
                "ton" in q || "pul" in q || "crypto" in q -> "TON kelajak va raqamli erkinlik ramzidir! 💎"
                "rasulov" in q || "abubakir" in q -> "Abubakir haqiqiy usta Android developer ekan. Mana Telegram simulyatorini ham zo'r integratsiya qilibdi! 👍🚀"
                else -> "Menga yozganingiz uchun rahmat. Biz dunyodagi erkin aloqani har doim daxlsiz saqlaymiz!"
            }
        }
        "rasulov_01" -> {
            when {
                "salom" in q || "ishlar" in q -> "Salom do'stim! Profilimni topgach muloqot qilayotganingizdan xursandman. Samsung One UI loyihamiz juda kuchli chiqdi! 💻🎨"
                "yordam" in q -> "Dastur tahlilida savollaringiz bo'lsa xohlagan vaqt yozing, bu yerda Saved Messages kabiga eslatma holda ma'lumot qoldirishingiz mumkin."
                else -> "Ajoyib suhbat uchun rahmat! Tungi rejim va dasturdagi boshqa barcha simulyatsiyalar mukammal darajaga keltirildi. Muvaffaqiyat! 🌐💡"
            }
        }
        "messi_10" -> {
            "¡Hola! Un saludo muy especial para ti. ⚽ Jugar al fútbol me trae alegría, y me complace mucho saber que admiras mi trayectoria deportiva. ¡Un gran abrazo! 🏟️🏆"
        }
        "aistudio_bot" -> {
            when {
                "salom" in q -> "Assalomu alaykum! Men AI Studio Helper virtual botiman. Tizimda Dark Mode (Tungi rejim) va Telegram mukammal ishlamoqda. 🤖"
                else -> "Sizning barcha buyruqlaringiz va kiritgan xabarlaringiz tahlil qilinib saqlanmoqda. Android ilovani ishlatishda davom eting! ⚡"
            }
        }
        else -> "Xabaringiz saqlandi! 📂"
    }
}

data class TelegramMessage(
    val sender: String,
    val text: String,
    val timestamp: String,
    val isRead: Boolean = true
)


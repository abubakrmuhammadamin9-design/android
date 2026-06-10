package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Report
import com.example.data.ReportRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ReportRepository(database.reportDao())

    // ----------------------------------------------------
    // APP NAVIGATION & GENERAL STATES
    // ----------------------------------------------------
    private val _currentTab = MutableStateFlow("launcher") // "launcher", "reports", "playmarket", "settings"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true) // Default to true (Dark Mode) as Samsung user standard, but easily toggleable
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    private val _activeGameScreen = MutableStateFlow<String?>(null)
    val activeGameScreen: StateFlow<String?> = _activeGameScreen.asStateFlow()

    private val _selectedWallpaper = MutableStateFlow("aurora") // "aurora", "sunset", "neon", "ocean", "forest", "darkness"
    val selectedWallpaper: StateFlow<String> = _selectedWallpaper.asStateFlow()

    fun selectWallpaper(wp: String) {
        _selectedWallpaper.value = wp
    }

    private val _recentGames = MutableStateFlow<List<String>>(emptyList())
    val recentGames: StateFlow<List<String>> = _recentGames.asStateFlow()

    fun addRecentGame(gameId: String) {
        val current = _recentGames.value.toMutableList()
        current.remove(gameId)
        current.add(0, gameId)
        _recentGames.value = current.take(6)
    }

    fun selectGameScreen(screen: String?) {
        _activeGameScreen.value = screen
        if (screen != null) {
            addRecentGame(screen)
        }
    }

    private val _isRecentsPanelOpen = MutableStateFlow(false)
    val isRecentsPanelOpen: StateFlow<Boolean> = _isRecentsPanelOpen.asStateFlow()

    fun setRecentsPanelOpen(open: Boolean) {
        _isRecentsPanelOpen.value = open
    }

    private val _customClockTime = MutableStateFlow<String?>(null) // Format "HH:mm", null means use real system clock
    val customClockTime: StateFlow<String?> = _customClockTime.asStateFlow()

    fun updateCustomClockTime(time: String?) {
        _customClockTime.value = time
    }

    private val _isNotificationShadeOpen = MutableStateFlow(false)
    val isNotificationShadeOpen: StateFlow<Boolean> = _isNotificationShadeOpen.asStateFlow()

    fun setNotificationShadeOpen(open: Boolean) {
        _isNotificationShadeOpen.value = open
    }

    // ----------------------------------------------------
    // SIMULATED GOOGLE ACCOUNT SIGN-IN STATE
    // ----------------------------------------------------
    private val _isGoogleSignedIn = MutableStateFlow(false)
    val isGoogleSignedIn = _isGoogleSignedIn.asStateFlow()

    private val _googleAccountEmail = MutableStateFlow("")
    val googleAccountEmail = _googleAccountEmail.asStateFlow()

    private val _googleAccountName = MutableStateFlow("")
    val googleAccountName = _googleAccountName.asStateFlow()

    fun signInWithGoogle(name: String, email: String) {
        viewModelScope.launch {
            _googleAccountName.value = name.ifBlank { "Samsung Foydalanuvchisi" }
            _googleAccountEmail.value = email.ifBlank { "samsung.user@gmail.com" }
            _isGoogleSignedIn.value = true
        }
    }

    fun signOutGoogle() {
        _isGoogleSignedIn.value = false
        _googleAccountEmail.value = ""
        _googleAccountName.value = ""
    }

    // ----------------------------------------------------
    // SAMSUNG DEVICE CARE OPTIMIZATION SYSTEM
    // ----------------------------------------------------
    private val _deviceOptimizationPercent = MutableStateFlow(78)
    val deviceOptimizationPercent = _deviceOptimizationPercent.asStateFlow()

    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing = _isOptimizing.asStateFlow()

    private val _deviceRamStatus = MutableStateFlow("2.8 GB / 8.0 GB Bo'sh")
    val deviceRamStatus = _deviceRamStatus.asStateFlow()

    private val _deviceStorageStatus = MutableStateFlow("112.4 GB / 256.0 GB Bo'sh")
    val deviceStorageStatus = _deviceStorageStatus.asStateFlow()

    fun optimizeDevice() {
        if (_isOptimizing.value) return
        viewModelScope.launch {
            _isOptimizing.value = true
            delay(1500) // Beautiful optimization simulation delay
            _deviceOptimizationPercent.value = 100
            _deviceRamStatus.value = "4.2 GB / 8.0 GB Bo'sh"
            _deviceStorageStatus.value = "114.8 GB / 256.0 GB Bo'sh" // cache cleared
            _isOptimizing.value = false
        }
    }

    // ----------------------------------------------------
    // REPORTS STORAGE AND CRUD
    // ----------------------------------------------------
    val allReports: StateFlow<List<Report>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Barchasi")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredReports: StateFlow<List<Report>> = combine(allReports, _searchText, _selectedCategory) { list, search, category ->
        list.filter { report ->
            val matchesSearch = report.title.contains(search, ignoreCase = true) ||
                    report.description.contains(search, ignoreCase = true)
            val matchesCategory = category == "Barchasi" || report.category == category
            matchesSearch && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun isReportTitleDuplicate(title: String): Boolean {
        return allReports.value.any { it.title.trim().equals(title.trim(), ignoreCase = true) }
    }

    fun addReport(title: String, description: String, category: String, photoPath: String?, rating: Int) {
        viewModelScope.launch {
            val report = Report(
                title = title.ifBlank { "Nomsiz Hisobot" },
                description = description.ifBlank { "Tavsif berilmagan." },
                category = category,
                photoPath = photoPath,
                rating = rating
            )
            repository.insert(report)
        }
    }

    fun deleteReport(report: Report) {
        viewModelScope.launch {
            repository.delete(report)
        }
    }

    // ----------------------------------------------------
    // CAMERA / PICTURE SNAP SIMULATION
    // ----------------------------------------------------
    private val _cameraFlashOn = MutableStateFlow(false)
    val cameraFlashOn = _cameraFlashOn.asStateFlow()

    private val _cameraSceneIndex = MutableStateFlow(0)
    val cameraSceneIndex = _cameraSceneIndex.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing = _isCapturing.asStateFlow()

    fun toggleCameraFlash() {
        _cameraFlashOn.value = !_cameraFlashOn.value
    }

    fun nextCameraScene() {
        _cameraSceneIndex.value = (_cameraSceneIndex.value + 1) % 4
    }

    fun snapPhoto(onSnapped: (String) -> Unit) {
        viewModelScope.launch {
            _isCapturing.value = true
            delay(1000) // Simulate shutter click
            _isCapturing.value = false
            // Preset mock images that fit reports
            val scenes = listOf("hujjat", "analitika", "ish_joyi", "eslatma")
            onSnapped(scenes[_cameraSceneIndex.value])
        }
    }

    // ----------------------------------------------------
    // GAME 1: "TEPAGA CHIQUSH" (CLIMBER GAME)
    // ----------------------------------------------------
    // State of character, scores, and scrolling physics
    private val _climbHighScore = MutableStateFlow(0)
    val climbHighScore = _climbHighScore.asStateFlow()

    private val _climbCurrentScore = MutableStateFlow(0)
    val climbCurrentScore = _climbCurrentScore.asStateFlow()

    private val _climbGameState = MutableStateFlow("START") // "START", "PLAYING", "GAMEOVER"
    val climbGameState = _climbGameState.asStateFlow()

    // Character position & variables
    private val _playerY = MutableStateFlow(0f) // height in game units
    val playerY = _playerY.asStateFlow()

    private val _playerX = MutableStateFlow(150f) // horizontal center position
    val playerX = _playerX.asStateFlow()

    // Platform configurations
    private val _platforms = MutableStateFlow<List<Pair<Float, Float>>>(emptyList()) // list of Pair(X, Y)
    val platforms = _platforms.asStateFlow()

    fun startClimbingGame() {
        _climbCurrentScore.value = 0
        _playerY.value = 0f
        _playerX.value = 150f
        _climbGameState.value = "PLAYING"
        
        // Spawn active starting platforms
        val startPlatforms = mutableListOf<Pair<Float, Float>>()
        var lastY = 120f
        for (i in 1..8) {
            val randomX = Random.nextFloat() * 220f + 40f // random width boundary
            startPlatforms.add(Pair(randomX, lastY))
            lastY += 160f
        }
        _platforms.value = startPlatforms
    }

    fun tapClimbJump(successZoneWidth: Float) {
        if (_climbGameState.value != "PLAYING") return
        
        // When player taps, they jump. We simulate timing difficulty:
        // We verify if player is successfully landing on the next platform or if they score.
        // In simple modern vertical climber tap, each tap advances player up, and can double score if timing is perfect!
        _climbCurrentScore.value += 1
        val newY = _playerY.value + 160f
        _playerY.value = newY
        
        // Spawns more platforms upwards
        val currentList = _platforms.value.toMutableList()
        val highestY = currentList.maxOfOrNull { it.second } ?: 0f
        if (newY > highestY - 400f) {
            val randomX = Random.nextFloat() * 210f + 45f
            currentList.add(Pair(randomX, highestY + 160f))
            if (currentList.size > 14) {
                currentList.removeAt(0)
            }
            _platforms.value = currentList
        }

        // Random chance of falling (e.g. 8% obstacle or slippery platform) to make the mini-game rich & competitive
        if (Random.nextFloat() < 0.08f && _climbCurrentScore.value > 3) {
            triggerClimbGameOver()
        }
    }

    fun triggerClimbGameOver() {
        _climbGameState.value = "GAMEOVER"
        if (_climbCurrentScore.value > _climbHighScore.value) {
            _climbHighScore.value = _climbCurrentScore.value
        }
    }

    // ----------------------------------------------------
    // GAME 2: "XOL VA NOL" (TIC-TAC-TOE)
    // ----------------------------------------------------
    private val _ticTacToeBoard = MutableStateFlow(List(9) { "" })
    val ticTacToeBoard = _ticTacToeBoard.asStateFlow()

    private val _ticTacToeTurn = MutableStateFlow("X") // "X" (User) or "O" (CPU)
    val ticTacToeTurn = _ticTacToeTurn.asStateFlow()

    private val _ticTacToeWinner = MutableStateFlow<String?>(null) // "X", "O", "DURANG" (Draw), or null
    val ticTacToeWinner = _ticTacToeWinner.asStateFlow()

    private val _tttScoreUser = MutableStateFlow(0)
    val tttScoreUser = _tttScoreUser.asStateFlow()

    private val _tttScoreCpu = MutableStateFlow(0)
    val tttScoreCpu = _tttScoreCpu.asStateFlow()

    fun playTicTacToeCell(index: Int) {
        if (_ticTacToeBoard.value[index] != "" || _ticTacToeWinner.value != null || _ticTacToeTurn.value != "X") return

        val currentBoard = _ticTacToeBoard.value.toMutableList()
        currentBoard[index] = "X"
        _ticTacToeBoard.value = currentBoard

        if (checkTttWinner(currentBoard, "X")) {
            _ticTacToeWinner.value = "X"
            _tttScoreUser.value += 1
            return
        }

        if (currentBoard.none { it == "" }) {
            _ticTacToeWinner.value = "DURANG"
            return
        }

        _ticTacToeTurn.value = "O"
        viewModelScope.launch {
            delay(600) // short delay for natural CPU move
            makeCpuTttMove()
        }
    }

    private fun makeCpuTttMove() {
        val currentBoard = _ticTacToeBoard.value.toMutableList()
        val emptyIndexes = currentBoard.indices.filter { currentBoard[it] == "" }
        if (emptyIndexes.isEmpty() || _ticTacToeWinner.value != null) return

        // Simple Smart CPU Move selection
        // 1. Try to win
        var selectedIndex = findWinningTttMove(currentBoard, "O")
        // 2. Try to block player
        if (selectedIndex == -1) {
            selectedIndex = findWinningTttMove(currentBoard, "X")
        }
        // 3. Take random
        if (selectedIndex == -1) {
            selectedIndex = emptyIndexes[Random.nextInt(emptyIndexes.size)]
        }

        currentBoard[selectedIndex] = "O"
        _ticTacToeBoard.value = currentBoard

        if (checkTttWinner(currentBoard, "O")) {
            _ticTacToeWinner.value = "O"
            _tttScoreCpu.value += 1
            return
        }

        if (currentBoard.none { it == "" }) {
            _ticTacToeWinner.value = "DURANG"
            return
        }

        _ticTacToeTurn.value = "X"
    }

    private fun findWinningTttMove(board: List<String>, player: String): Int {
        for (i in board.indices) {
            if (board[i] == "") {
                val tempBoard = board.toMutableList()
                tempBoard[i] = player
                if (checkTttWinner(tempBoard, player)) return i
            }
        }
        return -1
    }

    private fun checkTttWinner(b: List<String>, p: String): Boolean {
        val winCombos = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // lines
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columns
            listOf(0, 4, 8), listOf(2, 4, 6) // diagonals
        )
        return winCombos.any { combo -> combo.all { index -> b[index] == p } }
    }

    fun resetTicTacToe() {
        _ticTacToeBoard.value = List(9) { "" }
        _ticTacToeTurn.value = "X"
        _ticTacToeWinner.value = null
    }

    // ----------------------------------------------------
    // GAME 3: "XOTIRA CHARXI" (MEMORY CARDS)
    // ----------------------------------------------------
    private val cardSymbols = listOf("📊", "📸", "📝", "🚀", "🏆", "🌟", "🛠️", "🎯")
    
    private val _memoryCards = MutableStateFlow<List<MemoryCard>>(emptyList())
    val memoryCards = _memoryCards.asStateFlow()

    private val _memoryMoves = MutableStateFlow(0)
    val memoryMoves = _memoryMoves.asStateFlow()

    private val _memoryScore = MutableStateFlow(0)
    val memoryScore = _memoryScore.asStateFlow()

    private val _memoryGameOver = MutableStateFlow(false)
    val memoryGameOver = _memoryGameOver.asStateFlow()

    private var selectedFirstIndex: Int? = null
    private var selectedSecondIndex: Int? = null
    private var isProcessingMemoryPair = false

    fun startMemoryGame() {
        _memoryMoves.value = 0
        _memoryScore.value = 0
        _memoryGameOver.value = false
        selectedFirstIndex = null
        selectedSecondIndex = null
        isProcessingMemoryPair = false

        // Double items and shuffle
        val doubledSymbols = (cardSymbols + cardSymbols).shuffled()
        _memoryCards.value = doubledSymbols.mapIndexed { index, symbol ->
            MemoryCard(id = index, symbol = symbol)
        }
    }

    fun clickMemoryCard(index: Int) {
        if (isProcessingMemoryPair) return
        val cards = _memoryCards.value
        val card = cards[index]
        if (card.isFlipped || card.isMatched) return

        val updatedCards = cards.toMutableList()
        updatedCards[index] = card.copy(isFlipped = true)
        _memoryCards.value = updatedCards

        if (selectedFirstIndex == null) {
            selectedFirstIndex = index
        } else {
            selectedSecondIndex = index
            _memoryMoves.value += 1
            checkForMemoryMatch()
        }
    }

    private fun checkForMemoryMatch() {
        val idx1 = selectedFirstIndex ?: return
        val idx2 = selectedSecondIndex ?: return
        isProcessingMemoryPair = true

        viewModelScope.launch {
            delay(1000) // let user see card for 1 second
            val cards = _memoryCards.value.toMutableList()
            if (cards[idx1].symbol == cards[idx2].symbol) {
                // Matched!
                cards[idx1] = cards[idx1].copy(isMatched = true)
                cards[idx2] = cards[idx2].copy(isMatched = true)
                _memoryScore.value += 10
            } else {
                // Not Matched, flip back
                cards[idx1] = cards[idx1].copy(isFlipped = false)
                cards[idx2] = cards[idx2].copy(isFlipped = false)
            }
            _memoryCards.value = cards
            selectedFirstIndex = null
            selectedSecondIndex = null
            isProcessingMemoryPair = false

            // check game over
            if (cards.all { it.isMatched }) {
                _memoryGameOver.value = true
            }
        }
    }
}

data class MemoryCard(
    val id: Int,
    val symbol: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

// MainViewModel Factory class
class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

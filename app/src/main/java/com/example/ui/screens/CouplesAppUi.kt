package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CalendarEvent
import com.example.data.model.JournalEntry
import com.example.data.model.PingMessage
import com.example.data.model.PhotoMemory
import com.example.data.security.CoupleEncryption
import com.example.ui.sound.RomanticSounds
import com.example.ui.viewmodel.CoupleViewModel
import java.text.SimpleDateFormat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouplesAppUi(
    viewModel: CoupleViewModel,
    modifier: Modifier = Modifier
) {
    val currentPartner by viewModel.currentPartner
    val selectedThemeName by viewModel.themeMode
    val isE2EEnabled by viewModel.isE2EEnabled

    val journals by viewModel.allJournals.collectAsStateWithLifecycle()
    val pinnedJournals by viewModel.pinnedJournals.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.allEvents.collectAsStateWithLifecycle()
    val pings by viewModel.allPings.collectAsStateWithLifecycle()
    val memoriesDesc by viewModel.allPhotoMemoriesDesc.collectAsStateWithLifecycle()
    val memoriesAsc by viewModel.allPhotoMemoriesAsc.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Timeline") } // "Timeline", "Pings", "Calendar", "Security"
    var showNotification by viewModel.showNotificationBanner

    // Standard date formatting
    val sdf = remember { SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                // Top Header Section
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Heart Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 4.dp)
                            )
                            Text(
                                text = "HeartSync",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        // Switch active partner button
                        FilledTonalButton(
                            onClick = {
                                viewModel.togglePartner()
                                RomanticSounds.playSound("Lovable Purr")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("toggle_partner_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = "Switch Profile",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "I am: $currentPartner",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                // Beautiful Active Banner representing Simulating Partners duplex
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val otherPartnerName = if (currentPartner == "Alex") "Taylor" else "Alex"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Connected with $otherPartnerName (E2E Shield Active 🛡️)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Standard Custom M3 active pill indicator bottom navigation
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "Timeline",
                    onClick = { activeTab = "Timeline" },
                    icon = { Icon(Icons.Outlined.PhotoAlbum, contentDescription = "Timeline") },
                    label = { Text("Journal", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "Memories",
                    onClick = { activeTab = "Memories" },
                    icon = { Icon(Icons.Outlined.PhotoLibrary, contentDescription = "Secure Memories Grid") },
                    label = { Text("Memories", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "Pings",
                    onClick = { activeTab = "Pings" },
                    icon = { Icon(Icons.Outlined.Sms, contentDescription = "Workday Pings") },
                    label = { Text("Check-In", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "Calendar",
                    onClick = { activeTab = "Calendar" },
                    icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = "Shared Calendar") },
                    label = { Text("Calendar", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "Security",
                    onClick = { activeTab = "Security" },
                    icon = { Icon(Icons.Outlined.Security, contentDescription = "Security Keys") },
                    label = { Text("Security", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main views dispatcher
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "ActiveView"
            ) { targetTab ->
                when (targetTab) {
                    "Timeline" -> TimelineTab(
                        viewModel = viewModel,
                        journals = journals,
                        isE2EEnabled = isE2EEnabled,
                        sdf = sdf
                    )
                    "Memories" -> MemoriesTab(
                        viewModel = viewModel,
                        memoriesDesc = memoriesDesc,
                        memoriesAsc = memoriesAsc,
                        isE2EEnabled = isE2EEnabled,
                        currentPartner = currentPartner
                    )
                    "Pings" -> WorkdayPingsTab(
                        viewModel = viewModel,
                        pings = pings,
                        currentPartner = currentPartner,
                        sdf = sdf
                    )
                    "Calendar" -> CalendarTab(
                        viewModel = viewModel,
                        events = calendarEvents,
                        isE2EEnabled = isE2EEnabled,
                        sdf = sdf
                    )
                    "Security" -> SecurityTab(
                        viewModel = viewModel,
                        isE2EEnabled = isE2EEnabled,
                        themeName = selectedThemeName,
                        journals = journals
                    )
                }
            }

            // Real-Time Notification Toast Simulator Banner
            AnimatedVisibility(
                visible = showNotification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
            ) {
                showNotification?.let { msg ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = "Notification Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                maxLines = 3,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { showNotification = null }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close Banner",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Auto-hide the banner after 4.5 seconds
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(4500)
                        if (showNotification == msg) {
                            showNotification = null
                        }
                    }
                }
            }
        }
    }
}

// TIMELINE / JOURNAL SCREEN
@Composable
fun TimelineTab(
    viewModel: CoupleViewModel,
    journals: List<JournalEntry>,
    isE2EEnabled: Boolean,
    sdf: SimpleDateFormat
) {
    val memories by viewModel.allPhotoMemoriesDesc.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    var inputTitle by remember { mutableStateOf("") }
    var inputDesc by remember { mutableStateOf("") }
    var selectedArtwork by remember { mutableStateOf("Picnic Date") }

    val artworkPresets = listOf(
        "Picnic Date",
        "Movie Night",
        "Coffee Talk",
        "Park Walk",
        "Beach Sunset",
        "Sparkling Stars"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Timeline Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Our Private Journal",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Secure milestones & memories securely locked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            // Capture Moment FAB style button
            FilledTonalButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_journal_button")
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Add Journal")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Capture", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Virtual Screen Widget Simulator! It is incredibly clean and fun
        HomeWidgetSimulator(journals = journals, memories = memories, isE2EEnabled = isE2EEnabled, viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Journal List
        if (journals.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = "Empty",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your private timeline is empty.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Tap 'Capture' to secure your first sweet moment together!",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(journals) { entry ->
                    val resolvedTitle = if (isE2EEnabled) {
                        CoupleEncryption.decrypt(entry.title)
                    } else {
                        entry.title // Raw encrypted Base64
                    }

                    val resolvedDesc = if (isE2EEnabled) {
                        CoupleEncryption.decrypt(entry.description)
                    } else {
                        entry.description
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            // Canvas vector Illustration Preset
                            ArtworkIllustrationCanvas(
                                presetName = entry.presetImageName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )

                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (entry.isPinned) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Pin Status",
                                            tint = if (entry.isPinned) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable { viewModel.toggleJournalPin(entry) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = resolvedTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                    }

                                    // Author badge
                                    Surface(
                                        color = if (entry.author == "Alex") MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "By ${entry.author}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (entry.author == "Alex") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = sdf.format(Date(entry.timestamp)),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = resolvedDesc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Security Indicator badge
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isE2EEnabled) Icons.Filled.Lock else Icons.Filled.NoEncryption,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (isE2EEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isE2EEnabled) "Decrypted with Love Key" else "Encrypted SQLite Blob",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isE2EEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                                        )
                                    }

                                    TextButton(
                                        onClick = { viewModel.deleteJournal(entry) },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Capture Moment Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Camera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Secure Beautiful Memory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = inputTitle,
                            onValueChange = { inputTitle = it },
                            label = { Text("What did we do? (Title)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("memory_title_field"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = inputDesc,
                            onValueChange = { inputDesc = it },
                            label = { Text("Write down the sweet story...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("memory_story_field"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Text(
                            text = "Choose Romantic Cover Art Illustration",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(artworkPresets) { artwork ->
                                    val isSelected = selectedArtwork == artwork
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                            )
                                            .clickable { selectedArtwork = artwork }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = artwork,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputTitle.isNotBlank() && inputDesc.isNotBlank()) {
                            viewModel.addJournalEntry(inputTitle, inputDesc, selectedArtwork)
                            inputTitle = ""
                            inputDesc = ""
                            selectedArtwork = "Picnic Date"
                            showAddDialog = false
                            RomanticSounds.playSound("Romantic Flutter")
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Secure (E2E Encrypted)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// BEAUTIFUL CHRONOLOGICAL GRID MEMORIES TAB
@Composable
fun MemoriesTab(
    viewModel: CoupleViewModel,
    memoriesDesc: List<PhotoMemory>,
    memoriesAsc: List<PhotoMemory>,
    isE2EEnabled: Boolean,
    currentPartner: String
) {
    val dateSdf = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
    var searchQuery by remember { mutableStateOf("") }
    var isNewestFirst by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMemoryDetail by remember { mutableStateOf<PhotoMemory?>(null) }

    // Dialog state
    var inputTitle by remember { mutableStateOf("") }
    var inputCaption by remember { mutableStateOf("") }
    var inputLocation by remember { mutableStateOf("") }
    var inputPhotoUrl by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf("Sunset Dream") }

    val presetImages = remember {
        listOf(
            "Sunset Dream" to "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop",
            "Midnight Cafe" to "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=500&auto=format&fit=crop",
            "Rainy Window" to "https://images.unsplash.com/photo-1428908728789-d2de25dbd4e2?w=500&auto=format&fit=crop",
            "Warm Fireplace" to "https://images.unsplash.com/photo-1545048702-79362596cdc9?w=500&auto=format&fit=crop",
            "Starry Night" to "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?w=500&auto=format&fit=crop",
            "Rose Bouquet" to "https://images.unsplash.com/photo-1526047932273-341f2a7631f9?w=500&auto=format&fit=crop",
            "Golden Coast" to "https://images.unsplash.com/photo-1473116763269-25544724c6ce?w=500&auto=format&fit=crop",
            "Misty Mountains" to "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=500&auto=format&fit=crop"
        )
    }

    val memoriesList = if (isNewestFirst) memoriesDesc else memoriesAsc

    // Filter memories
    val filteredMemories = remember(memoriesList, searchQuery, isE2EEnabled) {
        memoriesList.filter { memory ->
            val title = if (isE2EEnabled) CoupleEncryption.decrypt(memory.title) else memory.title
            val location = if (isE2EEnabled) CoupleEncryption.decrypt(memory.location) else memory.location
            val caption = if (isE2EEnabled) CoupleEncryption.decrypt(memory.caption) else memory.caption

            searchQuery.trim().isEmpty() ||
                    title.contains(searchQuery, ignoreCase = true) ||
                    location.contains(searchQuery, ignoreCase = true) ||
                    caption.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Timeline Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Secure Photo Timeline",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "A private safe keeping of our favorite frames",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Add photo
            FilledTonalButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_photo_memory_button"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.AddPhotoAlternate,
                    contentDescription = "Upload Memory"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Capture", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search & Filter controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter memories...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search icon",
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("photo_search_field"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                ),
                singleLine = true
            )

            // Chronological toggle button
            FilledTonalIconButton(
                onClick = {
                    isNewestFirst = !isNewestFirst
                    RomanticSounds.playSound("Romantic Flutter")
                },
                modifier = Modifier
                    .size(52.dp)
                    .testTag("toggle_chronological_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (isNewestFirst) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = "Sort chronological",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active State Indicator Info pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (isE2EEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isE2EEnabled) {
                    "Secure Decryption Active: Decrypting 256-bit DB Records"
                } else {
                    "Private Shield Masked: Displaying Raw Base64 SQLite Signatures"
                },
                fontSize = 10.sp,
                color = (if (isE2EEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)).copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (isNewestFirst) "Newest First" else "Oldest First",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Memories Grid
        if (filteredMemories.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = "No images found",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No memories match \"$searchQuery\"" else "Your private photo grid is empty.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try searching for a different sweet word!" else "Click 'Capture' to snapshot your first beautiful private digital photo keepsake!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredMemories, key = { it.id }) { memory ->
                    val resolvedTitle = if (isE2EEnabled) CoupleEncryption.decrypt(memory.title) else memory.title
                    val resolvedCaption = if (isE2EEnabled) CoupleEncryption.decrypt(memory.caption) else memory.caption
                    val resolvedLocation = if (isE2EEnabled) CoupleEncryption.decrypt(memory.location) else memory.location

                    // Find Photo Source: preset Unsplash URL or Custom pasted URL
                    val matchingPreset = presetImages.firstOrNull { it.first == memory.photoUrl }
                    val resolvedPhotoUrl = matchingPreset?.second ?: memory.photoUrl

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMemoryDetail = memory
                                RomanticSounds.playSound("Romantic Flutter")
                            }
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .testTag("memory_card_${memory.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            // Immersive photo container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                if (resolvedPhotoUrl.isNotEmpty() && resolvedPhotoUrl.startsWith("http")) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(resolvedPhotoUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Memory Frame of $resolvedTitle",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    // Fallback beautiful vector styling if photoUrl parsing fails
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PartyMode,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }

                                // Encrypted overlay tag
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isE2EEnabled) Icons.Filled.LockOpen else Icons.Filled.Lock,
                                            contentDescription = null,
                                            tint = if (isE2EEnabled) Color(0xFF10B981) else Color(0xFFFFD166),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (isE2EEnabled) "Decrypted" else "E2E Secure",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Author indicator
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (memory.author == "Alex") {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                            } else {
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = memory.author,
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Card texts
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = resolvedTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                if (resolvedLocation.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.LocationOn,
                                            contentDescription = "Location",
                                            modifier = Modifier.size(10.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = resolvedLocation,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateSdf.format(Date(memory.timestamp)),
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                        fontWeight = FontWeight.Light
                                    )

                                    // Love Favorite button
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleMemoryFavorite(memory)
                                            RomanticSounds.playSound("Romantic Flutter")
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (memory.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Favorite Memory",
                                            tint = if (memory.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DETAIL VIEW MODAL
    if (selectedMemoryDetail != null) {
        val memory = selectedMemoryDetail!!
        val resolvedTitle = if (isE2EEnabled) CoupleEncryption.decrypt(memory.title) else memory.title
        val resolvedCaption = if (isE2EEnabled) CoupleEncryption.decrypt(memory.caption) else memory.caption
        val resolvedLocation = if (isE2EEnabled) CoupleEncryption.decrypt(memory.location) else memory.location
        val matchingPreset = presetImages.firstOrNull { it.first == memory.photoUrl }
        val resolvedPhotoUrl = matchingPreset?.second ?: memory.photoUrl

        AlertDialog(
            onDismissRequest = { selectedMemoryDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Memory Archive",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Photo layout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (resolvedPhotoUrl.isNotEmpty() && resolvedPhotoUrl.startsWith("http")) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(resolvedPhotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Memory Frame Detail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        // Favorite flag
                        if (memory.isFavorite) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Favorite",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = resolvedTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Face,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Captured by ${memory.author}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Text(
                            text = dateSdf.format(Date(memory.timestamp)),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    if (resolvedLocation.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Location Symbol",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = resolvedLocation,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))

                    Text(
                        text = resolvedCaption,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Database encryption badge representation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isE2EEnabled) Icons.Filled.Security else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (isE2EEnabled) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isE2EEnabled) {
                                "Securely protected by SHA-256 AES keys"
                            } else {
                                "Cipher-Locked SQLite: ${memory.title.take(12)}..."
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isE2EEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { selectedMemoryDetail = null }
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePhotoMemory(memory)
                        selectedMemoryDetail = null
                        RomanticSounds.playSound("Romantic Flutter")
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = "Delete Memory",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Memory")
                }
            }
        )
    }

    // CAPTURE MOMENT/ADD DIALOG
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Camera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Lock Sweet Memory Frame",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = inputTitle,
                            onValueChange = { inputTitle = it },
                            label = { Text("What did we capture? (Title)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("memory_title_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = inputCaption,
                            onValueChange = { inputCaption = it },
                            label = { Text("The story behind this photo...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("memory_caption_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = inputLocation,
                            onValueChange = { inputLocation = it },
                            label = { Text("Where was this? (Location - optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("memory_location_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Text(
                            text = "Select Private Photography Frame Preset",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            val rows = presetImages.chunked(4)
                            rows.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowItems.forEach { preset ->
                                        val isSelected = selectedPreset == preset.first
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedPreset = preset.first }
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                                    shape = RoundedCornerShape(10.dp)
                                                ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(preset.second)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = preset.first,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(32.dp),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                                Text(
                                                    text = preset.first,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(2.dp),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Or paste custom image URL",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = inputPhotoUrl,
                            onValueChange = { inputPhotoUrl = it },
                            placeholder = { Text("https://example.com/photo.jpg") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("memory_photo_url_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputTitle.isNotBlank() && inputCaption.isNotBlank()) {
                            val photoSource = if (inputPhotoUrl.isNotBlank()) inputPhotoUrl else selectedPreset
                            viewModel.addPhotoMemory(
                                title = inputTitle,
                                caption = inputCaption,
                                location = inputLocation,
                                photoUrl = photoSource
                            )
                            // reset dialog states
                            inputTitle = ""
                            inputCaption = ""
                            inputLocation = ""
                            inputPhotoUrl = ""
                            selectedPreset = "Sunset Dream"
                            showAddDialog = false
                            RomanticSounds.playSound("Romantic Flutter")
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Secure E2E Lock 🔒")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Discard")
                }
            }
        )
    }
}

// HOME SCREEN WIDGET SIMULATOR CARD
@Composable
fun HomeWidgetSimulator(
    journals: List<JournalEntry>,
    memories: List<PhotoMemory>,
    isE2EEnabled: Boolean,
    viewModel: CoupleViewModel
) {
    var widgetMode by remember { mutableStateOf("Journal") } // "Journal" or "Photo"

    val latestPinned = journals.firstOrNull { it.isPinned } ?: journals.firstOrNull()
    val title = latestPinned?.let { entry ->
        if (isE2EEnabled) CoupleEncryption.decrypt(entry.title) else entry.title
    } ?: "Our Special Moment"

    val desc = latestPinned?.let { entry ->
        if (isE2EEnabled) CoupleEncryption.decrypt(entry.description) else entry.description
    } ?: "Tap to sync private timeline memories here!"

    val latestFav = memories.firstOrNull { it.isFavorite } ?: memories.firstOrNull()
    val photoTitle = latestFav?.let {
        if (isE2EEnabled) CoupleEncryption.decrypt(it.title) else it.title
    } ?: "No Pinned Memories"
    val photoLocation = latestFav?.let {
        if (isE2EEnabled) CoupleEncryption.decrypt(it.location) else it.location
    } ?: ""
    val photoUrl = latestFav?.let {
        val presets = listOf(
            "Sunset Dream" to "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop",
            "Midnight Cafe" to "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=500&auto=format&fit=crop",
            "Rainy Window" to "https://images.unsplash.com/photo-1428908728789-d2de25dbd4e2?w=500&auto=format&fit=crop",
            "Warm Fireplace" to "https://images.unsplash.com/photo-1545048702-79362596cdc9?w=500&auto=format&fit=crop",
            "Starry Night" to "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?w=500&auto=format&fit=crop",
            "Rose Bouquet" to "https://images.unsplash.com/photo-1526047932273-341f2a7631f9?w=500&auto=format&fit=crop",
            "Golden Coast" to "https://images.unsplash.com/photo-1473116763269-25544724c6ce?w=500&auto=format&fit=crop",
            "Misty Mountains" to "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=500&auto=format&fit=crop"
        )
        presets.firstOrNull { p -> p.first == it.photoUrl }?.second ?: it.photoUrl
    } ?: ""

    val currentThemeName by viewModel.themeMode

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Widgets,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Partner Screen Widget - Live Preview",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "PINNED",
                        fontSize = 8.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tiny Row to toggle modes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Journal", "Photo").forEach { mode ->
                    val isSelected = widgetMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { widgetMode = mode }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (mode == "Journal") Icons.Outlined.Book else Icons.Outlined.Image,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (mode == "Journal") "Pinned Journal" else "Pinned Photo",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real visual layout widget
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = when (currentThemeName) {
                                "Sunset Amber" -> listOf(Color(0xFFEA580C), Color(0xFF7C2D12))
                                "Midnight Sky" -> listOf(Color(0xFF4F46E5), Color(0xFF1E1B4B))
                                "Emerald Garden" -> listOf(Color(0xFF10B981), Color(0xFF064E3B))
                                else -> listOf(Color(0xFFEC4899), Color(0xFF831843)) // Romantic Rose
                            }
                        )
                    )
                    .padding(12.dp)
            ) {
                if (widgetMode == "Journal") {
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "♥ HeartSync Pinned Moment",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Pinned",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = desc,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left photo thumbnail
                        Box(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            if (photoUrl.isNotEmpty() && photoUrl.startsWith("http")) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(photoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Pinned Photo Widget Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Image,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Right metadata
                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = photoTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (photoLocation.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(10.dp),
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = photoLocation,
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = Color.Red
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Pinned Memory",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (widgetMode == "Journal") {
                        "Tap the heart on any journal post below to pin it!"
                    } else {
                        "Mark any memory with a rose heart in the memories tab to pin it!"
                    },
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// VECTOR DRAWING OF THE PRESETS IN THE APP WITH COMPOSE CANVAS (STUNNING AND DEPENDENCY-FREE!)
@Composable
fun ArtworkIllustrationCanvas(presetName: String, modifier: Modifier = Modifier) {
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorSecondary = MaterialTheme.colorScheme.secondary
    val colorTertiary = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Background gradient
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(colorSecondary.copy(alpha = 0.08f), colorPrimary.copy(alpha = 0.15f))
            ),
            size = size
        )

        when (presetName) {
            "Picnic Date" -> {
                // Draw a retro pink-white checkered picnic pattern
                val cellSize = 22.dp.toPx()
                val numRows = (h / cellSize).toInt() + 1
                val numCols = (w / cellSize).toInt() + 1
                for (r in 0..numRows) {
                    for (c in 0..numCols) {
                        if ((r + c) % 2 == 0) {
                            drawRect(
                                color = colorPrimary.copy(alpha = 0.12f),
                                topLeft = Offset(c * cellSize, r * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }
                // Draw a cute red cherry shape in the center
                drawCircle(color = Color(0xFFEF4444), radius = 18.dp.toPx(), center = Offset(w / 2 - 12.dp.toPx(), h / 2 + 10.dp.toPx()))
                drawCircle(color = Color(0xFFEF4444), radius = 18.dp.toPx(), center = Offset(w / 2 + 12.dp.toPx(), h / 2 + 15.dp.toPx()))
                // Stem
                val stemPath = Path().apply {
                    moveTo(w / 2 - 8.dp.toPx(), h / 2 + 10.dp.toPx())
                    quadraticTo(w / 2, h / 2 - 15.dp.toPx(), w / 2 + 10.dp.toPx(), h / 2 - 20.dp.toPx())
                }
                drawPath(path = stemPath, color = Color(0xFF10B981), style = Stroke(width = 3.dp.toPx()))
            }
            "Movie Night" -> {
                // Drawing starry dark slate movie theater screen boundary
                drawRect(color = Color(0xFF1E293B), size = size)
                // Cinema beams
                val beamPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, h)
                    lineTo(w / 2, h)
                    close()
                }
                drawPath(path = beamPath, color = Color(0xFFFDE047).copy(alpha = 0.15f))
                // Popcorn bucket card
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(w / 2 - 20.dp.toPx(), h / 2 - 10.dp.toPx()),
                    size = Size(40.dp.toPx(), 55.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                // Red stripes
                for (i in 0..3) {
                    drawRect(
                        color = Color(0xFFEF4444),
                        topLeft = Offset(w / 2 - 17.dp.toPx() + (i * 10.dp.toPx()), h / 2 - 10.dp.toPx()),
                        size = Size(4.dp.toPx(), 55.dp.toPx())
                    )
                }
                // Yellow fluffy puffs on top
                drawCircle(color = Color(0xFFFACC15), radius = 8.dp.toPx(), center = Offset(w / 2 - 12.dp.toPx(), h / 2 - 12.dp.toPx()))
                drawCircle(color = Color(0xFFFDE047), radius = 10.dp.toPx(), center = Offset(w / 2, h / 2 - 15.dp.toPx()))
                drawCircle(color = Color(0xFFFACC15), radius = 7.dp.toPx(), center = Offset(w / 2 + 14.dp.toPx(), h / 2 - 11.dp.toPx()))
            }
            "Coffee Talk" -> {
                // Cozy wood table background color
                drawRect(color = Color(0xFFFDE047).copy(alpha = 0.08f), size = size)
                // Draw warm table circle
                drawCircle(color = Color(0xFFD97706).copy(alpha = 0.15f), radius = h / 1.3f, center = Offset(w / 2, h))
                // Two coffee mugs
                // Mug left
                drawRoundRect(
                    color = colorSecondary,
                    topLeft = Offset(w / 2 - 45.dp.toPx(), h / 2 - 10.dp.toPx()),
                    size = Size(32.dp.toPx(), 40.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                // Handle left
                drawCircle(
                    color = colorSecondary,
                    radius = 10.dp.toPx(),
                    center = Offset(w / 2 - 48.dp.toPx(), h / 2 + 10.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )

                // Mug right (primary rose/tint)
                drawRoundRect(
                    color = colorPrimary,
                    topLeft = Offset(w / 2 + 13.dp.toPx(), h / 2 - 10.dp.toPx()),
                    size = Size(32.dp.toPx(), 40.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                // Handle right
                drawCircle(
                    color = colorPrimary,
                    radius = 10.dp.toPx(),
                    center = Offset(w / 2 + 48.dp.toPx(), h / 2 + 10.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )

                // Steam lines
                for (dx in listOf(-30.dp, 28.dp)) {
                    val steam = Path().apply {
                        moveTo(w / 2 + dx.toPx(), h / 2 - 18.dp.toPx())
                        quadraticTo(w / 2 + dx.toPx() - 5.dp.toPx(), h / 2 - 28.dp.toPx(), w / 2 + dx.toPx(), h / 2 - 38.dp.toPx())
                    }
                    drawPath(path = steam, color = Color.Gray.copy(alpha = 0.4f), style = Stroke(width = 2.dp.toPx()))
                }
            }
            "Park Walk" -> {
                // Sky background blue
                drawRect(color = Color(0xFFBAE6FD).copy(alpha = 0.3f))
                // Earth/Green ground arc
                drawCircle(color = Color(0xFFBBF7D0), radius = w, center = Offset(w / 2, h + 30.dp.toPx()))
                // Golden sun
                drawCircle(color = Color(0xFFFDE047), radius = 22.dp.toPx(), center = Offset(w - 35.dp.toPx(), 35.dp.toPx()))
                // Cozy romantic swing / bench
                drawRect(
                    color = Color(0xFF78350F),
                    topLeft = Offset(w / 2 - 30.dp.toPx(), h / 2 + 12.dp.toPx()),
                    size = Size(60.dp.toPx(), 6.dp.toPx())
                )
                // Bench legs
                drawRect(color = Color(0xFF78350F), topLeft = Offset(w / 2 - 25.dp.toPx(), h / 2 + 18.dp.toPx()), size = Size(4.dp.toPx(), 18.dp.toPx()))
                drawRect(color = Color(0xFF78350F), topLeft = Offset(w / 2 + 21.dp.toPx(), h / 2 + 18.dp.toPx()), size = Size(4.dp.toPx(), 18.dp.toPx()))
                // Heart floating above bench
                val heartPath = Path().apply {
                    val cx = w / 2
                    val cy = h / 2 - 12.dp.toPx()
                    moveTo(cx, cy)
                    cubicTo(cx - 10.dp.toPx(), cy - 10.dp.toPx(), cx - 15.dp.toPx(), cy + 5.dp.toPx(), cx, cy + 15.dp.toPx())
                    cubicTo(cx + 15.dp.toPx(), cy + 5.dp.toPx(), cx + 10.dp.toPx(), cy - 10.dp.toPx(), cx, cy)
                    close()
                }
                drawPath(path = heartPath, color = Color(0xFFEF4444))
            }
            "Beach Sunset" -> {
                // Ocean sunset radial sunburst
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF97316), Color(0xFFF87171), Color(0xFF38BDF8))
                    )
                )
                // Giant sun sinking on ocean horizon
                drawCircle(color = Color(0xFFFEF08A), radius = 40.dp.toPx(), center = Offset(w / 2, h / 2 + 15.dp.toPx()))
                // Silhouetted couples holding hands (drawn as 2 lovely stick heads with a tiny heart between!)
                drawCircle(color = Color(0xFF451A03), radius = 8.dp.toPx(), center = Offset(w / 2 - 14.dp.toPx(), h / 2 + 25.dp.toPx()))
                drawCircle(color = Color(0xFF451A03), radius = 8.dp.toPx(), center = Offset(w / 2 + 14.dp.toPx(), h / 2 + 25.dp.toPx()))
                // Bodies
                drawRect(color = Color(0xFF451A03), topLeft = Offset(w / 2 - 16.dp.toPx(), h / 2 + 33.dp.toPx()), size = Size(4.dp.toPx(), 20.dp.toPx()))
                drawRect(color = Color(0xFF451A03), topLeft = Offset(w / 2 + 12.dp.toPx(), h / 2 + 33.dp.toPx()), size = Size(4.dp.toPx(), 20.dp.toPx()))
                // Soft golden water reflections
                for (i in 0..4) {
                    val widthRef = 60.dp.toPx() - (i * 8.dp.toPx())
                    drawRoundRect(
                        color = Color(0xFFFEF08A).copy(alpha = 0.7f),
                        topLeft = Offset(w / 2 - (widthRef / 2), h / 2 + 25.dp.toPx() + (i * 8.dp.toPx())),
                        size = Size(widthRef, 3.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                }
            }
            else -> {
                // "Sparkling Stars" (Cosmic romantic sparks)
                drawRect(color = Color(0xFF1E1B4B))
                val random = Random(42)
                for (i in 1..25) {
                    val rx = random.nextFloat() * w
                    val ry = random.nextFloat() * h
                    val sizeStar = random.nextFloat() * 4.dp.toPx() + 1.dp.toPx()
                    drawCircle(color = Color.White.copy(alpha = random.nextFloat()), radius = sizeStar, center = Offset(rx, ry))
                }
                // Draw a giant mystical heart constellation path
                val constPath = Path().apply {
                    val cx = w / 2
                    val cy = h / 2
                    moveTo(cx, cy - 25.dp.toPx())
                    lineTo(cx - 30.dp.toPx(), cy)
                    lineTo(cx, cy + 35.dp.toPx())
                    lineTo(cx + 30.dp.toPx(), cy)
                    close()
                }
                drawPath(path = constPath, color = colorPrimary.copy(alpha = 0.5f), style = Stroke(width = 1.dp.toPx()))
            }
        }
    }
}

// WORKDAY CHECK-IN & MESSAGE PINGS TAB
@Composable
fun WorkdayPingsTab(
    viewModel: CoupleViewModel,
    pings: List<PingMessage>,
    currentPartner: String,
    sdf: SimpleDateFormat
) {
    var rawTextMsg by remember { mutableStateOf("") }
    val otherPartner = if (currentPartner == "Alex") "Taylor" else "Alex"

    val sweetMessagePresets = listOf(
        "Thinking of you! ♥",
        "Coffee check-in! ☕",
        "You've got this! Proud of you. 🌟",
        "Be home soon! 🚗",
        "Miss you incredibly much! ✨",
        "Call me when free, gorgeous! 📞"
    )

    val locationPresets = listOf(
        Triple("Downtown Office 🏢", 40.7128, -74.0060),
        Triple("Local Coffee Shop ☕", 40.7268, -74.0120),
        Triple("Gym / Running Path 🏃‍♂️", 40.7300, -73.9900),
        Triple("Transit / Commuting 🚇", 40.7100, -74.0200)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Workday Check-Ins",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Instantly notify each other without busy overhead",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            IconButton(
                onClick = { viewModel.clearPings() },
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = "Clear all pings",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CHAT TIMELINE SCENE (Messages view)
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (pings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Comment,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No Pings Sent Today",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "Send a pre-set sweet check-in or simulated location alert below. They will immediately play custom romantic sound chimes!",
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = true,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(pings) { ping ->
                            val isMe = ping.senderName == currentPartner
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    modifier = Modifier
                                        .widthIn(max = 280.dp)
                                        .clickable {
                                            // Tap message to replay its custom acoustic tone!
                                            RomanticSounds.playSound(ping.soundPlayed)
                                        },
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 2.dp,
                                        bottomEnd = if (isMe) 2.dp else 16.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Sender Name
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isMe) "Me ($currentPartner)" else ping.senderName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                            )

                                            // Trigger Sound Tag
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.MusicNote,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(10.dp),
                                                    tint = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = ping.soundPlayed,
                                                    fontSize = 8.sp,
                                                    color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Text Message
                                        Text(
                                            text = ping.messageText,
                                            fontSize = 13.sp,
                                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                                        )

                                        // Location tag if applicable
                                        if (ping.pingType == "LOCATION") {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Surface(
                                                color = if (isMe) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Place,
                                                        contentDescription = "Map Marker",
                                                        tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "At: ${ping.locationLabel} (Coordinates: ${ping.locationLatitude}, ${ping.locationLongitude})",
                                                        fontSize = 9.sp,
                                                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }
                                            }
                                        }

                                        // CHECK-IN block with interactive status selection
                                        if (ping.pingType == "CHECK_IN") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                color = if (isMe) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Favorite,
                                                            contentDescription = null,
                                                            tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Text(
                                                            text = "HeartSync Check-In",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    
                                                    if (ping.responseEmoji.isEmpty()) {
                                                        if (isMe) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(8.dp)
                                                                        .clip(CircleShape)
                                                                        .background(Color.White.copy(alpha = 0.8f))
                                                                )
                                                                Text(
                                                                    text = "Waiting for $otherPartner's response...",
                                                                    fontSize = 10.sp,
                                                                    fontStyle = FontStyle.Italic,
                                                                    color = Color.White.copy(alpha = 0.8f)
                                                                )
                                                            }
                                                        } else {
                                                            Text(
                                                                text = "Select your current status emoji:",
                                                                fontSize = 10.sp,
                                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                                modifier = Modifier.padding(bottom = 6.dp)
                                                            )
                                                            
                                                            val emojis = listOf(
                                                                "😊" to "Happy",
                                                                "😴" to "Tired",
                                                                "💖" to "Loved",
                                                                "☕" to "Busy",
                                                                "🍕" to "Hungry",
                                                                "🧠" to "Active"
                                                            )
                                                            
                                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    emojis.take(3).forEach { (emoji, label) ->
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .weight(1f)
                                                                                .clip(RoundedCornerShape(8.dp))
                                                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                                                .clickable {
                                                                                    viewModel.updateResponseEmoji(ping.id, emoji)
                                                                                }
                                                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                                                            contentAlignment = Alignment.Center
                                                                        ) {
                                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                                Text(text = emoji, fontSize = 16.sp)
                                                                                Spacer(modifier = Modifier.width(3.dp))
                                                                                Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    emojis.drop(3).forEach { (emoji, label) ->
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .weight(1f)
                                                                                .clip(RoundedCornerShape(8.dp))
                                                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                                                .clickable {
                                                                                    viewModel.updateResponseEmoji(ping.id, emoji)
                                                                                }
                                                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                                                            contentAlignment = Alignment.Center
                                                                        ) {
                                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                                Text(text = emoji, fontSize = 16.sp)
                                                                                Spacer(modifier = Modifier.width(3.dp))
                                                                                Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(
                                                                    if (isMe) Color.White.copy(alpha = 0.15f)
                                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                                                )
                                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(
                                                                text = ping.responseEmoji,
                                                                fontSize = 24.sp,
                                                                modifier = Modifier.padding(end = 4.dp)
                                                            )
                                                            Column {
                                                                Text(
                                                                    text = if (isMe) "$otherPartner is feeling" else "You responded",
                                                                    fontSize = 9.sp,
                                                                    color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                                )
                                                                Text(
                                                                    text = when(ping.responseEmoji) {
                                                                        "😊" -> "Super Happy & Wonderful! 💕"
                                                                        "😴" -> "A Bit Sleepy & Restless 😴"
                                                                        "💖" -> "Full of Warm Love & Hugs 🥰"
                                                                        "☕" -> "Fully Busy but Thinking of You! ☕"
                                                                        "🍕" -> "Hungry & Craving Snacks! 🍕"
                                                                        "🧠" -> "Productive & Focused Mode! 🧠"
                                                                        else -> "Wonderful! ✨"
                                                                    },
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isMe) Color.White else MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Formatted timestamp
                                        Text(
                                            text = sdf.format(Date(ping.timestamp)),
                                            fontSize = 8.sp,
                                            color = if (isMe) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // PREMIUM CHECK-IN ACTION TRIGGER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable {
                    viewModel.sendPing(
                        messageText = "Hey there! Sending a fast check-in. Tell me how you are doing? 🥰",
                        pingType = "CHECK_IN",
                        sound = "Warm Heartbeat"
                    )
                }
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .testTag("check_in_ping_button"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Check-in icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Send Status Check-In 💓",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ping $otherPartner to select their mood emoji instantly!",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(32.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // INTERACTIVE MESSAGING CONTROLLER TABS
        Text(
            text = "Send Check-In or Simulated Location Alerts instantly to $otherPartner:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Preset Sweet Messages Matrix (Single Tap to Ping with customized romantic synthesizer acoustic tones)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("♥ Sweet Messages", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(sweetMessagePresets) { msg ->
                            Button(
                                onClick = {
                                    // Celestial Chime for standard lovely texts
                                    viewModel.sendPing(
                                        messageText = msg,
                                        pingType = "SWEET_MESSAGE",
                                        sound = "Celestial Chime"
                                    )
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                            ) {
                                Text(
                                    text = msg,
                                    fontSize = 8.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Location Syncing Shortcuts
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("📍 Location Pings", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(locationPresets) { loc ->
                            Button(
                                onClick = {
                                    // Lovable Purr / Special Sound for location arriving notifications
                                    viewModel.sendPing(
                                        messageText = "My simulated location is shared! 📍",
                                        pingType = "LOCATION",
                                        locationLabel = loc.first,
                                        lat = loc.second,
                                        lng = loc.third,
                                        sound = "Lovable Purr"
                                    )
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(
                                    text = "Send Match: ${loc.first}",
                                    fontSize = 7.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // JOKES DEVIATOR & CUSTOM TEXT INPUT ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Trigger Funny Romantic/Cheesy Joke button
            Button(
                onClick = { viewModel.triggerRandomJoke() },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("joke_trigger_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)) // Orange / Yellowish
            ) {
                Icon(Icons.Filled.TheaterComedy, contentDescription = "Romantic Joke Puns")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cheesy Joke 🎙️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Custom Text Input for customized chats
            OutlinedTextField(
                value = rawTextMsg,
                onValueChange = { rawTextMsg = it },
                placeholder = { Text("Or write secret message...", fontSize = 11.sp) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("custom_sms_field"),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (rawTextMsg.isNotBlank()) {
                                viewModel.sendPing(
                                    messageText = rawTextMsg,
                                    pingType = "SWEET_MESSAGE",
                                    sound = "Sparkling Twinkle"
                                )
                                rawTextMsg = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            )
        }
    }
}

// SHARED CALENDAR & TRACKING MILESTONES
@Composable
fun CalendarTab(
    viewModel: CoupleViewModel,
    events: List<CalendarEvent>,
    isE2EEnabled: Boolean,
    sdf: SimpleDateFormat
) {
    var showAddEventDialog by remember { mutableStateOf(false) }

    var eventTitle by remember { mutableStateOf("") }
    var eventNotes by remember { mutableStateOf("") }
    var eventCategory by remember { mutableStateOf("ANNIVERSARY") }
    var eventToneSelection by remember { mutableStateOf("Romantic Flutter") }

    val categoryOptions = listOf(
        "ANNIVERSARY",
        "DATE_NIGHT",
        "MILESTONE",
        "REMINDER"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Our Shared Calendar",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Never miss birthdays, dates, or anniversaries again",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Button(
                onClick = { showAddEventDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_calendar_event_button")
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Add Calendar Event", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Schedule", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Countdown Card to the next date!
        UpcomingCountdownBanner(events = events, isE2EEnabled = isE2EEnabled)

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar milestones lists
        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Empty Events",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your Shared Calendar is empty.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "Schedule custom romantic dates and anniversary reminders!",
                        fontSize = 11.sp,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    val titleDecoded = if (isE2EEnabled) CoupleEncryption.decrypt(event.title) else event.title
                    val descDecoded = if (isE2EEnabled) CoupleEncryption.decrypt(event.description) else event.description

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Icon visual
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when (event.eventType) {
                                            "ANNIVERSARY" -> Color(0xFFFECDD3)
                                            "DATE_NIGHT" -> Color(0xFFFEF3C7)
                                            "MILESTONE" -> Color(0xFFD1FAE5)
                                            else -> Color(0xFFE0E7FF)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (event.eventType) {
                                        "ANNIVERSARY" -> Icons.Filled.Favorite
                                        "DATE_NIGHT" -> Icons.Filled.Restaurant
                                        "MILESTONE" -> Icons.Filled.MilitaryTech
                                        else -> Icons.Filled.EventNote
                                    },
                                    contentDescription = event.eventType,
                                    tint = when (event.eventType) {
                                        "ANNIVERSARY" -> Color(0xFFBE185D)
                                        "DATE_NIGHT" -> Color(0xFFD97706)
                                        "MILESTONE" -> Color(0xFF047857)
                                        else -> Color(0xFF4338CA)
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Main Text
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = titleDecoded,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = descDecoded,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        contentDescription = "Time",
                                        modifier = Modifier.size(12.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = sdf.format(Date(event.dateMillis)),
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Sound indicator & playable hook
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                                        .clickable { RomanticSounds.playSound(event.notificationSound) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VolumeUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Custom Tone: ${event.notificationSound} (Listen 🎧)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            // Delete button
                            IconButton(onClick = { viewModel.deleteCalendarEvent(event) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Event",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Special Date Dialog
    if (showAddEventDialog) {
        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = {
                Text("Schedule Heart Date Reminders", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = eventTitle,
                            onValueChange = { eventTitle = it },
                            label = { Text("Event Reference Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("event_title_field"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = eventNotes,
                            onValueChange = { eventNotes = it },
                            label = { Text("Details & special planning notes...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("event_notes_field"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Text("Category of Event", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            categoryOptions.forEach { opt ->
                                val isChosen = eventCategory == opt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { eventCategory = opt }
                                        .padding( vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt.replace("_", " "),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("Custom Romantic Sound Notification style", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    item {
                        // Sound Picker with playbacks
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RomanticSounds.soundsList.forEach { sound ->
                                val isSelected = eventToneSelection == sound
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable {
                                            eventToneSelection = sound
                                            // Play immediate preview!
                                            RomanticSounds.playSound(sound)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                eventToneSelection = sound
                                                RomanticSounds.playSound(sound)
                                            }
                                        )
                                        Text(text = sound, fontSize = 11.sp)
                                    }

                                    Icon(
                                        imageVector = Icons.Filled.VolumeUp,
                                        contentDescription = "Test sound preview",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (eventTitle.isNotBlank() && eventNotes.isNotBlank()) {
                            // Schedules dates slightly in future in demo (example: 7 days and 3 hours from now)
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, 7)

                            viewModel.addCalendarEvent(
                                title = eventTitle,
                                notes = eventNotes,
                                dateMillis = cal.timeInMillis,
                                type = eventCategory,
                                soundName = eventToneSelection
                            )

                            eventTitle = ""
                            eventNotes = ""
                            eventCategory = "ANNIVERSARY"
                            eventToneSelection = "Romantic Flutter"
                            showAddEventDialog = false
                            RomanticSounds.playSound("Romantic Flutter")
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Secure Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// REDUCING DATE COUNTDOWN BANNER CARD
@Composable
fun UpcomingCountdownBanner(events: List<CalendarEvent>, isE2EEnabled: Boolean) {
    val upcomingEvent = events.minByOrNull { it.dateMillis } ?: return

    val decryptedTitle = if (isE2EEnabled) {
        CoupleEncryption.decrypt(upcomingEvent.title)
    } else {
        upcomingEvent.title
    }

    val daysRemaining = remember(upcomingEvent.dateMillis) {
        val diff = upcomingEvent.dateMillis - System.currentTimeMillis()
        val days = (diff / 86400000L)
        if (days < 0) 0 else days + 1
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "UPCOMING SHARED MILESTONE: ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                Text(
                    text = decryptedTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "A critical private moment secured with your partner.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Large circle with countdown numbers
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$daysRemaining",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = if (daysRemaining == 1L) "DAY" else "DAYS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// SECURITY CENTER & E2E REVIEWS TAB
@Composable
fun SecurityTab(
    viewModel: CoupleViewModel,
    isE2EEnabled: Boolean,
    themeName: String,
    journals: List<JournalEntry>
) {
    var rawInputKey by remember { mutableStateOf(CoupleEncryption.getSharedSecret()) }

    val themeOptions = listOf(
        "Romantic Rose",
        "Sunset Amber",
        "Midnight Sky",
        "Emerald Garden"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Header
        item {
            Column {
                Text(
                    text = "Security Center & Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Full control over your intimate data and application theme",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // 1. E2E Toggle card (Proof visualizer)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isE2EEnabled) Icons.Filled.VerifiedUser else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (isE2EEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Intimacy Protection Status",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Toggle switch to see decrypted vs raw encrypted
                        Switch(
                            checked = isE2EEnabled,
                            onCheckedChange = { viewModel.toggleE2E() },
                            modifier = Modifier.testTag("e2e_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "When E2E is active, your private journal entries and calendar notes are encrypted locally with AES-128 algorithms before storing in Android's SQLite file system. To prove this, toggle E2E off to see how the database looks to unauthorized eyes!",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simulated database visualization box
                    Surface(
                        color = Color.Black.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "RAW SQLITE RECORD PREVIEW:",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            val firstJournal = journals.firstOrNull()
                            val sampleTitle = firstJournal?.title ?: "vH58eH9gqW+1sdX=="
                            val sampleDesc = firstJournal?.description ?: "n2X7v8S1HdwP6A9vG3=="

                            Text(
                                text = "TABLE: journal_entries\n" +
                                        "ID: ${firstJournal?.id ?: 1}\n" +
                                        "TITLE (ENCRYPTED ENVELOPE):\n\"$sampleTitle\"\n" +
                                        "STORY:\n\"$sampleDesc\"",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isE2EEnabled) Color.DarkGray else Color.Red
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (isE2EEnabled) Icons.Filled.LockOpen else Icons.Filled.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isE2EEnabled) Color(0xFF10B981) else Color.Red
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isE2EEnabled) "Decrypted securely using active Love Key" else "LOCKED RAW BLOB SHIELD IS SECURE 🔒",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isE2EEnabled) Color(0xFF10B981) else Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Secret Key Manager
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Shared Secret Key Manager (E2E Key Rotation)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "Your shared secret acts as the unique decryption key. You and your partner must set the exact same love key to sync and decrypt each other's messages!",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = rawInputKey,
                        onValueChange = {
                            rawInputKey = it
                            CoupleEncryption.updateSharedSecret(it)
                        },
                        label = { Text("Active Shared Love Key (AES)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("secret_key_field"),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    // Plays cute secure alert
                                    RomanticSounds.playSound("Sparkling Twinkle")
                                }
                            ) {
                                Icon(Icons.Filled.Key, contentDescription = "Rotate Key")
                            }
                        }
                    )
                }
            }
        }

        // 3. Theme customizer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Select Custom Romantic Theme Visuals",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Choose the aesthetic matching your unique relationship mood:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    themeOptions.forEach { opt ->
                        val isSelected = themeName == opt
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    viewModel.changeTheme(opt)
                                    RomanticSounds.playSound("Lovable Purr")
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Miniature colored preview bulb
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (opt) {
                                                "Sunset Amber" -> Color(0xFFEA580C)
                                                "Midnight Sky" -> Color(0xFF4F46E5)
                                                "Emerald Garden" -> Color(0xFF10B981)
                                                else -> Color(0xFFEC4899)
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = opt,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Active Theme",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

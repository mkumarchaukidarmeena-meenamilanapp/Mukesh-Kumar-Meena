package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Profile
import com.example.ui.GotraResult
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.ProfileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainDashboard()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(viewModel: ProfileViewModel = viewModel()) {
    val currentTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val detailProfile by viewModel.detailProfile.collectAsStateWithLifecycle()
    var showGotraInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "मीणा समाज विवाह",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "शुभ दम्पति मिलन केंद्र ❤️",
                            fontSize = 11.sp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showGotraInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Gotra Rules",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "सत्यापित",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val tabs = listOf(
                    Triple("वर-वधू", Icons.Default.Search, "tab_explore"),
                    Triple("गोत्र मिलान", Icons.Default.Check, "tab_gotra"),
                    Triple("रजिस्ट्रेशन", Icons.Default.Add, "tab_register"),
                    Triple("पसंदीदा", Icons.Default.Favorite, "tab_shortlist")
                )

                tabs.forEachIndexed { index, (label, icon, tag) ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { viewModel.setTab(index) },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.testTag(tag)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> ExploreScreen(viewModel)
                1 -> GotraMatcherScreen(viewModel)
                2 -> RegisterScreen(viewModel)
                3 -> ShortlistScreen(viewModel)
            }

            // Detail view sheet overlay
            detailProfile?.let { profile ->
                DetailDialog(
                    profile = profile,
                    onDismiss = { viewModel.setDetailProfile(null) },
                    onShortlistToggle = { viewModel.toggleShortlist(profile) },
                    onDelete = {
                        viewModel.deleteProfile(profile)
                        viewModel.setDetailProfile(null)
                    }
                )
            }

            // Gotra Info Dialog
            if (showGotraInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showGotraInfoDialog = false },
                    title = {
                        Text(
                            text = "मीणा समाज गोत्र मिलन नियम",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "मीणा समाज एवं हिंदू पारंपरिक विवाहों में 'चार गोत्र' टालने की परंपरा है। विवाह तय करते समय वर एवं वधू के निम्नलिखित कुल प्रणालियों में समानता नहीं होनी चाहिए:",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "1. स्वयं / पिता का गोत्र (Self / Father's Gotra)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "2. माता का गोत्र / ननिहाल (Mother's Gotra / Nihal)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "3. पिता की माता का गोत्र / दादी (Dadi's Gotra)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "4. माता की माता का गोत्र / नानी (Nani's Gotra)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "हमारा गोत्र मिलान कैलकुलेटर इन सभी 16 संयोजन संभावनाओं की जांच करके आपको सचेत करता है।",
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showGotraInfoDialog = false }) {
                            Text("ठीक है (Got It)")
                        }
                    }
                )
            }
        }
    }
}

// Custom painter helper to draw highly impressive gradient avatars natively
@Composable
fun ProfileAvatar(gender: String, avatarId: Int, size: Int = 72) {
    val isGroom = gender.lowercase() == "groom"
    val startColor = if (isGroom) Color(0xFF1E3C72) else Color(0xFFD53369)
    val endColor = if (isGroom) Color(0xFF2A5298) else Color(0xFFDAAE51)

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(startColor, endColor)))
            .border(2.dp, MaterialTheme.colorScheme.tertiary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = gender,
                tint = Color.White,
                modifier = Modifier.size((size * 0.5).dp)
            )
            Text(
                text = if (isGroom) "वर (M)" else "वधू (F)",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = (size * 0.16).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(viewModel: ProfileViewModel) {
    val profiles by viewModel.filteredProfiles.collectAsStateWithLifecycle()
    val genderFilter by viewModel.genderFilter.collectAsStateWithLifecycle()
    val query by viewModel.queryFilter.collectAsStateWithLifecycle()
    val cityFilter by viewModel.cityFilter.collectAsStateWithLifecycle()
    val gotraFilter by viewModel.gotraFilter.collectAsStateWithLifecycle()

    val availableCities by viewModel.filterCities.collectAsStateWithLifecycle()
    val availableGotras by viewModel.filterGotras.collectAsStateWithLifecycle()

    var showFiltersAdv by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcoming & Swastik Traditional Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🪔",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "शुभ दम्पति खोज मंडल",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "मीणा समाज के योग्य भावी जीवनसाथी की सूची। गोत्र देखकर संस्कारी रिश्ते खोजें।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Search and Filter Bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setQueryFilter(it) },
            placeholder = { Text("नाम, गोत्र, व्यवसाय या शहर खोजें...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showFiltersAdv = !showFiltersAdv }) {
                    Icon(
                        imageVector = if (showFiltersAdv) Icons.Default.KeyboardArrowDown else Icons.Default.Search,
                        contentDescription = "Expand Filters",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Gender selectors pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All" to "सभी भावी साथी", "Groom" to "वर (Grooms) 🤵", "Bride" to "वधू (Brides) 👰").forEach { (value, label) ->
                val selected = genderFilter == value
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setGenderFilter(value) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Advanced filter selectors
        AnimatedVisibility(visible = showFiltersAdv) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "फिल्टर परिशोधित करें (Refine Search)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Gotra Filter Dropdown
                        var gotraExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { gotraExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = if (gotraFilter == "All") "सभी गोत्र" else gotraFilter,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(
                                expanded = gotraExpanded,
                                onDismissRequest = { gotraExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("सभी गोत्र", fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.setGotraFilter("All")
                                        gotraExpanded = false
                                    }
                                )
                                availableGotras.forEach { gotra ->
                                    DropdownMenuItem(
                                        text = { Text(gotra, fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.setGotraFilter(gotra)
                                            gotraExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // City Filter Dropdown
                        var cityExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { cityExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = if (cityFilter == "All") "सभी शहर" else cityFilter,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(
                                expanded = cityExpanded,
                                onDismissRequest = { cityExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("सभी शहर", fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.setCityFilter("All")
                                        cityExpanded = false
                                    }
                                )
                                availableCities.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city, fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.setCityFilter(city)
                                            cityExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Clear filter button
                        IconButton(
                            onClick = {
                                viewModel.setQueryFilter("")
                                viewModel.setGotraFilter("All")
                                viewModel.setCityFilter("All")
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear filters",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Profiles count banner
        Text(
            text = "मिलान सूची: ${profiles.size} भावी दम्पति विकल्प मिले",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        // Scrollable profiles list
        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "कोई मिलान नहीं मिला!\n(No profiles match current filters)",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(profiles) { profile ->
                    ProfileCard(
                        profile = profile,
                        onClick = { viewModel.setDetailProfile(profile) },
                        onShortlistToggle = { viewModel.toggleShortlist(profile) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileCard(profile: Profile, onClick: () -> Unit, onShortlistToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("profile_card_${profile.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(gender = profile.gender, avatarId = profile.avatarId, size = 64)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = profile.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "उम्र: ${profile.age} वर्ष | ${profile.height}", fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "स्वयं गोत्र: ${profile.gotraSelf}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = profile.city,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${profile.education} • ${profile.occupation}",
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = onShortlistToggle,
                modifier = Modifier.testTag("shortlist_btn_${profile.id}")
            ) {
                Icon(
                    imageVector = if (profile.isShortlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Shortlist Favorite",
                    tint = if (profile.isShortlisted) Color.Red else Color.Gray
                )
            }
        }
    }
}

// Full screen popup with highly traditional invitation styling
@Composable
fun DetailDialog(
    profile: Profile,
    onDismiss: () -> Unit,
    onShortlistToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp),
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Holy header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚜️ श्री गणेशाय नमः ⚜️",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profile card main presentation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.03f)
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileAvatar(gender = profile.gender, avatarId = profile.avatarId, size = 88)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = profile.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "निवाशी: ${profile.nativePlace}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail sections card under traditional border
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🕉️ जन्म एवं गोत्र विवरण (Birth & Gotras)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow(label = "जन्म तिथि (Birth Date)", value = profile.dateOfBirth)
                        DetailRow(label = "आयु एवं कद (Age & Height)", value = "${profile.age} वर्ष | ${profile.height}")
                        DetailRow(label = "स्वयं गोत्र (Self Gotra)", value = profile.gotraSelf, highlight = true)
                        DetailRow(label = "माता गोत्र (Nihal Gotra)", value = profile.gotraMother)
                        DetailRow(label = "दादी का गोत्र (Dadi's Gotra)", value = profile.gotraDadi)
                        DetailRow(label = "नानी का गोत्र (Nani's Gotra)", value = profile.gotraNani)

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "🎓 शिक्षा एवं व्यवसाय",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow(label = "शैक्षणिक योग्यता", value = profile.education)
                        DetailRow(label = "व्यवसाय/नौकरी", value = profile.occupation, highlight = true)
                        DetailRow(label = "वार्षिक आय (Income)", value = "₹ ${profile.annualIncome} / वर्ष")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "🏡 पारिवारिक परिचय (Family info)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow(label = "पिताजी का नाम", value = profile.fatherName)
                        DetailRow(label = "पिताजी का कार्य", value = profile.fatherOccupation)
                        DetailRow(label = "माताजी का नाम", value = profile.motherName)
                        DetailRow(label = "मूल निवास (Village)", value = profile.nativePlace)

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "📞 संपर्क सूत्र विवरण (Contacts)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow(label = "फोन नंबर", value = profile.phone, highlight = true)
                        DetailRow(label = "ईमेल", value = profile.email)

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "परिचय: ${profile.aboutMe}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions tray inside Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(
                                AnnotatedString(
                                    "✨ मीणा समाज विवाह बायोडाटा ✨\n" +
                                            "नाम: ${profile.name}\n" +
                                            "गोत्र स्वयं: ${profile.gotraSelf}\n" +
                                            "ननिहाल: ${profile.gotraMother}\n" +
                                            "कद एवं उम्र: ${profile.height} | ${profile.age} वर्ष\n" +
                                            "शिक्षा: ${profile.education}\n" +
                                            "नौकरी: ${profile.occupation} | आय: ${profile.annualIncome}\n" +
                                            "पिताजी: ${profile.fatherName}\n" +
                                            "स्मार्टफोन: ${profile.phone}\n" +
                                            "मीणा विवाह एप्प के माध्यम से भेजा गया।"
                                )
                            )
                            Toast.makeText(context, "बायोडाटा क्लिपबोर्ड में कॉपी हुआ!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("बायोडाटा शेयर", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onShortlistToggle,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (profile.isShortlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (profile.isShortlisted) Color.Red else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (profile.isShortlisted) "पसंदीदा हटाएँ" else "पसंदीदा जोड़े", fontSize = 12.sp)
                    }

                    // Delete button if the profile is user-created
                    if (profile.isUserCreated) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Red.copy(alpha = 0.1f))
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Profile", tint = Color.Red)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Normal)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 12.dp)
        )
    }
}

@Composable
fun GotraMatcherScreen(viewModel: ProfileViewModel) {
    val context = LocalContext.current
    val allProfiles by viewModel.allProfilesStream.collectAsStateWithLifecycle()

    val matchGroom by viewModel.gotraMatchGroom.collectAsStateWithLifecycle()
    val matchBride by viewModel.gotraMatchBride.collectAsStateWithLifecycle()

    val selfGroom by viewModel.customGroomSelf.collectAsStateWithLifecycle()
    val motherGroom by viewModel.customGroomMother.collectAsStateWithLifecycle()
    val dadiGroom by viewModel.customGroomDadi.collectAsStateWithLifecycle()
    val naniGroom by viewModel.customGroomNani.collectAsStateWithLifecycle()

    val selfBride by viewModel.customBrideSelf.collectAsStateWithLifecycle()
    val motherBride by viewModel.customBrideMother.collectAsStateWithLifecycle()
    val dadiBride by viewModel.customBrideDadi.collectAsStateWithLifecycle()
    val naniBride by viewModel.customBrideNani.collectAsStateWithLifecycle()

    var showGroomChooser by remember { mutableStateOf(false) }
    var showBrideChooser by remember { mutableStateOf(false) }

    var testTriggered by remember { mutableStateOf(false) }
    var matchResult by remember { mutableStateOf<GotraResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🌟 पारस्परिक गोत्र मिलान कसौटी",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "हिंदू परंपरा अनुसार विवाह बंधन हेतु वर एवं वधु के 4 गोत्रों का आपस में मिलान करना अनिवार्य है। नीचे वर व वधु के गोत्र भरे या सूची में से प्रोफाइल चुनें।",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Profile Quick Selection Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showGroomChooser = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = if (matchGroom != null) "वर: ${matchGroom?.name?.substringBefore(" (")}" else "🤵 वर प्रोफाइल चुनें", fontSize = 12.sp)
            }

            Button(
                onClick = { showBrideChooser = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = if (matchBride != null) "वधू: ${matchBride?.name?.substringBefore(" (")}" else "👰 वधू प्रोफाइल चुनें", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Split inputs side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Groom Inputs (Left)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🤵 वर के गोत्र (Groom's)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                GotraInputField(label = "स्वयं गोत्र (Self)", value = selfGroom, onValueChange = { viewModel.updateCustomGotra(true, "self", it) })
                GotraInputField(label = "ननिहाल गोत्र (Mother)", value = motherGroom, onValueChange = { viewModel.updateCustomGotra(true, "mother", it) })
                GotraInputField(label = "दादी गोत्र (Dadi)", value = dadiGroom, onValueChange = { viewModel.updateCustomGotra(true, "dadi", it) })
                GotraInputField(label = "नानी गोत्र (Nani)", value = naniGroom, onValueChange = { viewModel.updateCustomGotra(true, "nani", it) })
            }

            // Bride Inputs (Right)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "👰 वधू के गोत्र (Bride's)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                GotraInputField(label = "स्वयं गोत्र (Self)", value = selfBride, onValueChange = { viewModel.updateCustomGotra(false, "self", it) })
                GotraInputField(label = "ननिहाल गोत्र (Mother)", value = motherBride, onValueChange = { viewModel.updateCustomGotra(false, "mother", it) })
                GotraInputField(label = "दादी गोत्र (Dadi)", value = dadiBride, onValueChange = { viewModel.updateCustomGotra(false, "dadi", it) })
                GotraInputField(label = "नानी गोत्र (Nani)", value = naniBride, onValueChange = { viewModel.updateCustomGotra(false, "nani", it) })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    testTriggered = true
                    matchResult = viewModel.calculateGotraCompatibility()
                },
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("test_compatibility"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("परीक्षण करें (Check Union)")
            }

            OutlinedButton(
                onClick = {
                    viewModel.clearGotraInputs()
                    testTriggered = false
                    matchResult = null
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("साफ़ करें (Clear)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display results
        if (testTriggered && matchResult != null) {
            val result = matchResult!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.isCompatible) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (result.isCompatible) Color(0xFF4CAF50) else Color(0xFFEF5350)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (result.isCompatible) "✔️" else "⚠️",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (result.isCompatible) "शुभ मिलाप परिणाम (Safe to Marry)" else "असंगत गोत्र चेतावनी (Overlap Conflict)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (result.isCompatible) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = result.message,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )

                    if (result.overlaps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "गोत्र टकराव की सूची (Conflicts):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Red
                        )
                        result.overlaps.forEach { overlap ->
                            Text(
                                text = overlap,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Chooser Dialogs
        if (showGroomChooser) {
            ChooserDialog(
                title = "वर प्रोफाइल सूची",
                profiles = allProfiles.filter { it.gender == "Groom" },
                onDismiss = { showGroomChooser = false },
                onSelect = {
                    viewModel.setGotraMatchGroom(it)
                    showGroomChooser = false
                }
            )
        }

        if (showBrideChooser) {
            ChooserDialog(
                title = "वधू प्रोफाइल सूची",
                profiles = allProfiles.filter { it.gender == "Bride" },
                onDismiss = { showBrideChooser = false },
                onSelect = {
                    viewModel.setGotraMatchBride(it)
                    showBrideChooser = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GotraInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun ChooserDialog(
    title: String,
    profiles: List<Profile>,
    onDismiss: () -> Unit,
    onSelect: (Profile) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxHeight(0.6f)) {
                if (profiles.isEmpty()) {
                    Text("कोई भी प्रोफाइल उपलब्ध नहीं है।")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(profiles) { profile ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(profile) }
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileAvatar(gender = profile.gender, avatarId = profile.avatarId, size = 40)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = profile.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "गोत्र: ${profile.gotraSelf} | माँ: ${profile.gotraMother}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun RegisterScreen(viewModel: ProfileViewModel) {
    val context = LocalContext.current

    // Fields
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Groom") } // Groom or Bride
    var age by remember { mutableStateOf("26") }
    var dob by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("5'7\"") }
    var gotraSelf by remember { mutableStateOf("") }
    var gotraMother by remember { mutableStateOf("") }
    var gotraDadi by remember { mutableStateOf("") }
    var gotraNani by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var nativePlace by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Rajasthan") }
    var fatherName by remember { mutableStateOf("") }
    var fatherOcc by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var aboutMe by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "📝 विवाह पंजीयन फॉर्म (New Biodata Register)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "इस स्थानीय फॉर्म के द्वारा मीणा समाज मिलन केंद्र एप्प में नया बायोडाटा सेव करें। यह प्रोफाइल तत्काल खोज सूची में दिखाई देगी।",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "👤 बुनियादी जानकारी (Basic Info)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("पूरा नाम (Full Name)") }, modifier = Modifier.fillMaxWidth())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("प्रकार (Gender): ", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { gender = "Groom" }) {
                RadioButton(selected = gender == "Groom", onClick = { gender = "Groom" })
                Text("वर (Groom)", fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { gender = "Bride" }) {
                RadioButton(selected = gender == "Bride", onClick = { gender = "Bride" })
                Text("वधू (Bride)", fontSize = 13.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("उम्र (Age)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("कद (Height - e.g. 5'8\")") },
                modifier = Modifier.weight(1.5f)
            )
        }

        OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("जन्म तिथि (DOB - e.g. 12-10-1999)") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        // Gotra Section
        Text(
            text = "🕉️ गोत्र विवरण (Four Gotras Avoiding Rule)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = gotraSelf, onValueChange = { gotraSelf = it }, label = { Text("स्वयं गोत्र (Self)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = gotraMother, onValueChange = { gotraMother = it }, label = { Text("ननिहाल गोत्र") }, modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = gotraDadi, onValueChange = { gotraDadi = it }, label = { Text("दादी गोत्र (Dadi)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = gotraNani, onValueChange = { gotraNani = it }, label = { Text("नानी गोत्र (Nani)") }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Professional Section
        Text(
            text = "🎓 शैक्षणिक व व्यावसायिक विवरण",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

        OutlinedTextField(value = education, onValueChange = { education = it }, label = { Text("शिक्षा (Education - B.Tech, M.A)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = occupation, onValueChange = { occupation = it }, label = { Text("नौकरी / व्यवसाय (Teacher, Engineer)") }, modifier = Modifier.fillMaxWidth())

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = income, onValueChange = { income = it }, label = { Text("वार्षिक आय (Annual Income)") }, modifier = Modifier.weight(1.2f))
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("कार्य शहर (City)") }, modifier = Modifier.weight(1f))
        }

        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("राज्य (State)") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        // Parents info section
        Text(
            text = "🏡 पारिवारिक पृष्ठभूमि (Family Info)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

        OutlinedTextField(value = fatherName, onValueChange = { fatherName = it }, label = { Text("पिता का नाम (Father's Name)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = fatherOcc, onValueChange = { fatherOcc = it }, label = { Text("पिता का व्यवसाय (Father's Occupation)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = motherName, onValueChange = { motherName = it }, label = { Text("माता का नाम (Mother's Name)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nativePlace, onValueChange = { nativePlace = it }, label = { Text("मूल निवास गांव व तहसील (Paternal Village)") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        // Contact info
        Text(
            text = "📞 संपर्क सूत्र (Contact details)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("मोबाइल नंबर (Mobile No.)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("ईमेल पता (Email)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = aboutMe, onValueChange = { aboutMe = it }, label = { Text("बायोडाटा टिप्पणी (Short Bio description)") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isEmpty() || gotraSelf.isEmpty() || phone.isEmpty() || city.isEmpty()) {
                    Toast.makeText(context, "नाम, गोत्र, मोबाइल नंबर  और शहर अनिवार्य हैं।", Toast.LENGTH_LONG).show()
                } else {
                    val ageVal = age.toIntOrNull() ?: 25
                    val randAvatarId = (1..8).random()

                    val newProfile = Profile(
                        name = name,
                        gender = gender,
                        age = ageVal,
                        dateOfBirth = dob.ifEmpty { "N/A" },
                        height = height.ifEmpty { "N/A" },
                        gotraSelf = gotraSelf,
                        gotraMother = gotraMother.ifEmpty { "N/A" },
                        gotraDadi = gotraDadi.ifEmpty { "N/A" },
                        gotraNani = gotraNani.ifEmpty { "N/A" },
                        education = education.ifEmpty { "Graduate" },
                        occupation = occupation.ifEmpty { "Self Employed" },
                        annualIncome = income.ifEmpty { "N/A" },
                        fatherName = fatherName.ifEmpty { "N/A" },
                        fatherOccupation = fatherOcc.ifEmpty { "N/A" },
                        motherName = motherName.ifEmpty { "N/A" },
                        phone = phone,
                        email = email.ifEmpty { "N/A" },
                        aboutMe = aboutMe.ifEmpty { "नया बायोडाटा प्रविष्ट " },
                        city = city,
                        state = state,
                        nativePlace = nativePlace.ifEmpty { city },
                        isUserCreated = true,
                        avatarId = randAvatarId
                    )

                    viewModel.saveProfile(newProfile)
                    Toast.makeText(context, "🎉 बधाई हो! नया बायोडाटा सफलतापूर्वक सहेजा गया और सूची में जुड़ गया है।", Toast.LENGTH_LONG).show()

                    // Reset form fields
                    name = ""
                    dob = ""
                    gotraSelf = ""
                    gotraMother = ""
                    gotraDadi = ""
                    gotraNani = ""
                    education = ""
                    occupation = ""
                    income = ""
                    fatherName = ""
                    fatherOcc = ""
                    motherName = ""
                    nativePlace = ""
                    phone = ""
                    email = ""
                    aboutMe = ""

                    // Switch back to search screen view
                    viewModel.setTab(0)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_biodata"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("रिश्ता सहेजें (Submit Biodata Profile)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
fun ShortlistScreen(viewModel: ProfileViewModel) {
    val shortlisted by viewModel.shortlistedProfilesStream.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💖",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = "पसंदीदा वर-वधू प्रोफाइल सूची",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "आपके द्वारा भविष्य के संपर्क एवं विचार के लिए सहेज कर रखी गई वर-वधू की प्रोफाइल सूची।",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        if (shortlisted.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❤️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "कोई पसंदीदा प्रोफाइल सहेजी नहीं गयी है।\n(Shortlist list is empty)",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(shortlisted) { profile ->
                    ProfileCard(
                        profile = profile,
                        onClick = { viewModel.setDetailProfile(profile) },
                        onShortlistToggle = { viewModel.toggleShortlist(profile) }
                    )
                }
            }
        }
    }
}

package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Profile
import com.example.data.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProfileRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProfileRepository(database.profileDao())
        
        // Check and pre-populate if database is empty on first launch
        viewModelScope.launch {
            repository.checkForPreload()
        }
    }

    // List of common Meena Gotras for UI autocompletes/pickers
    val commonGotras = listOf(
        "जोड़वाल (Jorwal)",
        "मैंडा (Meda)",
        "बाटर (Batar)",
        "मंडिया (Mandia)",
        "सूका (Sukka)",
        "गोमे (Gome)",
        "गोरम (Goram)",
        "चंदेल (Chandel)",
        "सिंगल (Singhal)",
        "मालवी (Malvi)",
        "झारवाल (Jharwal)",
        "चेची (Chechi)",
        "कावड़िया (Kawadiya)",
        "भूरिया (Bhuriya)",
        "चौकीदार (Chaukidar)",
        "जमींदार (Zamindar)"
    )

    // Observables
    val allProfilesStream: StateFlow<List<Profile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortlistedProfilesStream: StateFlow<List<Profile>> = repository.shortlistedProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _selectedTab = MutableStateFlow(0) // 0: Browse, 1: Gotra Matcher, 2: Register/My Biodata, 3: Shortlist
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _detailProfile = MutableStateFlow<Profile?>(null)
    val detailProfile: StateFlow<Profile?> = _detailProfile.asStateFlow()

    // Filter states
    private val _genderFilter = MutableStateFlow("All") // "All", "Groom", "Bride"
    val genderFilter: StateFlow<String> = _genderFilter.asStateFlow()

    private val _queryFilter = MutableStateFlow("")
    val queryFilter: StateFlow<String> = _queryFilter.asStateFlow()

    private val _gotraFilter = MutableStateFlow("All")
    val gotraFilter: StateFlow<String> = _gotraFilter.asStateFlow()

    private val _cityFilter = MutableStateFlow("All")
    val cityFilter: StateFlow<String> = _cityFilter.asStateFlow()

    // Selected profile for Gotra match selection
    private val _gotraMatchGroom = MutableStateFlow<Profile?>(null)
    val gotraMatchGroom: StateFlow<Profile?> = _gotraMatchGroom.asStateFlow()

    private val _gotraMatchBride = MutableStateFlow<Profile?>(null)
    val gotraMatchBride: StateFlow<Profile?> = _gotraMatchBride.asStateFlow()

    // Manual input Gotra Matcher states
    private val _customGroomSelf = MutableStateFlow("")
    val customGroomSelf = _customGroomSelf.asStateFlow()
    private val _customGroomMother = MutableStateFlow("")
    val customGroomMother = _customGroomMother.asStateFlow()
    private val _customGroomDadi = MutableStateFlow("")
    val customGroomDadi = _customGroomDadi.asStateFlow()
    private val _customGroomNani = MutableStateFlow("")
    val customGroomNani = _customGroomNani.asStateFlow()

    private val _customBrideSelf = MutableStateFlow("")
    val customBrideSelf = _customBrideSelf.asStateFlow()
    private val _customBrideMother = MutableStateFlow("")
    val customBrideMother = _customBrideMother.asStateFlow()
    private val _customBrideDadi = MutableStateFlow("")
    val customBrideDadi = _customBrideDadi.asStateFlow()
    private val _customBrideNani = MutableStateFlow("")
    val customBrideNani = _customBrideNani.asStateFlow()

    // Computed filtered profiles
    val filteredProfiles: StateFlow<List<Profile>> = combine(
        allProfilesStream, _genderFilter, _queryFilter, _gotraFilter, _cityFilter
    ) { list, gender, query, gotra, city ->
        list.filter { profile ->
            val matchGender = (gender == "All" || profile.gender == gender)
            val matchQuery = query.isEmpty() || 
                    profile.name.contains(query, ignoreCase = true) ||
                    profile.city.contains(query, ignoreCase = true) ||
                    profile.education.contains(query, ignoreCase = true) ||
                    profile.occupation.contains(query, ignoreCase = true) ||
                    profile.gotraSelf.contains(query, ignoreCase = true)
            val matchGotra = (gotra == "All" || profile.gotraSelf == gotra)
            val matchCity = (city == "All" || profile.city == city)
            
            matchGender && matchQuery && matchGotra && matchCity
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique cities and gotras for filter spinners
    val filterCities: StateFlow<List<String>> = allProfilesStream.map { list ->
        list.map { it.city }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filterGotras: StateFlow<List<String>> = allProfilesStream.map { list ->
        list.map { it.gotraSelf }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setDetailProfile(profile: Profile?) {
        _detailProfile.value = profile
    }

    fun setGenderFilter(gender: String) {
        _genderFilter.value = gender
    }

    fun setQueryFilter(query: String) {
        _queryFilter.value = query
    }

    fun setGotraFilter(gotra: String) {
        _gotraFilter.value = gotra
    }

    fun setCityFilter(city: String) {
        _cityFilter.value = city
    }

    fun toggleShortlist(profile: Profile) {
        viewModelScope.launch {
            repository.toggleShortlist(profile.id, !profile.isShortlisted)
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }

    fun saveProfile(profile: Profile) {
        viewModelScope.launch {
            repository.insertProfile(profile)
        }
    }

    // Gotra Matcher helpers
    fun setGotraMatchGroom(profile: Profile?) {
        _gotraMatchGroom.value = profile
        if (profile != null) {
            _customGroomSelf.value = profile.gotraSelf
            _customGroomMother.value = profile.gotraMother
            _customGroomDadi.value = profile.gotraDadi
            _customGroomNani.value = profile.gotraNani
        }
    }

    fun setGotraMatchBride(profile: Profile?) {
        _gotraMatchBride.value = profile
        if (profile != null) {
            _customBrideSelf.value = profile.gotraSelf
            _customBrideMother.value = profile.gotraMother
            _customBrideDadi.value = profile.gotraDadi
            _customBrideNani.value = profile.gotraNani
        }
    }

    fun updateCustomGotra(isGroom: Boolean, type: String, value: String) {
        if (isGroom) {
            when (type) {
                "self" -> _customGroomSelf.value = value
                "mother" -> _customGroomMother.value = value
                "dadi" -> _customGroomDadi.value = value
                "nani" -> _customGroomNani.value = value
            }
            _gotraMatchGroom.value = null // Discard linked profile model on manual text modification
        } else {
            when (type) {
                "self" -> _customBrideSelf.value = value
                "mother" -> _customBrideMother.value = value
                "dadi" -> _customBrideDadi.value = value
                "nani" -> _customBrideNani.value = value
            }
            _gotraMatchBride.value = null
        }
    }

    fun clearGotraInputs() {
        _gotraMatchGroom.value = null
        _gotraMatchBride.value = null
        _customGroomSelf.value = ""
        _customGroomMother.value = ""
        _customGroomDadi.value = ""
        _customGroomNani.value = ""
        _customBrideSelf.value = ""
        _customBrideMother.value = ""
        _customBrideDadi.value = ""
        _customBrideNani.value = ""
    }

    // Perform community Gotra compatibility logic
    // Returns List of overlaps encountered (empty list means full matching success)
    fun calculateGotraCompatibility(): GotraResult {
        val gs = _customGroomSelf.value.trim()
        val gm = _customGroomMother.value.trim()
        val gd = _customGroomDadi.value.trim()
        val gn = _customGroomNani.value.trim()

        val bs = _customBrideSelf.value.trim()
        val bm = _customBrideMother.value.trim()
        val bd = _customBrideDadi.value.trim()
        val bn = _customBrideNani.value.trim()

        if (gs.isEmpty() || bs.isEmpty()) {
            return GotraResult(isValid = false, message = "कृपया कम से कम स्वयं का गोत्र दर्ज करें। (Please enter at least self gotras)", overlaps = emptyList())
        }

        val conflicts = mutableListOf<String>()

        // Helper to clarify terms in results
        fun normalizeGotra(text: String): String {
            // Remove English translation brackets to check pure Hindi core
            return text.substringBefore(" (").trim()
        }

        val groomGotras = listOf(Pair("वर का स्वयं", gs), Pair("वर का ननिहाल", gm), Pair("वर की दादी", gd), Pair("वर की नानी", gn))
        val brideGotras = listOf(Pair("वधू का स्वयं", bs), Pair("वधू का ननिहाल", bm), Pair("वधू की दादी", bd), Pair("वधू की नानी", bn))

        for (g in groomGotras) {
            if (g.second.isEmpty()) continue
            for (b in brideGotras) {
                if (b.second.isEmpty()) continue
                if (normalizeGotra(g.second).equals(normalizeGotra(b.second), ignoreCase = true)) {
                    conflicts.add("• ⚠️ ${g.first} गोत्र (${g.second}) और ${b.first} गोत्र (${b.second}) समान हैं।")
                }
            }
        }

        return if (conflicts.isEmpty()) {
            GotraResult(
                isValid = true,
                isCompatible = true,
                message = "❤️ बहुत शुभ! गोत्र मिलान पूर्णतः सफल रहा है। विवाह के लिए शुभ दम्पति अनुकूल हैं। \n(Perfect Match! Gotras are fully compatible for sacred union.)",
                overlaps = emptyList()
            )
        } else {
            GotraResult(
                isValid = true,
                isCompatible = false,
                message = "❌ गोत्र समानता पाई गई! हिंदू/मीणा परंपरा अनुसार इस विवाह बंधन से बचा जाना चाहिए। \n(Overlap Detected! Marriage not advised under orthodox gotra avoidance rule.)",
                overlaps = conflicts
            )
        }
    }
}

data class GotraResult(
    val isValid: Boolean,
    val isCompatible: Boolean = false,
    val message: String,
    val overlaps: List<String>
)

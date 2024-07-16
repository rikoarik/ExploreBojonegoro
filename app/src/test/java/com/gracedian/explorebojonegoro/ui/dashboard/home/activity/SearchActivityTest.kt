package com.gracedian.explorebojonegoro.ui.dashboard.home.activity

import android.content.Context
import android.content.Intent
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.SearchAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.SearchItem
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SearchActivityTest {

    private lateinit var searchActivity: SearchActivity

    @Mock
    private lateinit var mockRecyclerView: RecyclerView

    @Mock
    private lateinit var mockSearchAdapter: SearchAdapter

    @Mock
    private lateinit var mockBackButton: ImageView

    @Mock
    private lateinit var mockSearchEditText: EditText

    @Mock
    private lateinit var mockFilterButton: ImageView

    @Mock
    private lateinit var mockResultTextView: TextView

    @Mock
    private lateinit var mockNotFoundImageView: ImageView

    @Mock
    private lateinit var mockIntent: Intent

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        val context = ApplicationProvider.getApplicationContext<Context>()
        FirebaseApp.initializeApp(context)
        searchActivity = Robolectric.buildActivity(SearchActivity::class.java).create().get()
        searchActivity.searchRecyclerView = mockRecyclerView
        searchActivity.searchAdapter = mockSearchAdapter
        searchActivity.backButton = mockBackButton
        searchActivity.searchEditText = mockSearchEditText
        searchActivity.filterButton = mockFilterButton
        searchActivity.resultTextView = mockResultTextView
        searchActivity.notFoundImageView = mockNotFoundImageView
        searchActivity.intent = mockIntent
        ShadowLooper.idleMainLooper()
    }

    @Test
    fun testInitializeViews() {
        searchActivity.initializeViews()
        assertNotNull(searchActivity.backButton)
        assertNotNull(searchActivity.searchEditText)
        assertNotNull(searchActivity.filterButton)
        assertNotNull(searchActivity.resultTextView)
        assertNotNull(searchActivity.searchRecyclerView)
        assertNotNull(searchActivity.notFoundImageView)
    }

    @Test
    fun testFilterSearchResults() {
        val searchItemsList = mutableListOf<SearchItem>()
        searchItemsList.add(SearchItem("image1", "wisata1", "kategori1", 4.0, "alamat1", 10))
        searchItemsList.add(SearchItem("image2", "wisata2", "kategori2", 3.5, "alamat2", 20))
        searchActivity.searchItemsList.addAll(searchItemsList)

        val query = "wisata1"
        searchActivity.filterSearchResults(query)

        verify(mockSearchAdapter).setItems(anyList())
    }

    @Test
    fun testIsCategoryMatch() {
        val searchItem = SearchItem("image", "wisata", "kategori1", 4.0, "alamat", 10)
        searchActivity.appliedCategory = "kategori1"
        assertTrue(searchActivity.isCategoryMatch(searchItem))

        searchActivity.appliedCategory = "kategori2"
        assertFalse(searchActivity.isCategoryMatch(searchItem))
    }

    @Test
    fun testIsRatingMatch() {
        val searchItem = SearchItem("image", "wisata", "kategori", 4.0, "alamat", 10)
        searchActivity.appliedRating = 3.5f
        assertTrue(searchActivity.isRatingMatch(searchItem))

        searchActivity.appliedRating = 4.5f
        assertFalse(searchActivity.isRatingMatch(searchItem))
    }

    @Test
    fun testIsJarakMaxMatch() {
        val searchItem = SearchItem("image", "wisata", "kategori", 4.0, "alamat", 10)
        searchActivity.appliedJarakMax = 15
        assertTrue(searchActivity.isJarakMaxMatch(searchItem))

        searchActivity.appliedJarakMax = 5
        assertFalse(searchActivity.isJarakMaxMatch(searchItem))
    }


}

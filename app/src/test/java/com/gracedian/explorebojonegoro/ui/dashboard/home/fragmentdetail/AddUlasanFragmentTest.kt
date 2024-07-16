package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.item.User
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.UlasanItems
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AddUlasanFragmentTest {

    private lateinit var fragment: AddUlasanFragment

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockDatabaseReference: DatabaseReference


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        val context = ApplicationProvider.getApplicationContext<Context>()
        FirebaseApp.initializeApp(context)

        fragment = AddUlasanFragment()

        fragment.auth = mockFirebaseAuth
        fragment.databaseReference = mockDatabaseReference

        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().start().resume().get()
        activity.supportFragmentManager.beginTransaction().add(fragment, null).commitNow()

        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.fragment_add_ulasan, null, false)

        fragment.namaWisata = view.findViewById(R.id.namaWisata)
        fragment.locWisata = view.findViewById(R.id.locWisata)
        fragment.ratingBar = view.findViewById(R.id.ratingBar)
        fragment.editTextTextMultiLine = view.findViewById(R.id.editTextTextMultiLine)
        fragment.btnApply = view.findViewById(R.id.btnApply)

        fragment.namaWisata.text = "Test Wisata"
        fragment.locWisata.text = "Test Lokasi"
    }

    @Test
    fun testOnCreateView() {
        val inflater = LayoutInflater.from(fragment.requireActivity())
        val container: ViewGroup? = null
        val savedInstanceState: Bundle? = null

        val view = fragment.onCreateView(inflater, container, savedInstanceState)

        assertNotNull(view)
        assertNotNull(fragment.namaWisata)
        assertNotNull(fragment.locWisata)
        assertNotNull(fragment.ratingBar)
        assertNotNull(fragment.editTextTextMultiLine)
        assertNotNull(fragment.btnApply)
    }


    @Test
    fun testBtnApplyClick_withNoCurrentUser() {
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)
        fragment.editTextTextMultiLine.setText("Test Ulasan")
        fragment.ratingBar.rating = 4.5f

        fragment.btnApply.performClick()

        ShadowLooper.idleMainLooper()

        verify(mockDatabaseReference, never()).child("Ulasan")
    }
}

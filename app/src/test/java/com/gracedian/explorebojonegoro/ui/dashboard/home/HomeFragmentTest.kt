package com.gracedian.explorebojonegoro.ui.dashboard.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.PopularAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.WisataTerdekatAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.PopularItem
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.WisataTerdekatItem
import com.gracedian.explorebojonegoro.ui.dashboard.home.response.WeatherResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HomeFragmentTest {

    private lateinit var fragment: HomeFragment
    private lateinit var context: Context

    @Mock
    private lateinit var mockFusedLocationClient: FusedLocationProviderClient

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockDatabase: FirebaseDatabase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        context = RuntimeEnvironment.getApplication().applicationContext
        fragment = HomeFragment()
        fragment.fusedLocationClient = mockFusedLocationClient
        fragment.auth = mockFirebaseAuth
        fragment.database = mockDatabase

        // Inflate the view for the fragment
        val inflater = LayoutInflater.from(context)
        val container: ViewGroup = FrameLayout(context)
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        fragment.onCreateView(inflater, container, null)

    }

    @Test
    fun testCheckPermission_whenPermissionGranted() {
        // Mock permission granted
        `when`(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        fragment.checkPermission()

        // Verify getLocation is called
        verify(mockFusedLocationClient).lastLocation
    }

    @Test
    fun testCheckPermission_whenPermissionNotGranted() {
        // Mock permission not granted
        `when`(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
            .thenReturn(PackageManager.PERMISSION_DENIED)

        fragment.checkPermission()

    }

    @Test
    fun testGetLocation_withLocation() {
        val mockLocation = mock(Location::class.java)
        `when`(mockLocation.latitude).thenReturn(1.0)
        `when`(mockLocation.longitude).thenReturn(1.0)
        `when`(mockFusedLocationClient.lastLocation)
            .thenReturn(com.google.android.gms.tasks.Tasks.forResult(mockLocation))

        fragment.getLocation()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertNotNull(fragment.currentLocation)
        assertEquals(1.0, fragment.currentLocation?.latitude!!, 0.0)
        assertEquals(1.0, fragment.currentLocation?.longitude!!, 0.0)
    }

    @Test
    fun testGetUser() {
        val mockUser = mock(FirebaseAuth::class.java)
        `when`(mockUser.currentUser?.uid).thenReturn("userId")

        fragment.getUser()

        // Verify that user data is fetched from the database
        verify(mockDatabase.reference.child("users").child("userId"))
            .addListenerForSingleValueEvent(any())
    }

    @Test
    fun testGetWeatherData() {
        val latitude = 1.0
        val longitude = 1.0
        val mockWeatherResponse = mock(WeatherResponse::class.java)
        `when`(mockWeatherResponse.main.temp).thenReturn(25.0)
        `when`(mockWeatherResponse.weather[0].description).thenReturn("clear sky")
        `when`(mockWeatherResponse.weather[0].icon).thenReturn("01d")

        fragment.getWeatherData(latitude, longitude)

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertEquals("25 °C", fragment.suhutxt.text)
        assertEquals("clear sky", fragment.ketCuaca.text)
    }

    // Add more tests for other methods as needed
}

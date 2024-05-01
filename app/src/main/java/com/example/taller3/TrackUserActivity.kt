package com.example.taller3

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class TrackUserActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener
{
    private var trackedUid: String? = null
    private var mMap: GoogleMap? = null
    private var location: Location? = null
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private lateinit var myRef: DatabaseReference
    private var user: User? = null
    private lateinit var distanceLbl : TextView
    private lateinit var mapLbl : TextView
    private var polyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_user)
        auth = Firebase.auth
        trackedUid = this.intent.getStringExtra("trackedUid")
        myRef = database.getReference(PATH_USERS.plus(trackedUid))

        mapLbl = findViewById(R.id.mapLbl)
        distanceLbl = findViewById(R.id.distanceTxt)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun requestLocationPermission()
    {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )
    }

    override fun onMapReady(googleMap: GoogleMap)
    {
        mMap = googleMap
        mMap!!.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = true
        mMap!!.uiSettings?.isZoomControlsEnabled = true
        mMap!!.uiSettings?.isZoomGesturesEnabled = true

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }
        mMap!!.isMyLocationEnabled = true
        mMap!!.setOnMyLocationChangeListener { location ->
            this.location = location

            val userLoc = Location("")
            userLoc.latitude = user!!.latitude
            userLoc.longitude = user!!.longitude
            val distance = String.format("%.2f", location.distanceTo(userLoc).toDouble() / 1000)

            distanceLbl.text = "El usuario se encuentra a: $distance km"
            polyline?.remove()

            // Draw a new polyline between the two points
            val polylineOptions = PolylineOptions()
                .add(LatLng(location.latitude, location.longitude))
                .add(LatLng(userLoc.latitude, userLoc.longitude))
                .color(Color.RED)
                .width(5f)
            polyline = mMap?.addPolyline(polylineOptions)

            //updateDistance()
        }

        mMap!!.setMyLocationEnabled(true); // enable location layer of map
        mMap!!.setOnMyLocationButtonClickListener(this);

        // Initialize the user's location and setup listeners
        initUserLocation()
    }

    private fun initUserLocation() {
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Retrieve the object from the dataSnapshot
                user = dataSnapshot.getValue(User::class.java)

                // Save the initial location of the user
                location = Location("")
                location!!.latitude = user!!.latitude
                location!!.longitude = user!!.longitude

                val pos = LatLng(user!!.latitude, user!!.longitude)
                mMap!!.addMarker(MarkerOptions().position(pos).title(user!!.name.plus(" ").plus(user!!.lastName)))


                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(pos))
                mMap!!.moveCamera(CameraUpdateFactory.zoomTo(10F))

                mapLbl.text = mapLbl.text.toString().plus(" ").plus(user!!.name.plus(" ").plus(user!!.lastName))

                // Add listener to user's location changes
                myRef.child("latitude").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newLatitude = snapshot.getValue(Double::class.java)
                        if (newLatitude != null && Math.abs(newLatitude - user!!.latitude) > 0.0001) {
                            user!!.latitude = newLatitude
                            updateDistance()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error retrieving data", error.toException())
                    }
                })

                myRef.child("longitude").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newLongitude = snapshot.getValue(Double::class.java)
                        if (newLongitude != null && Math.abs(newLongitude - user!!.longitude) > 0.0001) {
                            user!!.longitude = newLongitude
                            updateDistance()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error retrieving data", error.toException())
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
                Log.e(TAG, "Error retrieving data", databaseError.toException())
            }
        })
    }

    private fun updateDistance()
    {
        val pos = LatLng(user!!.latitude, user!!.longitude)
        mMap!!.clear()
        mMap!!.addMarker(MarkerOptions().position(pos).title(user!!.name.plus(" ").plus(user!!.lastName)))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(pos))

        location?.let { loc ->
            val userLoc = Location("")
            userLoc.latitude = user!!.latitude
            userLoc.longitude = user!!.longitude
            val distance = String.format("%.2f", loc.distanceTo(userLoc).toDouble() / 1000)

            distanceLbl.text = "El usuario se encuentra a: $distance km"
            polyline?.remove()

            // Draw a new polyline between the two points
            val polylineOptions = PolylineOptions()
                .add(LatLng(loc.latitude, loc.longitude))
                .add(LatLng(userLoc.latitude, userLoc.longitude))
                .color(Color.RED)
                .width(5f)
            polyline = mMap?.addPolyline(polylineOptions)
        }
    }

    override fun onMyLocationButtonClick(): Boolean
    {
        location?.let { loc ->
            val pos = LatLng(loc.latitude, loc.longitude)
            //mMap!!.addMarker(MarkerOptions().position(pos).title("Mi Ubicación"))
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 12f))

        }

        return true
    }

}
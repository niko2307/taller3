package com.example.taller3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException


class MainMenuActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener, AvailableChangeService.AvailableChangeListener {
    private var mMap: GoogleMap? = null
    private lateinit var auth: FirebaseAuth
    private var location: Location? = null
    private val database = FirebaseDatabase.getInstance()
    private lateinit var myRef: DatabaseReference
    private lateinit var changeService: AvailableChangeService

    override fun onRequestPermissionsResult
                (
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode)
        {
            0 -> {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    this.onMapReady(mMap!!)
                }
                else
                {
                    Toast.makeText(this, "Permiso Negado, para utilizar la aplicación se requiere permisos de ubicación", Toast.LENGTH_LONG).show()
                    myRef = database.getReference(PATH_USERS + auth.currentUser!!.uid)
                    myRef.child("available").setValue(false)
                    auth.signOut()
                    val intentLogOut = Intent(this, LogInActivity::class.java)
                    intentLogOut.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    changeService.stopListening()
                    startActivity(intentLogOut)
                }
                return
            }

        }

    }


    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        // Inicialización y configuración de la actividad
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        auth = Firebase.auth
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        myRef = database.getReference(PATH_USERS + auth.currentUser!!.uid)
        myRef.child("available").setValue(true)

        changeService = AvailableChangeService(this)
        changeService.addAvailableChangeListener(this)
        changeService.startListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_principal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        // Handle item selection
        return when (item.itemId)
        {
            R.id.menuLogOut ->
            {
                //myRef = database.getReference(PATH_USERS + auth.currentUser!!.uid)
                myRef.child("available").setValue(false)
                auth.signOut()
                val intentLogOut = Intent(this, LogInActivity::class.java)
                intentLogOut.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                changeService.stopListening()
                startActivity(intentLogOut)
                finish()

                true
            }
            R.id.menuToggleStatus ->
            {
                //myRef = database.getReference(PATH_USERS + auth.currentUser!!.uid)
                myRef.child("available").get().addOnSuccessListener { availableSnapshot ->
                    val isAvailable = availableSnapshot.getValue(Boolean::class.java) ?: false
                    myRef.child("available").setValue(!isAvailable)
                    val statusText = if (!isAvailable) "disponible" else "no disponible"
                    Toast.makeText(this, "Ahora te encuentras $statusText", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.menuAvailableUsers ->
            {
                val intentAvailableUsers = Intent(this, ActiveUsersActivity::class.java)
                changeService.stopListening()
                startActivity(intentAvailableUsers)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
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
        }
        mMap!!.setMyLocationEnabled(true); // enable location layer of map
        mMap!!.setOnMyLocationButtonClickListener(this);

        lateinit var jsonString: String
        try
        {
            jsonString = this.assets.open("locations.json")
                .bufferedReader()
                .use { it.readText() }
        }
        catch (ioException: IOException)
        {
            Log.i("MyApp", ioException.toString())
        }

        val listMarkerType = object : TypeToken<List<KeyMarker>>() {}.type
        val listMarkers : List<KeyMarker> = Gson().fromJson(jsonString, listMarkerType)
        for(marker in listMarkers)
        {
            val pos = LatLng(marker.latitude!!, marker.longitude!!)
            mMap!!.addMarker(MarkerOptions().position(pos).title(marker.name))
        }


        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(listMarkers[0].latitude!!, listMarkers[0].longitude!!)))
        mMap!!.moveCamera(CameraUpdateFactory.zoomTo(10F))

    }



    override fun onBackPressed()
    {
        // do nothing
    }

    override fun onMyLocationButtonClick(): Boolean
    {
        location?.let { loc ->
            val pos = LatLng(loc.latitude, loc.longitude)
            mMap!!.addMarker(MarkerOptions().position(pos).title("Mi Ubicación"))
            myRef = database.getReference(PATH_USERS+auth.currentUser!!.uid)
            myRef.child("latitude").setValue(loc.latitude)
            myRef.child("longitude").setValue(loc.longitude)
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 12f))
        }
        return true
    }

    override fun onAvailableUsersChanged(users: List<User>) {
        if (users.isEmpty()) {
            Toast.makeText(this, "No hay usuarios disponibles", Toast.LENGTH_SHORT).show()
        } else {
            // Aquí podrías actualizar la interfaz de usuario para mostrar los usuarios disponibles
            // Por ejemplo, podrías usar un RecyclerView y un adapter para mostrar la lista de usuarios
        }
    }


}
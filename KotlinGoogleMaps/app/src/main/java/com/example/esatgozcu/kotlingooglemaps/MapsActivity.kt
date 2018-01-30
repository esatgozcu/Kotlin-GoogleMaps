package com.example.esatgozcu.kotlingooglemaps

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContentResolverCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var locationManager:LocationManager?=null
    var locationListener:LocationListener?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(myListener)

        // Konum tespit özelliklerini kullanabilmek için LocationManager oluşturuyoruz.
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Konum değişikliği olduğu zaman..
        locationListener = object : LocationListener{
            override fun onLocationChanged(location: Location?) {

                // Koordinatların olduğu yeri haritada yakınlaştırıyoruz.
                val userLocation = LatLng(location!!.latitude,location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))

            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }
            override fun onProviderEnabled(provider: String?) {
            }
            override fun onProviderDisabled(provider: String?) {
            }
        }
        // İzin verilmemiş ise izin istiyoruz.
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        }
        // İzin verilmiş ise..
        else
        {
            // Konumu güncelliyoruz
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)

            val intent = intent
            val info = intent.getStringExtra("info")
            // Eğer yeni konum eklenecekse..
            if(info.equals("new"))
            {
                // Harita ilk açıldığında kullanıcının en son bilinen yerine gidiyoruz.
                mMap.clear()
                val lastLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation!=null)
                {
                    val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,16f))
                }
            }
            // Kayıtlı bir yer gösterileceği zaman..
            else{

                mMap.clear()
                // MainActivity sayfasında gelen koordinatları haritada gösteriyoruz.
                val latitude = intent.getDoubleExtra("latitude",0.0)
                val longitude = intent.getDoubleExtra("longitude",0.0)
                val name = intent.getStringExtra("name")
                val location = LatLng(latitude,longitude)
                mMap.addMarker(MarkerOptions().position(location).title(name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,16.0f))
            }
        }
    }
    // İzin isteme sonucunu değerlendiriyoruz.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        // Eğer izin isteme sonucunda bir sonuç varsa..
        if (grantResults.size>0)
        {
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                // Konumu güncelliyoruz.
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    // Haritanın üzerine uzun basıldığı zaman..
    val myListener = object : GoogleMap.OnMapLongClickListener{
        override fun onMapLongClick(p0: LatLng?) {

            // Koordinat bilgisine göre adres bulmak için geocoder oluşturuyoruz.
            val geocoder = Geocoder(applicationContext, Locale.getDefault())
            var address = ""

            // Adres bilgisini çekiyoruz.
            try{
                val addressList = geocoder.getFromLocation(p0!!.latitude,p0.longitude,1)
                if (addressList!=null && addressList.size>0)
                {
                    if (addressList[0].thoroughfare!=null)
                    {
                        address+=addressList[0].thoroughfare

                        if (addressList[0].subThoroughfare!=null)
                        {
                            address+=addressList[0].subThoroughfare
                        }
                    }
                }
                else
                {
                    // Koordinatlar üzerindeki konum bulunamıyorsa..
                    address="Yeni Yer"
                }
            }
            catch (e:Exception)
            {
                e.printStackTrace()
            }

            // Dokunduğumuz yere Marker ekliyoruz ve başlığında Geocoder'dan gelen adresi ekliyoruz.
            mMap.addMarker(MarkerOptions().position(p0!!).title(address))

            Toast.makeText(applicationContext,"Yeni Yer Oluşturuldu",Toast.LENGTH_LONG).show()

            try{

                // Üzerine uzun basılı tutulan yerin koordinatlarını ve adres bilgisini veritabanına kayıt ediyoruz.
                val latitude = p0.latitude.toString()
                val longitude=p0.longitude.toString()

                val database = openOrCreateDatabase("Yerler",Context.MODE_PRIVATE,null)

                database.execSQL("CREATE TABLE IF NOT EXISTS yerler (name VARCHAR, latitude VARCHAR, longitude VARCHAR)")

                val toCompile = "INSERT INTO yerler (name,latitude,longitude) VALUES (?,?,?)"

                val sqLiteStatement = database.compileStatement(toCompile)

                sqLiteStatement.bindString(1,address)
                sqLiteStatement.bindString(2,latitude)
                sqLiteStatement.bindString(3,longitude)

                sqLiteStatement.execute()

            }catch (e:Exception)
            {
                e.printStackTrace()
            }
        }
    }
}

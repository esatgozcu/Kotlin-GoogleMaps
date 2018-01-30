package com.example.esatgozcu.kotlingooglemaps

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.ArrayAdapter
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    // Verilerimizi aktaracağımı dizilerimizi oluşturuyoruz.
    val namesArray = ArrayList<String>()
    val locationArray = ArrayList<LatLng>()

    // Harita sayfasından geri gelindiği zaman..
    override fun onResume() {
        // Harita sayfasında gelince tekrarda dizileri güncelliyoruz.
        getData()
        super.onResume()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ListView'de itemlere tıklanıldığı zaman..
        listView.setOnItemClickListener { parent, view, position, id ->

            // MapsActivity sayfasına geçiş yapıyoruz
            val intent = Intent(applicationContext,MapsActivity::class.java)
            // Eski bir yer gösterileceğini belirtiyoruz.
            intent.putExtra("info","old")
            // MapsActivity sayfasına koordinatları ve adresi aktarıyoruz.
            intent.putExtra("name",namesArray[position])
            intent.putExtra("latitude",locationArray[position].latitude)
            intent.putExtra("longitude",locationArray[position].longitude)
            startActivity(intent)
        }
        getData()
    }
    // Veritabanından verileri çekip dizilere aktarıyoruz.
    fun getData()
    {
        try {

            // Veritabanı açma veya yok ise oluşturma
            val database = openOrCreateDatabase("Yerler",Context.MODE_PRIVATE,null)
            // Tablo oluşturma
            val cursor = database.rawQuery("SELECT * FROM yerler",null)

            if (cursor != null)
            {
                // Verileri çekebilmek için index oluşturuyoruz
                val nameIndex= cursor.getColumnIndex("name")
                val latitudeIndex =cursor.getColumnIndex("latitude")
                val longitudeIndex= cursor.getColumnIndex("longitude")

                // Her seferinde üstüne yazmaması için dizileri temizliyoruz.
                namesArray.clear()
                locationArray.clear()

                // Bir sonraki satıra geçiyoruz.
                cursor.moveToFirst()

                // İlk satırdan itibaren verileri çekiyoruz ve dizilere aktarıyoruz.
                while (cursor!=null)
                {
                    val nameData = cursor.getString(nameIndex)
                    val latitudeData = cursor.getString(latitudeIndex)
                    val longitudeData = cursor.getString(longitudeIndex)

                    namesArray.add(nameData)

                    val latitudeCoordinate = latitudeData.toDouble()
                    val longitudeCoordinate = longitudeData.toDouble()

                    val location = LatLng(latitudeCoordinate,longitudeCoordinate)

                    locationArray.add(location)

                    // Bir sonraki satıra geçiyoruz.
                    cursor.moveToNext()
                }
            }

        }catch (e:Exception)

        {
            e.printStackTrace()
        }
        // Dizimizi ArrayAdaptere aktarıp listView ile bağlıyoruz.
        val arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,namesArray)
        listView.adapter = arrayAdapter
    }
    // Ekle tuşuna basıldığı zaman
    fun add(view: View)
    {
        // MapsActivity sayfasına geçiş yapıyoruz.
        val intent = Intent(applicationContext,MapsActivity::class.java)
        // Yeni konum ekleneceğiniz belirtiyoruz.
        intent.putExtra("info","new")
        startActivity(intent)
    }
}

package com.example.epicture

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.epicture.ui.gallery.GalleryFragment
import org.json.JSONArray

class gallery : AppCompatActivity() {
    var images: Any? = null
    var accessToken: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_activity)
        images = JSONArray(intent.getStringExtra("images"))
        accessToken = intent.getStringExtra("access_token")
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, GalleryFragment.newInstance())
                .commitNow()
        }

    }

}

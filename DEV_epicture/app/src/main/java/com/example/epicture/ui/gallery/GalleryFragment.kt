package com.example.epicture.ui.gallery

import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.example.epicture.R
import com.example.epicture.gallery
import com.example.epicture.ui.home.VolleySingleton
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI



class GalleryFragment : Fragment() {
    private lateinit var myParent: gallery

    companion object {
        fun newInstance() = GalleryFragment()
    }

    private lateinit var viewModel: GalleryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.gallery_fragment, container, false)
        myParent = context as gallery
        val images = myParent.images as JSONArray
        val gallery = root.findViewById<LinearLayout>(R.id.myGallery)
        println(images)
        println(myParent.accessToken)
        for (i in 0 until images.length()) {
            val jsonSubInner: JSONObject = images.getJSONObject(i)
            var url = jsonSubInner.get("link").toString()
            val type = jsonSubInner.get("type").toString()
            val views = jsonSubInner.get("views").toString()
            if (type.contains("video")) {
                val container = layoutInflater.inflate(R.layout.video_container, null)
                val video : VideoView = container.findViewById(R.id.videoView)

                video.setVideoPath(url)
                video.setOnPreparedListener {
                    it.isLooping = true
                }
                video.start()
                val btnLike: RadioButton = container.findViewById(R.id.likeVideo)
                getFav(jsonSubInner.get("id").toString(), btnLike)
                btnLike.setOnClickListener {
                    favoriteImage(jsonSubInner.get("id").toString())
                    if (btnLike.tag == "unlike") {
                        btnLike.tag = "like"
                        btnLike.setButtonDrawable(R.drawable.ic_heart_full)
                    } else {
                        btnLike.tag = "unlike"
                        btnLike.setButtonDrawable(R.drawable.ic_heart_empty)
                    }
                }
                val view : RadioButton = container.findViewById(R.id.viewVideo)
                view.text = views
                gallery?.addView(container)
            } else {
                val container = layoutInflater.inflate(R.layout.image_container, null)

                val image = container.findViewById<ImageView>(R.id.imageView)
                activity?.let {
                    Glide.with(it)
                        .load(url)
                        .into(image)
                }
                val btnLike: RadioButton = container.findViewById(R.id.likeImage)
                getFav(jsonSubInner.get("id").toString(), btnLike)
                btnLike.setOnClickListener {
                    favoriteImage(jsonSubInner.get("id").toString())
                    if (btnLike.tag == "unlike") {
                        btnLike.tag = "like"
                        btnLike.setButtonDrawable(R.drawable.ic_heart_full)
                    } else {
                        btnLike.tag = "unlike"
                        btnLike.setButtonDrawable(R.drawable.ic_heart_empty)
                    }
                }
                val view : RadioButton = container.findViewById(R.id.viewImage)
                view.text = views
                gallery?.addView(container)
            }
        }
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GalleryViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun getFav(id: String, btnLike: RadioButton) {
        println("access token : " + myParent.accessToken)
        if (myParent.accessToken == null || myParent.accessToken == "null") {
            return
        }
        val url = "https://api.imgur.com/3/image/$id"

        println("Starting get image")
        val request = object : StringRequest(
            Method.GET, url,
            Response.Listener<String> { response ->
                var strResp = response.toString()
                val jsonObj = JSONObject(strResp)
                val data: JSONObject = jsonObj.getJSONObject("data")
                val favorite = data.get("favorite").toString()
                println ("set favorite : $favorite")
                if (favorite == "true") {
                    btnLike.tag = "like"
                    btnLike.setButtonDrawable(R.drawable.ic_heart_full)
                }
                btnLike.text = (data.get("width").toString().toInt() / 10).toString()

            }, Response.ErrorListener{
                println("Volley error: $it")
                // Error in request
            })
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + myParent.accessToken
                return headers
            }
        }
        this.context?.let { it1 -> VolleySingleton.getInstance(it1).addToRequestQueue(request) }
    }

    private fun favoriteImage(imageId: String) {
        println("image id : $imageId")
        if (myParent.accessToken == null || myParent.accessToken == "null") {
            return
        }
        val url = "https://api.imgur.com/3/image/$imageId/favorite"
        val params = HashMap<String,String>()

        val jsonObject = JSONObject(params as Map<*, *>)
        println(jsonObject.toString())

        println("Starting Upload")
        val request = object: JsonObjectRequest(
            Method.POST,url,jsonObject,
            Response.Listener { response ->}, Response.ErrorListener{
                println("Volley error: $it")
                // Error in request
            })
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + myParent.accessToken
                return headers
            }
        }
        this.context?.let { it1 -> VolleySingleton.getInstance(it1).addToRequestQueue(request) }
    }
}

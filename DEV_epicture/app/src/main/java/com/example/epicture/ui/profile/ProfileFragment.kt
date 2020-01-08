package com.example.epicture.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.epicture.MainActivity
import com.example.epicture.R
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.epicture.Login
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.profile.*
import org.json.JSONArray
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var myParent : MainActivity
    private lateinit var dimLayout : RelativeLayout
    var accessToken : String? = ""
    var refreshToken : String? = ""
    var username : String? = ""
    var accountId : String? = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myParent = context as MainActivity
        accessToken = myParent.accessToken
        refreshToken = myParent.refreshToken
        username = myParent.username
        accountId = myParent.accountId

        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        val include1 : View = root.findViewById(R.id.displayProfile)
        val include2 : View = root.findViewById(R.id.displayLogin)

        if (accessToken == null || accessToken == "null") {
            include1.visibility = View.INVISIBLE
            include2.visibility = View.VISIBLE
            val btnLogin: Button = root.findViewById(R.id.buttonNotLogin)
            btnLogin.setOnClickListener {
                val intent = Intent(activity, Login::class.java)
                startActivity(intent)
            }
        } else {
            dimLayout = myParent.findViewById(R.id.dim_layout)
            getProfilInfo(root)
            getProfileAlbum(root, inflater)

            include1.visibility = View.VISIBLE
            include2.visibility = View.INVISIBLE
            val textView: TextView = root.findViewById(R.id.text_profile)
            profileViewModel.text.observe(this, Observer {
                textView.text = username
            })
        }
        return root
    }

    private fun getProfileAlbum(root: View, inflater: LayoutInflater) {
// Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(activity)
        val url = "https://api.imgur.com/3/account/me/images"


        // Request a string response from the provided URL.
        val stringReq = object : StringRequest(
            Method.GET, url,
            Response.Listener<String> { response ->

                var strResp = response.toString()
                val jsonObj = JSONObject(strResp)
                val jsonArray: JSONArray = jsonObj.getJSONArray("data")
                val tabLayout = root.findViewById<TableLayout>(R.id.tableProfile)
                var tr = inflater.inflate(R.layout.table_row_layout, null)
                for (i in 0 until jsonArray.length()) {
                    var image: JSONObject = jsonArray.getJSONObject(i)
                    try {
                        val imageV : ImageView
                        if (i % 3 == 0) {
                            imageV = tr.findViewById(R.id.image1)
                        } else if (i % 3 == 1) {
                            imageV = tr.findViewById(R.id.image2)
                        } else {
                            imageV = tr.findViewById(R.id.image3)
                        }
                        activity?.let {
                            Glide.with(it)
                                .load(image.get("link").toString())
                                .into(imageV)
                        }
                        imageV.setOnClickListener {
                            openPopUp(root, image.get("link").toString())
                        }
                        if (i % 3 == 2 || i + 1 == jsonArray.length()) {
                            tabLayout.addView(tr)
                            tr = inflater.inflate(R.layout.table_row_layout, null)
                        }

                    } catch (e: org.json.JSONException) {

                    }
                }
            },
            Response.ErrorListener {println("That didn't work!")})

        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer " + myParent.accessToken
                return headers
            }
        }
        queue.add(stringReq)
    }

    private fun getProfilInfo(root: View) {

        val textBio: TextView = root.findViewById(R.id.text_bio)
        val avatar: ImageView = root.findViewById(R.id.avatar)
        val cover: ImageView = root.findViewById(R.id.cover)

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(activity)
        val url = "https://api.imgur.com/3/account/$username"

        // Request a string response from the provided URL.
        val stringReq = object : StringRequest(
            Method.GET, url,
            Response.Listener<String> { response ->

                var strResp = response.toString()
                val jsonObj = JSONObject(strResp)
                val data: JSONObject = jsonObj.get("data") as JSONObject
                try {
                    textBio.text = data.get("bio").toString()
                    activity?.let {
                        Glide.with(it)
                            .load(data.get("avatar").toString())
                            .into(avatar)
                    }
                    activity?.let {
                        Glide.with(it)
                            .load(data.get("cover").toString())
                            .into(cover)
                    }
                } catch (e: org.json.JSONException) {

                }

            },
            Response.ErrorListener {println("That didn't work!")})

        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Client-ID c285652aaf34652"
                return headers
            }
        }
        queue.add(stringReq)
    }

    private fun openPopUp(root: View, uri: String) {
        // Initialize a new layout inflater instance
        val inflater:LayoutInflater = this.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate a custom view using layout inflater
        val popUp = inflater.inflate(R.layout.simple_pic, null)

        //popUp.setBackgroundColor(resources.getColor(R.color.colorPrimaryLight))

        val image : ImageView = popUp.findViewById(R.id.simplePic)
        activity?.let {
            Glide.with(it)
                .load(uri)
                .into(image)
        }


        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            popUp, // Custom view to show in popup window
            ConstraintLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            ConstraintLayout.LayoutParams.WRAP_CONTENT// Window height
        )
        //val buttonPopup = popUp.findViewById<ImageButton>(R.id.closePic)
        image.setOnClickListener{
            popupWindow.dismiss()
        }
        popupWindow.isFocusable = true
        popupWindow.elevation = 10.0F

        //val choosePic = popUp.findViewById<ImageView>(R.id.choosePic)
        //choosePic.setImageURI()

        // Create a new slide animation for popup window enter transition
        val slideIn = Slide()
        slideIn.slideEdge = Gravity.TOP
        popupWindow.enterTransition = slideIn

        // Slide animation for popup window exit transition
        val slideOut = Slide()
        slideOut.slideEdge = Gravity.RIGHT
        popupWindow.exitTransition = slideOut


        popupWindow.isOutsideTouchable = true

        val profile = root.findViewById<ViewGroup>(R.id.tableProfile)
        TransitionManager.beginDelayedTransition(profile)
        dimLayout.visibility = View.VISIBLE
        popupWindow.setOnDismissListener {
            dimLayout.visibility = View.GONE
        }
        popupWindow.showAtLocation(
            profile, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }
}
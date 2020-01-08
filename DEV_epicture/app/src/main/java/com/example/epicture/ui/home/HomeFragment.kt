package com.example.epicture.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.transition.Slide
import android.transition.TransitionManager
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.epicture.Login
import com.example.epicture.R

import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.material.floatingactionbutton.FloatingActionButton

import org.json.JSONArray
import org.json.JSONObject

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.epicture.MainActivity
import com.example.epicture.gallery
import java.io.File
import java.util.*
import kotlin.collections.HashMap

import kotlinx.android.synthetic.main.fragment_home.*


class VolleySingleton constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: VolleySingleton? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: VolleySingleton(context).also {
                    INSTANCE = it
                }
            }
    }
    private val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}

class HomeFragment : Fragment(){

    //image pick code
    private val IMAGE_PICK_CODE = 1000;
    //Permission code
    private val PERMISSION_CODE = 1001;

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var myParent : MainActivity
    private lateinit var dimLayout : RelativeLayout
    var accessToken : String? = ""
    var refreshToken : String? = ""
    var username : String? = ""
    var accountId : String? = ""
    var searchWord : String? = ""
    var searchBy : String? = ""
    var searchWindow : String? = ""

    private fun setBtnLogin(view: View) {
        val btnLogin : ImageButton = view.findViewById(R.id.loginBtn)
        val searchV : SearchView = view.findViewById(R.id.searchView)
        val textV : TextView = view.findViewById(R.id.textView)

        homeViewModel.btnImage.observe(this, Observer {
            if (myParent.username != null && myParent.username != "null")
                btnLogin.setImageResource(it)
            else
                btnLogin.setImageResource(R.drawable.ic_login_white_24dp)
        })

        if (myParent.username != null && myParent.username != "null") {
            searchV.visibility = SearchView.VISIBLE
            textV.visibility = TextView.INVISIBLE
            btnLogin.setOnClickListener {
                myParent.accessToken = "null"
                myParent.refreshToken = "null"
                myParent.username = "null"
                myParent.accountId = "null"
                setBtnLogin(view)
            }
        } else {
            searchV.visibility = SearchView.INVISIBLE
            textV.visibility = TextView.VISIBLE
            val layout = view.findViewById<LinearLayout>(R.id.photos)
            layout.removeAllViews()
            btnLogin.setOnClickListener {
                val intent = Intent(activity, Login::class.java)
                btnLogin.setImageResource(R.drawable.ic_logout_white_24dp)
                startActivity(intent)
                setBtnLogin(view)
            }
        }
    }


    private fun loadTopPics(view: View) {
        // Load Top pics

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(activity)
        val url = "https://api.imgur.com/3/gallery/hot/time/0?IMGURPLATFORM=web&client_id=546c25a59c58ad7&realtimeResults=false&showViral=true"

        var count = 0

        // Request a string response from the provided URL.
        val stringReq = object : StringRequest(
            Method.GET, url,
            Response.Listener<String> { response ->

                val layout = view.findViewById<LinearLayout>(R.id.photos)
                layout.removeAllViews()

                var strResp = response.toString()
                val jsonObj = JSONObject(strResp)
                val jsonArray: JSONArray = jsonObj.getJSONArray("data")
                for (i in 0 until jsonArray.length()) {
                    var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                    try {
                        var jsonImages: JSONArray = jsonInner.getJSONArray("images")
                        val linearLayout = view.findViewById<LinearLayout>(R.id.photos)
                        var jsonSubInner: JSONObject = jsonImages.getJSONObject(0)

                        var url = jsonSubInner.get("link").toString()

                        val container = layoutInflater.inflate(R.layout.gallery_container, null)

                        val image = container.findViewById<ImageView>(R.id.imageView)
                        activity?.let {
                            Glide.with(it)
                                    .load(url)
                                    .into(image)
                        }
                        image.setOnClickListener {
                            val intent = Intent(activity, gallery::class.java)
                            intent.putExtra("images", jsonImages.toString())
                            intent.putExtra("access_token", myParent.accessToken)
                            startActivity(intent)
                        }
                        val upVote : RadioButton = container.findViewById(R.id.upVote)
                        val downVote : RadioButton = container.findViewById(R.id.downVote)
                        val comment : RadioButton = container.findViewById(R.id.comment)
                        setVote(upVote, downVote, jsonInner.get("vote").toString(), jsonInner.get("ups").toString(), jsonInner.get("downs").toString())
                        comment.text = jsonInner.get("comment_count").toString()
                        upVote.setOnClickListener {
                            changeVote(R.id.upVote, container, jsonInner.get("id").toString())
                        }
                        downVote.setOnClickListener {
                            changeVote(R.id.downVote, container, jsonInner.get("id").toString())
                        }
                        linearLayout?.addView(container)
                    } catch (e: org.json.JSONException) {

                    }
                }
                println("lenght : $count")
            },
            Response.ErrorListener {println("That didn't work!")})

        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                println("access: " + myParent.accessToken)
                if (myParent.accessToken == null || myParent.accessToken == "null") {
                    headers["Authorization"] = "Client-ID c285652aaf34652"
                } else {
                    headers["Authorization"] = "Bearer " + myParent.accessToken
                }
                return headers
            }
        }
        queue.add(stringReq)

        //End Load Top pics
    }


    fun changeVote(checkedRadioId: Any, root: View, galleryId: String) {
        if (myParent.accessToken == null || myParent.accessToken == "null") {
            return
        }
        val upVote : RadioButton = root.findViewById(R.id.upVote)
        val downVote : RadioButton = root.findViewById(R.id.downVote)
        if(checkedRadioId == R.id.upVote) {
            if (upVote.tag == "true") {
                vote(galleryId, "veto")
                upVote.text = (upVote.text.toString().toInt() - 1).toString()
                upVote.tag = "false"
                upVote.setButtonDrawable(R.drawable.ic_upvote_empty)
            } else {
                vote(galleryId, "up")
                upVote.text = (upVote.text.toString().toInt() + 1).toString()
                if (downVote.tag == "true") {
                    downVote.text = (downVote.text.toString().toInt() - 1).toString()
                }
                upVote.tag = "true"
                downVote.tag = "false"
                upVote.setButtonDrawable(R.drawable.ic_upvote_full)
                downVote.setButtonDrawable(R.drawable.ic_downvote_empty)
            }
        } else if(checkedRadioId== R.id.downVote) {
            if (downVote.tag == "true") {
                downVote.text = (downVote.text.toString().toInt() - 1).toString()
                vote(galleryId, "veto")
                downVote.tag = "false"
                downVote.setButtonDrawable(R.drawable.ic_downvote_empty)
            } else {
                downVote.text = (downVote.text.toString().toInt() + 1).toString()
                if (upVote.tag == "true") {
                    upVote.text = (upVote.text.toString().toInt() - 1).toString()
                }
                vote(galleryId, "down")
                downVote.tag = "true"
                upVote.tag = "false"
                downVote.setButtonDrawable(R.drawable.ic_downvote_full)
                upVote.setButtonDrawable(R.drawable.ic_upvote_empty)
            }
        }
    }

    //private val client = OkHttpClient()
    var objectMapper = ObjectMapper()

    //var request = Request.Builder().url("https://api.imgur.com/3/gallery/user/rising/0.json").build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        myParent = context as MainActivity
        accessToken = myParent.accessToken
        refreshToken = myParent.refreshToken
        username = myParent.username
        accountId = myParent.accountId
        dimLayout = myParent.findViewById(R.id.dim_layout)

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setBtnLogin(view)

        val search: SearchView = view.findViewById(R.id.searchView)

        val params: LinearLayout = view.findViewById(R.id.params)

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchWord = query
                searchGallery()
                return false
            }
            override fun onQueryTextChange(query: String): Boolean {
                //Log.i(TAG,"Press querytextchange")
                return true
            }
        })

        search.setOnSearchClickListener {
            params.visibility = View.VISIBLE
        }

        search.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                params.visibility = View.INVISIBLE
                val layout = view.findViewById<LinearLayout>(R.id.photos)
                searchWord = ""
                layout.removeAllViews()
                loadTopPics(layout)
                return false
            }
        })

        val sortBy : Spinner = params.findViewById(R.id.spinnerSort)
        val sortWindow : Spinner = params.findViewById(R.id.spinnerWindow)

        val list1 = arrayOf("top", "time", "viral")
        val list2 = arrayOf("all", "day", "week", "month", "year")

        val arrayAdapter = ArrayAdapter(activity as Context, android.R.layout.simple_spinner_item, list1)
        sortBy.adapter = arrayAdapter

        sortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                searchBy = list1[position]
                searchGallery()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val arrayAdapter2 = ArrayAdapter(activity as Context, android.R.layout.simple_spinner_item, list2)
        sortWindow.adapter = arrayAdapter2

        sortWindow.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                searchWindow = list2[position]
                searchGallery()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val addButton : FloatingActionButton = view.findViewById(R.id.addButton)

        addButton.setOnClickListener {
            if (this.context?.let { it1 -> ContextCompat.checkSelfPermission(it1, Manifest.permission.READ_EXTERNAL_STORAGE) } ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE);
            }
            else{
                //permission already granted
                pickImageFromGallery();
            }
        }

        loadTopPics(view)

        return view
    }

    fun searchGallery() {
        val query = searchWord
        val sort = searchBy
        val window = searchWindow

        if (query == "")
            return
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(activity)
        val url = "https://api.imgur.com/3/gallery/search/$sort/$window?q=$query"


        // Request a string response from the provided URL.
        val stringReq = object : StringRequest(
            Method.GET, url,
            Response.Listener<String> { response ->

                val layout = view!!.findViewById<LinearLayout>(R.id.photos)
                layout.removeAllViews()

                var strResp = response.toString()
                val jsonObj = JSONObject(strResp)
                val jsonArray: JSONArray = jsonObj.getJSONArray("data")
                for (i in 0 until jsonArray.length()) {
                    var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                    try {
                        var jsonImages: JSONArray = jsonInner.getJSONArray("images")
                        val linearLayout = layout.findViewById<LinearLayout>(R.id.photos)
                        var jsonSubInner: JSONObject = jsonImages.getJSONObject(0)

                        var url = jsonSubInner.get("link").toString()


                        val container = layoutInflater.inflate(R.layout.gallery_container, null)

                        val image = container.findViewById<ImageView>(R.id.imageView)
                        activity?.let {
                            Glide.with(it)
                                .load(url)
                                .into(image)
                        }
                        image.setOnClickListener {
                            val intent = Intent(activity, gallery::class.java)
                            intent.putExtra("images", jsonImages.toString())
                            intent.putExtra("access_token", myParent.accessToken)
                            startActivity(intent)
                        }
                        val upVote : RadioButton = container.findViewById(R.id.upVote)
                        val downVote : RadioButton = container.findViewById(R.id.downVote)
                        val comment : RadioButton = container.findViewById(R.id.comment)
                        setVote(upVote, downVote, jsonInner.get("vote").toString(), jsonInner.get("ups").toString(), jsonInner.get("downs").toString())
                        comment.text = jsonInner.get("comment_count").toString()
                        upVote.setOnClickListener {
                            changeVote(R.id.upVote, container, jsonInner.get("id").toString())
                        }
                        downVote.setOnClickListener {
                            changeVote(R.id.downVote, container, jsonInner.get("id").toString())
                        }
                        linearLayout?.addView(container)
                    } catch (e: org.json.JSONException) {

                    }
                }
            },
            Response.ErrorListener {println("That didn't work!")})

        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                println("access: " + myParent.accessToken)
                if (myParent.accessToken == null || myParent.accessToken == "null") {
                    headers["Authorization"] = "Client-ID c285652aaf34652"
                } else {
                    headers["Authorization"] = "Bearer " + myParent.accessToken
                }
                return headers
            }
        }
        queue.add(stringReq)

    }

    override fun onResume() {
        setBtnLogin(view!!)
        loadTopPics(view!!)
        super.onResume()
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()

                }
                else{
                    //permission from popup denied
                    Toast.makeText(this.context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun encoder(filePath: String): String{
        println("filePath $filePath")
        val bytes = File(filePath).readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }


    private fun getPath(ctx: Context, uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = ctx.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        }
        return uri.path
    }

    private fun upload(filePath: String, title: String, description: String) {
        val url = "https://api.imgur.com/3/upload"
        val params = HashMap<String,String>()
        params["image"] = encoder(filePath)
        params["title"] = title
        params["description"] = description

        val jsonObject = JSONObject(params as Map<*, *>)
        println(jsonObject.toString())

        println("Starting Upload")
        val request = object: JsonObjectRequest(
            Method.POST,url,jsonObject,
            Response.Listener { response ->
                // Process the json
                try {
                    // ok
                    println("Response: $response")
                    if (myParent.accessToken == null || myParent.accessToken == "null") {
                        var strResp = response.toString()
                        val jsonObj = JSONObject(strResp)
                        val data: JSONObject = jsonObj.get("data") as JSONObject
                        val hash = data.get("deletehash").toString()
                        println("___________ deletehash: $hash")
                    }
                }catch (e:Exception){
                    // fail
                    println("Exception: $e")
                }

            }, Response.ErrorListener{
                println("Volley error: $it")
                // Error in request
            })
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                println("access: " + myParent.accessToken)
                if (myParent.accessToken == null || myParent.accessToken == "null") {
                    headers["Authorization"] = "Client-ID c285652aaf34652"
                } else {
                    headers["Authorization"] = "Bearer " + myParent.accessToken
                }
                return headers
            }
        }
        // Volley request policy, only one time request to avoid duplicate transaction
        /*request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            // 0 means no retry
            0, // DefaultRetryPolicy.DEFAULT_MAX_RETRIES = 2
            1f // DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
         */

        this.context?.let { it1 -> VolleySingleton.getInstance(it1).addToRequestQueue(request) }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            val filePath: String = this.context?.let { getPath(it, data?.data) }.toString()


            // Initialize a new layout inflater instance
            val inflater:LayoutInflater = this.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            // Inflate a custom view using layout inflater
            val popUp = inflater.inflate(R.layout.activity_upload,null)


            // Initialize a new instance of popup window
            val popupWindow = PopupWindow(
                popUp, // Custom view to show in popup window
                ConstraintLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                ConstraintLayout.LayoutParams.WRAP_CONTENT // Window height
            )


            popupWindow.isFocusable = true;
            // Set an elevation for the popup window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }

            val choosePic = popUp.findViewById<ImageView>(R.id.choosePic)
            choosePic.setImageURI(data?.data)
            val buttonPopup = popUp.findViewById<Button>(R.id.button_popup)
            buttonPopup.setOnClickListener{
                var titText : EditText = popUp.findViewById(R.id.titlePhoto)
                var descText : EditText = popUp.findViewById(R.id.description)

                upload(filePath, titText.text.toString(), descText.text.toString())

                popupWindow.dismiss()
            }

            // If API level 23 or higher then execute the code
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                // Create a new slide animation for popup window enter transition
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn

                // Slide animation for popup window exit transition
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut

            }

            popupWindow.isOutsideTouchable = true

            // Finally, show the popup window on app
            TransitionManager.beginDelayedTransition(linearLayout)
            dimLayout.visibility = View.VISIBLE
            popupWindow.setOnDismissListener {
                dimLayout.visibility = View.GONE
            }
            popupWindow.showAtLocation(
                linearLayout, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )
        }
    }


    private fun favoriteImage(imageId: String) {
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
                println("access: " + myParent.accessToken)
                    headers["Authorization"] = "Bearer " + myParent.accessToken
                return headers
            }
        }
        this.context?.let { it1 -> VolleySingleton.getInstance(it1).addToRequestQueue(request) }
    }

    private fun setVote(upVote: RadioButton, downVote: RadioButton, galleryVote: String, ups: String, downs: String) {
        upVote.text = ups
        downVote.text = downs
        if (galleryVote == "up") {
            upVote.tag = "true"
            upVote.setButtonDrawable(R.drawable.ic_upvote_full)
        } else if (galleryVote == "down") {
            downVote.tag = "true"
            downVote.setButtonDrawable(R.drawable.ic_downvote_full)
        }
    }

    private fun vote(galleryHash: String, toDo: String) {
        if (myParent.accessToken == null || myParent.accessToken == "null") {
            return
        }
        val url = "https://api.imgur.com/3/gallery/$galleryHash/vote/$toDo"
        val params = HashMap<String,String>()

        val jsonObject = JSONObject(params as Map<*, *>)
        println(jsonObject.toString())

        println("Starting vote $toDo to $galleryHash")
        val request = object: JsonObjectRequest(
            Method.POST,url,jsonObject,
            Response.Listener { response ->}, Response.ErrorListener{
                println("Volley error: $it")
                // Error in request
            })
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                println("access: " + myParent.accessToken)
                headers["Authorization"] = "Bearer " + myParent.accessToken
                return headers
            }
        }
        this.context?.let { it1 -> VolleySingleton.getInstance(it1).addToRequestQueue(request) }
    }
}
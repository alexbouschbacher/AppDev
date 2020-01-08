package com.example.epicture.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.epicture.Login
import com.example.epicture.MainActivity
import com.example.epicture.R
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.epicture.ui.home.VolleySingleton
import org.json.JSONArray
import org.json.JSONObject


class FavoriteFragment : Fragment() {

    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var myParent : MainActivity
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

        favoriteViewModel =
            ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_favorite, container, false)
        val include1 : View = root.findViewById(R.id.includeFavorite)
        val include2 : View = root.findViewById(R.id.includeNotLogin)
        if (accessToken == null || accessToken == "null" || accessToken == "") {
            include1.visibility = View.INVISIBLE
            include2.visibility = View.VISIBLE
            val btnLogin: Button = root.findViewById(R.id.buttonNotLogin)
            btnLogin.setOnClickListener {
                val intent = Intent(activity, Login::class.java)
                startActivity(intent)
            }
        } else {
            include1.visibility = View.VISIBLE
            include2.visibility = View.INVISIBLE

            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(activity)
            val url = "https://api.imgur.com/3/account/me/favorites/"


            // Request a string response from the provided URL.
            val stringReq = object : StringRequest(
                Method.GET, url,
                Response.Listener<String> { response ->

                    val layout = include1.findViewById<LinearLayout>(R.id.photosFavorite)
                    layout?.removeAllViews()

                    var strResp = response.toString()
                    val jsonObj = JSONObject(strResp)
                    val jsonArray: JSONArray = jsonObj.getJSONArray("data")
                    val linearLayout = layout.findViewById<LinearLayout>(R.id.photosFavorite)
                    linearLayout.removeAllViews()
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        try {
                            println("inner " + jsonInner)
                            var image_count: String = jsonInner.get("images_count").toString()
                            println("image_count : " + image_count)
                            var strUser = ""
                            var nb = ""
                            var jsonSubInner: JSONObject
                            if (image_count == "0") {
                                strUser = jsonInner.get("link").toString()
                                nb = jsonInner.get("views").toString()
                            } else {
                                var jsonImages: JSONArray = jsonInner.getJSONArray("images")
                                jsonSubInner = jsonImages.getJSONObject(0)
                                strUser = jsonSubInner.get("link").toString()
                                nb = jsonSubInner.get("views").toString()
                            }
                            println("strUser : " + strUser)
                            val content = layoutInflater.inflate(R.layout.image_container, null)

                            val newView = content.findViewById<ImageView>(R.id.imageView)
                            activity?.let {
                                Glide.with(it)
                                    .load(strUser)
                                    .into(newView)
                            }
                            val btnLike: RadioButton = content.findViewById(R.id.likeImage)
                            btnLike.tag = "like"
                            btnLike.setButtonDrawable(R.drawable.ic_heart_full)
                            btnLike.setOnClickListener {
                                if (btnLike.tag == "unlike") {
                                    btnLike.tag = "like"
                                    btnLike.setButtonDrawable(R.drawable.ic_heart_full)
                                    //favoriteImage(jsonInner.get("id").toString())
                                } else {
                                    //favoriteImage(jsonInner.get("id").toString())
                                    btnLike.tag = "unlike"
                                    btnLike.setButtonDrawable(R.drawable.ic_heart_empty)
                                }
                            }
                            val views : RadioButton = content.findViewById(R.id.viewImage)
                            views.text = nb
                            linearLayout?.addView(content)
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
        return root
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


//https://api.imgur.com/3/account/me/favorites/
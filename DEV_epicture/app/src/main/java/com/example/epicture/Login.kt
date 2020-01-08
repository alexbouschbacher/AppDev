package com.example.epicture

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import java.net.Authenticator
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Intent

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import java.nio.file.Files.delete
import java.nio.file.Files.exists
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.io.File
import java.net.URL


class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val url = "https://api.imgur.com/oauth2/authorize?client_id=X&response_type=token&state=APPLICATION_STATE"
        webView.loadUrl(url)
        val settings = webView.settings;
        settings.javaScriptEnabled = true
        android.webkit.CookieManager.getInstance().removeAllCookies(null)
        android.webkit.CookieManager.getInstance().flush()
        // Enable and setup web view cache
//        settings.setAppCacheEnabled(true)
  //      settings.cacheMode = WebSettings.LOAD_DEFAULT
    //    settings.setAppCachePath(cacheDir.path)

        settings.javaScriptCanOpenWindowsAutomatically = false
        val intent = Intent(baseContext, MainActivity::class.java)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String
            ): Boolean {
                var accessToken = url.substring(url.indexOf("access_token=") + "access_token=".length, url.indexOf("&", url.indexOf("access_token=")))
                intent.putExtra("accessToken",accessToken)
                var refreshToken = url.substring(url.indexOf("refresh_token=") + "refresh_token=".length, url.indexOf("&", url.indexOf("refresh_token=")))
                intent.putExtra("refreshToken",refreshToken)
                var username = url.substring(url.indexOf("username=") + "username=".length, url.indexOf("&", url.indexOf("username=")))
                intent.putExtra("username",username)
                var accountId = url.substring(url.indexOf("account_id=") + "account_id=".length, url.length)
                intent.putExtra("accountId",accountId)
                startActivity(intent)
                return true
            }
        }


    }
}

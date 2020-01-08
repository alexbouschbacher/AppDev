package com.example.epicture.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text
    private val _bio = MutableLiveData<String>().apply {
        value = ""
    }
    var bio: LiveData<String> = _bio
}
package com.example.epicture.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epicture.R

class HomeViewModel : ViewModel() {
    private var _image = MutableLiveData<Int>().apply {
        value = R.drawable.ic_logout_white_24dp
    }
    var btnImage: LiveData<Int> = _image
}
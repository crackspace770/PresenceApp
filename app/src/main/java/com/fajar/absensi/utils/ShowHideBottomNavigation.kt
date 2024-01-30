package com.fajar.absensi.utils

import androidx.fragment.app.Fragment
import com.fajar.absensi.ui.MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.hideBottomNavigationView(){
    val bottomNavigationView =
        (activity as MainActivity).findViewById<BottomNavigationView>(
            com.fajar.absensi.R.id.bottom_nav
        )
    bottomNavigationView.visibility = android.view.View.GONE
}

fun Fragment.showBottomNavigationView(){
    val bottomNavigationView =
        (activity as MainActivity).findViewById<BottomNavigationView>(
            com.fajar.absensi.R.id.bottom_nav
        )
    bottomNavigationView.visibility = android.view.View.VISIBLE
}
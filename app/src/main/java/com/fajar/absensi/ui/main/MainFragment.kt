package com.fajar.absensi.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fajar.absensi.databinding.FragmentMainBinding
import com.fajar.absensi.ui.absen.AbsenActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment:Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onAction()

    }

    private fun onAction(){
        binding.cvAbsenMasuk.setOnClickListener {
            startActivity(Intent(requireContext(),AbsenActivity::class.java) )
        }

    }

}
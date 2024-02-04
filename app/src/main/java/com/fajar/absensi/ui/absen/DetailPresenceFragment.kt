package com.fajar.absensi.ui.absen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.fajar.absensi.databinding.FragmentPresenceDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailPresenceFragment: Fragment() {

    private lateinit var binding: FragmentPresenceDetailBinding
    private val args by navArgs<DetailPresenceFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPresenceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n", "CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val presence = args.presence

        binding.apply {

            tvNoPegawaiDetail.text = presence.idPegawai
            tvNamaDetail.text = presence.name
            tvLokasiDetail.text = presence.lokasi
            tvTanggalDetail.text = presence.tanggal
            tvKeteranganDetail.text = presence.keterangan
            tvStatusDetail.text = presence.keterangan

            Glide.with(requireContext())
                .load(presence.fotoSelfie)
                .into(imageAttendee)

            imgClose.setOnClickListener {
                findNavController().navigateUp()
            }

        }

    }


}
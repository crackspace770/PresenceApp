package com.fajar.absensi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fajar.absensi.databinding.ItemListHistoryBinding
import com.fajar.absensi.model.Presensi

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemListHistoryBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(presensi: Presensi) {
            binding.apply {
                tvNama.text = presensi.name
                tvNomor.text = presensi.idPegawai
                tvAbsenTime.text = presensi.tanggal
                tvLokasi.text = presensi.lokasi
                tvStatusAbsen.text = presensi.keterangan
                Glide.with(itemView).load(presensi.fotoSelfie).into(imageProfile)
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Presensi>() {
        override fun areItemsTheSame(oldItem: Presensi, newItem: Presensi): Boolean {
            return oldItem.name== newItem.name
        }

        override fun areContentsTheSame(oldItem: Presensi, newItem: Presensi): Boolean {
            return oldItem == newItem
        }
    }


    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            ItemListHistoryBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HistoryAdapter.HistoryViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.bind(order)

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


}
package com.fajar.absensi.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Presensi(
    val uid: String,
    val name:String? = null,
    val idPegawai:String? = null,
    val fotoSelfie:String? = null,
    val tanggal: String? = null,
    val lokasi:String? = null,
    val keterangan: String? = null,
    val employeeName: String? = null,
    val employeeId: String? = null,

): Parcelable {
    // Add a default no-argument constructor
    constructor() : this("", "", "", "", "", "", "", "","")
}
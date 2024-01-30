package com.fajar.absensi.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fajar.absensi.model.Presensi
import com.fajar.absensi.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
):ViewModel() {

    private val _allHistory = MutableStateFlow<Resource<List<Presensi>>>(Resource.Unspecified())
    val allHistory = _allHistory.asStateFlow()

    init {
        getAllHistory()
    }

    private fun getAllHistory() {

        viewModelScope.launch {
            _allHistory.emit(Resource.Loading())
        }

        firestore.collection("user").document(auth.uid!!).collection("presensi").get()
            .addOnSuccessListener {
                val orders = it.toObjects(Presensi::class.java)
                viewModelScope.launch {
                    _allHistory.emit(Resource.Success(orders))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _allHistory.emit(Resource.Error(it.message.toString()))
                }
            }
    }

}
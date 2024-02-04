package com.fajar.absensi.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fajar.absensi.R
import com.fajar.absensi.adapter.HistoryAdapter
import com.fajar.absensi.databinding.FragmentMainBinding
import com.fajar.absensi.utils.Resource
import com.fajar.absensi.utils.loadImageUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class MainFragment:Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val viewModel by viewModels<MainViewModel>()
    private val historyAdapter by lazy{ HistoryAdapter() }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val dateFormat = "dd-MM-yyyy HH:mm:ss"

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


        lifecycleScope.launchWhenStarted {
            viewModel.allHistory.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAllOrders.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarAllOrders.visibility = View.GONE
                        historyAdapter.differ.submitList(it.data)

                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarAllOrders.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }

        historyAdapter.onClick = {
            val b = Bundle().apply { putParcelable("presence",it) }
            findNavController().navigate(R.id.action_mainFragment_to_detailPresenceFragment,b)
        }

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        retrieveData()
        setHistoryRV()

        val date = getCurrentDateTime()
        val time = getCurrentTime()


        binding.apply {
            tvDate.text = date.toString("EEEE, dd MMMM yyyy")
            tvTime.text = time
        }


    }

    private fun getCurrentTime(): String {
        val currentTime = System.currentTimeMillis()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(currentTime))
    }


    private fun setHistoryRV(){
        binding.rvHistories.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        }
    }

    @SuppressLint("SetTextI18n")
    private fun retrieveData(){

        val userId = auth.currentUser!!.uid
        val dataUser = db.collection("user").document(userId)
        dataUser.get().addOnSuccessListener {
            val name = it.get("name")
            val fotoProfile = it.get("fotoProfil")

            binding.apply {
                tvUsername.text =" Welcome, ${name.toString()}"
                ivImageProfile.loadImageUrl(fotoProfile.toString(),requireContext())

            }

        }

    }

    private fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    private fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    private fun dateFormatter(format: String = "yyyy-MM-dd"): SimpleDateFormat {
        val locale = Locale("id", "ID")

        return SimpleDateFormat(format, locale).apply {
            timeZone = TimeZone.getTimeZone("GMT+07:00")
        }
    }

}
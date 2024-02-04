package com.fajar.absensi.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.fajar.absensi.databinding.FragmentProfileBinding
import com.fajar.absensi.preference.UserPreference
import com.fajar.absensi.ui.authentication.LoginActivity
import com.fajar.absensi.utils.loadImageUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class ProfileFragment:Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var sharedPreference: UserPreference? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        sharedPreference = UserPreference(requireContext())

        retrieveData()
        onAction()
    }

    private fun onAction(){

        binding.clEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.clLogout.setOnClickListener {
            showExitConfirmationDialog()
        }

    }

    private fun retrieveData(){

        val userId = auth.currentUser!!.uid
        val dataUser = db.collection("user").document(userId)
        dataUser.get().addOnSuccessListener {
            val name = it.get("name")
            val fotoProfil = it.get("fotoProfil")

            binding.apply {
                tvNamaProfile.text = name.toString()
                ivPhoto.loadImageUrl(fotoProfil.toString(), requireContext())

            }

        }

    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                binding.progressBarDialog.root.visibility = View.VISIBLE
                // Clear the shared preference for login status
                sharedPreference?.saveBoolean("isLoggedIn", false)
                // Logout from Firebase
                auth.signOut()
                // Redirect to LoginActivity
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                binding.progressBarDialog.root.visibility = View.GONE
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}
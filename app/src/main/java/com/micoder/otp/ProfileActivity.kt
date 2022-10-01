package com.micoder.otp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.micoder.otp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        // logoutBtn click, logout the user
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

    }

    private fun checkUser() {
        // get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // logged out
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // logged in, get phone number of user
            val phone = firebaseUser.phoneNumber
            // set phone number
            binding.phoneTv.text = phone
        }
    }
}
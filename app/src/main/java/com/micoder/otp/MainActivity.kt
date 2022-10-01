package com.micoder.otp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.micoder.otp.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityMainBinding

    // if code sending failed, will used to resend
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null

    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var mVerificationId: String? = null
    private lateinit var firebaseAuth: FirebaseAuth

    private val TAG = "MAIN_TAG"

    // Progress Dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneLl.visibility = View.VISIBLE
        binding.codeLl.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted: ")
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // THis callback is invoked in an invalid request for verification is made,
                // for instance if the phone number format is not valid.
                progressDialog.dismiss()
                Log.d(TAG, "onVerificationFailed: ${e.message}")
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent: $verificationId")
                mVerificationId = verificationId
                forceResendingToken = token
                progressDialog.dismiss()

                Log.d(TAG, "onCodeSent: $verificationId")

                // hide phone layout, show code layout
                binding.phoneLl.visibility = View.GONE
                binding.codeLl.visibility = View.VISIBLE
                Toast.makeText(this@MainActivity, "Verification Code Sent...", Toast.LENGTH_SHORT).show()
                binding.codeSentDescriptionTv.text = "Please enter your verification code we sent to ${binding.phoneEt.text.toString().trim()}"
            }
        }

        // phoneContinueBtn click: input phone number, validate, start phone authentication/login
        binding.phoneContinueBtn.setOnClickListener {
            // input phone number
            val phone = binding.phoneEt.text.toString().trim()
            // validate phone number
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this@MainActivity, "Please enter your phone number", Toast.LENGTH_SHORT).show()
            } else {
                startPhoneNumberVerification(phone)
            }
        }

        // resendCodeTv click: (if code didn't receive) resend verification code / OTP
        binding.resendCodeTv.setOnClickListener {
            // input phone number
            val phone = binding.phoneEt.text.toString().trim()
            // validate phone number
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this@MainActivity, "Please enter your phone number", Toast.LENGTH_SHORT).show()
            } else {
                resendVerificationCode(phone, forceResendingToken)
            }
        }

        // codeSubmitBtn click: input verification code, validate, verify phone number with verification code
        binding.codeSubmitBtn.setOnClickListener {
            // input verification code
            val code = binding.codeEt.text.toString().trim()
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this@MainActivity, "Please enter verification code", Toast.LENGTH_SHORT).show()
            } else {
                verifyPhoneNumberWithCode(mVerificationId, code)
            }
        }
    }

    private fun startPhoneNumberVerification(phone : String) {
        Log.d(TAG, "startPhoneNumberVerification: $phone")
        progressDialog.setMessage("Verifying Phone Number...")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(phone: String, token: PhoneAuthProvider.ForceResendingToken?) {
        progressDialog.setMessage("Resending Code...")
        progressDialog.show()

        Log.d(TAG, "resendVerificationCode: $phone")

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks!!)
            .setForceResendingToken(token!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        Log.d(TAG, "verifyPhoneNumberWithCode: $verificationId $code")
        progressDialog.setMessage("Verifying Code...")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: ")
        progressDialog.setMessage("Logging In")

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                // login success
                progressDialog.dismiss()
                val phone = firebaseAuth.currentUser!!.phoneNumber
                Toast.makeText(this, "Logged In as $phone", Toast.LENGTH_SHORT).show()

                // start profile activity
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                // login failed
                progressDialog.dismiss()
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }



}

/*
Now we need to add
* 1) SHA1
* 2) SHA256
* 3) Browser Library
* 4) Enable Android Device Verification API in google cloud console API
*/

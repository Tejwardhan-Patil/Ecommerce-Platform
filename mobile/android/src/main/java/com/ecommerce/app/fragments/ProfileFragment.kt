package com.ecommerce.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.ecommerce.app.R
import com.ecommerce.app.activities.LoginActivity
import com.ecommerce.app.models.User
import com.ecommerce.app.services.ApiService
import com.ecommerce.app.services.AuthService
import com.ecommerce.app.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var editNameEditText: EditText
    private lateinit var editEmailEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var logoutButton: Button
    private var authService: AuthService? = null
    private var apiService: ApiService? = null
    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        profileImageView = rootView.findViewById(R.id.profileImageView)
        nameTextView = rootView.findViewById(R.id.nameTextView)
        emailTextView = rootView.findViewById(R.id.emailTextView)
        editNameEditText = rootView.findViewById(R.id.editNameEditText)
        editEmailEditText = rootView.findViewById(R.id.editEmailEditText)
        saveButton = rootView.findViewById(R.id.saveButton)
        logoutButton = rootView.findViewById(R.id.logoutButton)

        authService = AuthService(requireContext())
        apiService = ApiService()

        if (!authService?.isLoggedIn()!!) {
            navigateToLogin()
        }

        loadUserProfile()

        saveButton.setOnClickListener {
            updateProfile()
        }

        logoutButton.setOnClickListener {
            logout()
        }

        return rootView
    }

    private fun loadUserProfile() {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = authService?.getUserId()
            if (userId != null && NetworkUtils.isConnected(requireContext())) {
                try {
                    val user = apiService?.getUserProfile(userId)
                    if (user != null) {
                        currentUser = user
                        withContext(Dispatchers.Main) {
                            populateProfile(user)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error loading profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun populateProfile(user: User) {
        nameTextView.text = user.name
        emailTextView.text = user.email
        editNameEditText.setText(user.name)
        editEmailEditText.setText(user.email)

        Glide.with(this)
            .load(user.profileImageUrl)
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(profileImageView)
    }

    private fun updateProfile() {
        val newName = editNameEditText.text.toString().trim()
        val newEmail = editEmailEditText.text.toString().trim()

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Name and email cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updatedUser = apiService?.updateUserProfile(currentUser.id, newName, newEmail)
                if (updatedUser != null) {
                    withContext(Dispatchers.Main) {
                        currentUser = updatedUser
                        populateProfile(updatedUser)
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error updating profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun logout() {
        authService?.logout()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authService = null
        apiService = null
    }
}
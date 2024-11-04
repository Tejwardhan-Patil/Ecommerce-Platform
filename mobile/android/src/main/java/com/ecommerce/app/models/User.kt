package com.ecommerce.app.models

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    var username: String,
    var email: String,
    private var password: String,
    var fullName: String? = null,
    var address: String? = null,
    var phoneNumber: String? = null,
    var avatarUrl: String? = null,
    var isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    var lastLogin: Long? = null
) {

    // Password validation
    fun isValidPassword(inputPassword: String): Boolean {
        return inputPassword == password
    }

    // Password update
    fun updatePassword(currentPassword: String, newPassword: String): Boolean {
        return if (isValidPassword(currentPassword)) {
            password = newPassword
            true
        } else {
            false
        }
    }

    // Update profile info
    fun updateProfile(fullName: String? = null, address: String? = null, phoneNumber: String? = null, avatarUrl: String? = null) {
        if (fullName != null) this.fullName = fullName
        if (address != null) this.address = address
        if (phoneNumber != null) this.phoneNumber = phoneNumber
        if (avatarUrl != null) this.avatarUrl = avatarUrl
    }

    // Login
    fun login(inputPassword: String): Boolean {
        return if (isValidPassword(inputPassword)) {
            lastLogin = System.currentTimeMillis()
            true
        } else {
            false
        }
    }

    // Check if user is active
    fun isUserActive(): Boolean {
        return isActive
    }

    // Deactivate user
    fun deactivateAccount() {
        isActive = false
    }

    // Reactivate user
    fun reactivateAccount() {
        isActive = true
    }

    // String representation for logging or display purposes
    override fun toString(): String {
        return "User(id='$id', username='$username', email='$email', fullName='$fullName', address='$address', phoneNumber='$phoneNumber', avatarUrl='$avatarUrl', isActive=$isActive, createdAt=$createdAt, lastLogin=$lastLogin)"
    }

    // Companion object to manage creation of new users
    companion object {
        fun createUser(
            username: String,
            email: String,
            password: String,
            fullName: String? = null,
            address: String? = null,
            phoneNumber: String? = null,
            avatarUrl: String? = null
        ): User {
            return User(
                username = username,
                email = email,
                password = password,
                fullName = fullName,
                address = address,
                phoneNumber = phoneNumber,
                avatarUrl = avatarUrl
            )
        }
    }
}

// Helper function for validating email format
fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
    return emailRegex.toRegex().matches(email)
}

// User authentication service
class UserService(private val users: MutableList<User> = mutableListOf()) {

    fun registerUser(username: String, email: String, password: String): User? {
        if (users.any { it.email == email }) {
            println("User with this email already exists.")
            return null
        }

        if (!isValidEmail(email)) {
            println("Invalid email format.")
            return null
        }

        val newUser = User.createUser(username, email, password)
        users.add(newUser)
        return newUser
    }

    fun loginUser(email: String, password: String): User? {
        val user = users.find { it.email == email && it.isValidPassword(password) }
        return if (user != null) {
            user.login(password)
            user
        } else {
            null
        }
    }

    fun deactivateUser(user: User) {
        user.deactivateAccount()
    }

    fun reactivateUser(user: User) {
        user.reactivateAccount()
    }

    fun updateUserProfile(
        user: User,
        fullName: String? = null,
        address: String? = null,
        phoneNumber: String? = null,
        avatarUrl: String? = null
    ) {
        user.updateProfile(fullName, address, phoneNumber, avatarUrl)
    }

    fun deleteUser(user: User) {
        users.remove(user)
    }

    // Fetch all users
    fun getAllUsers(): List<User> {
        return users
    }

    // Fetch user by email
    fun getUserByEmail(email: String): User? {
        return users.find { it.email == email }
    }

    // Fetch active users
    fun getActiveUsers(): List<User> {
        return users.filter { it.isUserActive() }
    }
}
package com.example.politicalevents.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _authState.value = AuthState.Success(it.user!!.uid)
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
    }

    fun signup(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid

                db.collection("users").document(uid).set(
                    mapOf(
                        "uid" to uid,
                        "username" to name,
                        "totalPoints" to 0,
                        "level" to 1,
                        "streak" to 0
                    )
                )

                _authState.value = AuthState.Success(uid)
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Signup failed")
            }
    }

    fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Reset failed")
            }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}

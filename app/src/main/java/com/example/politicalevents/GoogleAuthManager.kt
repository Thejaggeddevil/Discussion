package com.example.politicalevents

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class GoogleAuthManager(context: Context) {

    private val auth = FirebaseAuth.getInstance()

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(
            context.resources.getIdentifier(
                "default_web_client_id",
                "string",
                context.packageName
            )
        ))
        .requestEmail()
        .build()

    private val client = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent() = client.signInIntent

    fun handleResult(
        data: android.content.Intent?,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it) }

        } catch (e: Exception) {
            onError(e)
        }
    }
}

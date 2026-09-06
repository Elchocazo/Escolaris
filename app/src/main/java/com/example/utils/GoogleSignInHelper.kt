package com.example.utils

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

object GoogleSignInHelper {

    /**
     * Triggers the Android Credential Manager Bottom Sheet for Google Sign-In.
     * Returns the Google ID Token or throws an exception if cancelled / failed.
     */
    suspend fun getGoogleIdToken(
        context: Context,
        serverClientId: String = "1039847291823-clientoauthsample.apps.googleusercontent.com"
    ): String {
        val credentialManager = CredentialManager.create(context)

        // Generate a random nonce for security
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result: GetCredentialResponse = credentialManager.getCredential(
            request = request,
            context = context
        )

        val credential = result.credential
        if (credential is androidx.credentials.CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return googleIdTokenCredential.idToken
        } else {
            throw IllegalStateException("Tipo de credencial no soportada para Google Sign-In")
        }
    }
}

package com.example.tastebook.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastebook.BuildConfig
import com.example.tastebook.DataStoreKeys.ACCESS_TOKEN
import com.example.tastebook.DataStoreKeys.IS_SIGNED_IN
import com.example.tastebook.DataStoreKeys.USER_EMAIL
import com.example.tastebook.data.Recipe
import com.example.tastebook.data.RecipeDao
import com.example.tastebook.dataStore
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes.DRIVE_FILE
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(
    private val recipeDao: RecipeDao,
    private val app: Application
) : ViewModel() {

    private val authClient = Identity.getAuthorizationClient(app)

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken

    private val _updateRecipes = MutableStateFlow(false)
    val updateRecipes: StateFlow<Boolean> = _updateRecipes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus

    private val _signInState = MutableStateFlow<String?>(null)
    val signInStatus: StateFlow<String?> = _signInState

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn = _isSignedIn

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    private val WEB_CLIENT_ID: String = BuildConfig.CLIENT_ID

//    private val WEB_CLIENT_ID by lazy {
//        app.resources.openRawResource(R.raw.web_credentials).bufferedReader().use {
//            val json = it.readText()
//            val obj = JSONObject(json)
//            obj.getJSONObject("web").getString("client_id")
//        }
//    }

    init {
        viewModelScope.launch {
            val prefs = app.dataStore.data.first()
            _isSignedIn.value = prefs[IS_SIGNED_IN] ?: false
            _userEmail.value = prefs[USER_EMAIL]
        }
    }

    // -----------------------------
    // Save login state to DataStore
    // -----------------------------
    suspend fun saveLoginState(signedIn: Boolean, token: String?) {
        app.dataStore.edit { prefs ->
            prefs[IS_SIGNED_IN] = signedIn
            token?.let { prefs[ACCESS_TOKEN] = it }
        }
        _isSignedIn.value = signedIn
        _accessToken.value = token
    }

    fun signOff() {
        viewModelScope.launch {
            app.dataStore.edit { prefs -> prefs.clear() }
            _isSignedIn.value = false
            _userEmail.value = null
        }
    }


    // -----------------------------------------
    // 1) Start Google sign-in using CredentialManager
    // -----------------------------------------
    fun beginGoogleSignIn(
        activity: Activity,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        Log.d("klima", "beginGoogleSignIn: ")
        val nonce = UUID.randomUUID().toString()
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .setNonce(nonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activity)
                val result = credentialManager.getCredential(activity, request)
                handleSignIn(result, launcher)
            } catch (e: Exception) {
                _signInState.value = "Sign-in failed: ${e.message}"
                Log.d("klima", "beginGoogleSignIn: ERROR ${e.message}")
            }
        }
    }

    private fun handleSignIn(
        result: GetCredentialResponse,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        Log.d("klima", "handleSignIn: ")
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
            _userEmail.value = googleIdToken.id
            _isSignedIn.value = true

            viewModelScope.launch {
                app.dataStore.edit { prefs ->
                    prefs[USER_EMAIL] = userEmail.value ?: "Unknown"
                }
            }
        }

        requestDriveAccess(launcher)
    }

    // -----------------------------------------
    // 2) Request Drive access
    // -----------------------------------------
    fun requestDriveAccess(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        val scopeDrive = Scope(DriveScopes.DRIVE_APPDATA)
        val scopeDriveFile = Scope(DRIVE_FILE)
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(scopeDrive, scopeDriveFile))
            .requestOfflineAccess(WEB_CLIENT_ID)
            .build()

        authClient.authorize(request)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    Log.d("klima", "requestDriveAccess: addOnSuccessListener hasResolution")
                    launcher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build()
                    )
                } else {
                    _accessToken.value = result.accessToken
//                    val serverAuthCode = result.serverAuthCode

                    // Save login state here
                    viewModelScope.launch {
                        saveLoginState(
                            signedIn = true,
                            token = result.accessToken
                        )
                    }

                    if (updateRecipes.value) {
                        uploadRecipesToDrive(launcher)
                        _updateRecipes.value = false
                    }
                    Log.d("klima", "requestDriveAccess: result.accessToken: ${result.accessToken}")
                }
            }
            .addOnFailureListener {
                Log.d("klima", "requestDriveAccess: ERROR ${it.message}")
                _syncStatus.value = "Failed to authorize: ${it.message}"
                if (updateRecipes.value) {
                    _isLoading.value = false
                    _updateRecipes.value = false
                }
            }
    }

    // -----------------------------------------
    // 3) Handle result from ActivityResultLauncher
    // -----------------------------------------
    fun handleAuthorizationResult(intent: Intent?) {
        try {
            val result = authClient.getAuthorizationResultFromIntent(intent)
            _accessToken.value = result.accessToken
            Log.d("klima", "handleAuthorizationResult: result.accessToken: ${result.accessToken}")
        } catch (e: Exception) {
            Log.d("klima", "handleAuthorizationResult: ERROR: ${e.message}")
            _syncStatus.value = "Authorization failed: ${e.message}"
        }
    }

    // -----------------------------
    // Sign Out / Sign Off
    // -----------------------------
    fun signOut() {
        viewModelScope.launch {
            try {
                signOff()
//                authClient.clearCredentialState()
                _accessToken.value = null
                _isSignedIn.value = false
                _userEmail.value = null
                _syncStatus.value = "Signed out"
            } catch (e: Exception) {
                _syncStatus.value = "Sign out failed: ${e.message}"
            }
        }
    }

    // -----------------------------
    // Upload recipes to root Drive
    // -----------------------------
    fun uploadRecipesToDrive(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        _isLoading.value = true
        val token = _accessToken.value
        if (token == null) {
            _updateRecipes.value = true
            _syncStatus.value = "Not authorized. Requesting access..."
            // Trigger Drive authorization
            requestDriveAccess(launcher)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1) Get all recipes from local DB
                val recipes = recipeDao.getAllOnce() // <-- make sure this returns List<Recipe>
                val json = Json.encodeToString(recipes)

                // 2) Check if file exists on Drive
                val files = listFiles(token)
                val existingFile = files.find { it.name == "recipes.json" }

                if (existingFile != null) {
                    // Update file
                    updateFile(token, existingFile.id, json)
                } else {
                    // Create new file
                    createFile(token, "recipes.json", json)
                }

                _syncStatus.value = "Recipes uploaded successfully"
                _isLoading.value = false

            } catch (e: Exception) {
                _syncStatus.value = "Upload failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // -----------------------------
// Sync recipes from root Drive to app
// -----------------------------
    fun syncRecipesFromDrive() {
        val token = _accessToken.value
        _isLoading.value = true
        if (token == null) {
            _syncStatus.value = "Not authorized"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val files = listFiles(token)
                val recipesFile = files.find { it.name == "recipes.json" }
                if (recipesFile != null) {
                    val json = downloadFile(token, recipesFile.id)
                    val recipes: List<Recipe> = Json.decodeFromString(json)

                    // Save to local DB
                    for (recipe in recipes) {
                        val exists =
                            recipeDao.getRecipeByTitle(recipe.title, recipe.category) != null
                        if (!exists) {
                            recipeDao.insert(recipe)
                        }
                    }

                    _syncStatus.value = "Sync completed: ${recipes.size} recipes"
                } else {
                    _syncStatus.value = "No recipes.json found on Drive"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _syncStatus.value = "Sync failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // -----------------------------
// Drive REST API helpers
// -----------------------------
    private suspend fun listFiles(token: String): List<DriveFileMeta> {
        val url = "https://www.googleapis.com/drive/v3/files?q=mimeType='application/json'"
        val response = httpGet(url, token)
        val obj = JSONObject(response)
        val arr = obj.optJSONArray("files") ?: JSONArray()
        val result = mutableListOf<DriveFileMeta>()
        for (i in 0 until arr.length()) {
            val f = arr.getJSONObject(i)
            result.add(DriveFileMeta(f.getString("id"), f.getString("name")))
        }
        return result
    }

    private fun downloadFile(token: String, fileId: String): String {
        val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
        return httpGet(url, token)
    }

    private fun httpGet(url: String, token: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.requestMethod = "GET"
        return conn.inputStream.bufferedReader().readText()
    }

    private fun createFile(token: String, name: String, content: String) {
        val url = URL("https://www.googleapis.com/drive/v3/files")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true

        val body = JSONObject()
            .put("name", name)
            .put("mimeType", "application/json")
            .toString()

        conn.outputStream.use { it.write(body.toByteArray()) }

        // Upload file content
        val fileContentUrl = "https://www.googleapis.com/upload/drive/v3/files?uploadType=media"
        val uploadConn = URL(fileContentUrl).openConnection() as HttpURLConnection
        uploadConn.requestMethod = "PATCH"
        uploadConn.setRequestProperty("Authorization", "Bearer $token")
        uploadConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        uploadConn.doOutput = true
        uploadConn.outputStream.use { it.write(content.toByteArray()) }
        uploadConn.inputStream.close()
    }

    private fun updateFile(token: String, fileId: String, content: String) {
        val url = URL("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "PATCH"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true
        conn.outputStream.use { it.write(content.toByteArray()) }
        conn.inputStream.close()
    }
}

data class DriveFileMeta(val id: String, val name: String)
package com.example.onlythefam.network

import android.util.Log
import com.example.onlythefam.GlobalVariables
import com.example.onlythefam.model.UserBloodType
import com.example.onlythefam.model.UserInfo
import com.example.onlythefam.model.UserPersonal
import com.example.onlythefam.model.UserAllergies
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class UserNetworkFacade {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    fun close(){
        client.close()
    }

    private suspend fun httpRequest(method: String, endpoint: String, body: Any? = null): HttpResponse? {
        val address = "http://${GlobalVariables.localIP}:5050/" + endpoint
        return when (method.uppercase()) {
            "GET" -> {
                withContext(Dispatchers.IO) {
                    client.get(address) {
                        contentType(ContentType.Application.Json)
                    }
                }
            }
            "POST" -> {
                client.post(address) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
            "PUT" -> {
                client.put(address) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
            else -> {
                Log.d("User Network", "Invalid Request: $method, No Operation")
                null
            }
        }
    }

    suspend fun getUserInfo(userId: String): UserInfo? {
        return try{
            val response = httpRequest("get", "getUserInfo?userID=$userId")
            if (response?.status == HttpStatusCode.OK){
                Json.decodeFromString<UserInfo>(response.bodyAsText())
            } else{
                null
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            null
        }
        finally {
            close()
        }
    }

    suspend fun getUserAllergies(userId: String): String {
        return try{
            val response = httpRequest("get", "getAllergies?userID=$userId")
            val jsonArrayString = response?.bodyAsText()
            val allergiesList: List<String> = Json.decodeFromString(jsonArrayString!!)
            allergiesList.joinToString(separator = ", ")
        }
        catch (e: Exception) {
            e.printStackTrace()
            ""
        } finally {
            close()
        }
    }

    suspend fun getUserHealthFacts(userId: String): String {
        return try{
            val response = httpRequest("get", "getHealthInfo?userID=$userId")
            val jsonArrayString = response?.bodyAsText()
            val healthInfoList: List<String> = Json.decodeFromString(jsonArrayString!!)
            healthInfoList.joinToString(separator = ", ")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        } finally {
            close()
        }
    }

    fun updateUserName(userId: String, name: String, coroutineScope: CoroutineScope){
        coroutineScope.launch {
            try{
                httpRequest("put", "updateName", UserPersonal(userId, name, ""))
            }
            catch (e: Exception) {
                e.printStackTrace()
            } finally {
                close()
            }
        }
    }

    fun updateUserDob(userId: String, dob: String, coroutineScope: CoroutineScope){
        coroutineScope.launch {
            try {
                httpRequest("put", "updateDob", UserPersonal(userId, "", dob))
            }
            catch (e: Exception) {
                e.printStackTrace()
            } finally {
                close()
            }
        }
    }

    fun addUserAllergies(userId: String, allergiesList: List<String>, coroutineScope: CoroutineScope){
        coroutineScope.launch {
            try {
                httpRequest("post", "addAllergies", UserAllergies(userId, allergiesList))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            close()
        }
    }

    fun changeUserBloodType(userId: String, bloodType: String, coroutineScope: CoroutineScope){
        coroutineScope.launch {
            try{
                httpRequest("put", "updateBloodType", UserBloodType(userId, bloodType))
            }
            catch (e: Exception) {
                e.printStackTrace()
            } finally {
                close()
            }
        }
    }

}
package com.example.gruya.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.gruya.data.mapper.toDomain
import com.example.gruya.data.remote.dtos.request.CreateVehicleRequest
import com.example.gruya.data.remote.dtos.request.UpdateVehicleRequest
import com.example.gruya.data.remote.dtos.response.VehicleResponse
import com.example.gruya.data.service.VehicleService
import com.example.gruya.domain.model.Vehicle
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class VehicleRepository @Inject constructor(
    private val vehicleService: VehicleService,
    @ApplicationContext private val context: Context
) {
    suspend fun listAll(): List<Vehicle>{
        val response = vehicleService.getAll()
        if (response.isSuccessful) return response.body()!!.toDomain()
        else Log.d("API_ERROR", "${response.code().toString()} \n ${response.message()}")
        return emptyList()
    }

    suspend fun getById(id: Int): Vehicle? {
        val response = vehicleService.getById(id)
        if (response.isSuccessful) return response.body()?.toDomain()
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return null
    }

    suspend fun create(request: CreateVehicleRequest): Vehicle? {
        val typePart = request.type.name.toRequestBody("text/plain".toMediaTypeOrNull())
        val platePart = request.licensePlate.toRequestBody("text/plain".toMediaTypeOrNull())
        val brandPart = request.brand.toRequestBody("text/plain".toMediaTypeOrNull())
        val modelPart = request.model.toRequestBody("text/plain".toMediaTypeOrNull())
        val insurancePart = request.insurance.toRequestBody("text/plain".toMediaTypeOrNull())
        val colorPart = request.color.toRequestBody("text/plain".toMediaTypeOrNull())
        
        val imagePart = request.imageUrl?.let { prepareImagePart(it) }

        val response = vehicleService.create(
            typePart, platePart, brandPart, modelPart, insurancePart, colorPart, imagePart
        )
        if (response.isSuccessful) return response.body()?.toDomain()
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return null
    }

    suspend fun update(id: Int, request: UpdateVehicleRequest): Vehicle? {
        val typePart = request.type.name.toRequestBody("text/plain".toMediaTypeOrNull())
        val platePart = request.licensePlate.toRequestBody("text/plain".toMediaTypeOrNull())
        val brandPart = request.brand.toRequestBody("text/plain".toMediaTypeOrNull())
        val modelPart = request.model.toRequestBody("text/plain".toMediaTypeOrNull())
        val insurancePart = request.insurance.toRequestBody("text/plain".toMediaTypeOrNull())
        val colorPart = request.color.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = request.imageUrl?.let { prepareImagePart(it) }

        val response = vehicleService.update(
            id, typePart, platePart, brandPart, modelPart, insurancePart, colorPart, imagePart
        )
        if (response.isSuccessful) return response.body()?.toDomain()
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return null
    }

    private fun prepareImagePart(uriString: String): MultipartBody.Part? {
        if (uriString.startsWith("http")) return null // Ya es una URL remota
        
        return try {
            val uri = Uri.parse(uriString)
            val file = getFileFromUri(uri) ?: return null
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Error preparando imagen", e)
            null
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "temp_vehicle_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return file
    }

    suspend fun delete(id: Int): Boolean {
        val response = vehicleService.delete(id)
        if (response.isSuccessful) return true
        else Log.d("API_ERROR", "${response.code()} \n ${response.message()}")
        return false
    }
}

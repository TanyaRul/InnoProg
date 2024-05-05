package com.innoprog.android.network.data

import com.innoprog.android.feature.profile.editingprofile.data.BodyResponse
import com.innoprog.android.feature.profile.editingprofile.data.CompanyBodyResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface ApiService {
    @POST
    fun post(@Url url: String, @Body body: Any, @HeaderMap headers: Map<String, String>): Call<ResponseBody>

    @GET
    fun get(@Url url: String, @HeaderMap headers: Map<String, String>): Call<ResponseBody>

    @PUT("/v1/profile")
    suspend fun editProfile(): BodyResponse

    @PUT("/v1/profile/company")
    suspend fun editProfileCompany(): CompanyBodyResponse
}

package com.jivosite.sdk.model.pojo.mediaimport com.jivosite.sdk.model.pojo.response.Responseimport com.squareup.moshi.Jsonimport com.squareup.moshi.JsonClass/** * Created on 30.10.2021. * * @author Alexander Tavtorkin (av.tavtorkin@gmail.com) */@JsonClass(generateAdapter = true)data class MediaSignResponse(    @Json(name = "sign")    val sign: String? = null,    @Json(name = "ts")    val ts: Long? = null,    @Json(name = "url")    val host: String? = null,    @Json(name = "metadata")    val metadata: String? = null,    @Json(name = "ok")    override val isOk: Boolean = false,    @Json(name = "error_list")    override val errorList: List<String>? = null,) : Response()
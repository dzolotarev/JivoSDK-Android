package com.jivosite.sdk.network.resourceimport androidx.annotation.MainThreadimport androidx.lifecycle.LiveDataimport androidx.lifecycle.MediatorLiveDataimport com.jivosite.sdk.network.response.ApiResponseimport com.jivosite.sdk.support.async.Schedulersimport java.util.*typealias HeadApiCall = () -> LiveData<ApiResponse<Void>>typealias HeadResponseHandler<R> = (Map<String, List<String>>) -> R/** * Created on 2020-01-31. * * @author Alexandr Shibelev (av.shibelev@gmail.com) */abstract class HeadResource<R : Any>(schedulers: Schedulers) {    private val result = MediatorLiveData<Resource<R>>()    init {        schedulers.ui.execute {            setValue(Resource.loading())            val apiSource = createCall()            result.addSource(apiSource) { apiResponse ->                result.removeSource(apiSource)                if (apiResponse != null && apiResponse.isSuccessful) {                    val headers = apiResponse.headers ?: Collections.emptyMap()                    setValue(Resource.success(handleResponse(headers)))                } else {                    setValue(Resource.success(handleResponse(Collections.emptyMap())))                }            }        }    }    @MainThread    private fun setValue(newValue: Resource<R>) {        if (result.value != newValue) {            result.value = newValue        }    }    fun asLiveData() = result as LiveData<Resource<R>>    protected abstract fun createCall(): LiveData<ApiResponse<Void>>    protected abstract fun handleResponse(headers: Map<String, List<String>>): R    class Builder<R : Any>(private val schedulers: Schedulers) {        private var apiCall: HeadApiCall? = null        private var responseHandler: HeadResponseHandler<R>? = null        fun apiCall(call: HeadApiCall): Builder<R> = apply { apiCall = call }        fun handleResponse(call: HeadResponseHandler<R>): Builder<R> = apply { responseHandler = call }        fun build(): HeadResource<R> {            return object : HeadResource<R>(schedulers) {                override fun createCall(): LiveData<ApiResponse<Void>> = requireNotNull(apiCall) {                    "You need to declare apiCall method"                }.invoke()                override fun handleResponse(headers: Map<String, List<String>>): R = requireNotNull(responseHandler) {                    "You need to declare handleResponse method"                }.invoke(headers)            }        }    }}
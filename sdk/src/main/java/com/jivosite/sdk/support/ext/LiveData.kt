package com.jivosite.sdk.support.extimport androidx.lifecycle.LiveDataimport androidx.lifecycle.Observerimport com.jivosite.sdk.network.resource.Resourceimport com.jivosite.sdk.network.resource.ResourceHandler/** * Created on 23.11.2020. * * @author Alexander Tavtorkin (av.tavtorkin@gmail.com) */inline fun <reified T> LiveData<T>.requireValue(): T {    return this.value ?: throw IllegalStateException("Value is null for ${this.javaClass.simpleName}")}inline fun <reified T> LiveData<Resource<T>>.loadSilently(noinline progress: ((Boolean) -> Unit)? = null) {    val observer = object : Observer<Resource<T>> {        override fun onChanged(t: Resource<T>?) {            ResourceHandler.of(t)                .progress { progress?.invoke(it) }                .result { this@loadSilently.removeObserver(this) }                .error { this@loadSilently.removeObserver(this) }                .handle()        }    }    observeForever(observer)}fun <T> LiveData<Resource<T>>.loadSilentlyResource(handler: ResourceHandler<T>.() -> Unit) {    val observer = object : Observer<Resource<T>> {        override fun onChanged(t: Resource<T>?) {            val resourceHandler = ResourceHandler.of(t)            handler(resourceHandler)            ResourceHandler.of(t)                .result { this@loadSilentlyResource.removeObserver(this) }                .error { this@loadSilentlyResource.removeObserver(this) }                .handle()            resourceHandler.handle()        }    }    observeForever(observer)}inline fun <reified T> LiveData<Resource<T>>.loadSilently(    noinline progress: ((Boolean) -> Unit)? = null,    noinline result: ((T) -> Unit)? = null) {    val observer = object : Observer<Resource<T>> {        override fun onChanged(t: Resource<T>?) {            ResourceHandler.of(t)                .progress { progress?.invoke(it) }                .result {                    this@loadSilently.removeObserver(this)                    result?.invoke(it)                }                .error { this@loadSilently.removeObserver(this) }                .handle()        }    }    observeForever(observer)}
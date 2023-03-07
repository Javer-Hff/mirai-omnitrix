package org.hff.miraiomnitrix.utils

import org.hff.miraiomnitrix.config.HttpProperties
import org.hff.miraiomnitrix.exception.MyException
import org.hff.miraiomnitrix.utils.SpringUtil.getBean
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object HttpUtil {
    private val httpProperties = getBean(HttpProperties::class)

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .version(HttpClient.Version.HTTP_1_1)
        .build()
    private var proxyClient: HttpClient? = null

    init {
        if (httpProperties != null) {
            val (host, port) = httpProperties.proxy
            if (host != null && port != null) {
                proxyClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .proxy(ProxySelector.of(InetSocketAddress(host, port)))
                    .build()
            }
        }
    }

    fun getString(url: String, headers: Map<String, String>? = null): String {
        val res = httpClient.send(requestGet(url, headers), HttpResponse.BodyHandlers.ofString())
        return tryGetRightRes(res)
    }

    fun getStringByProxy(url: String): String {
        val res = proxyClient?.send(requestGet(url), HttpResponse.BodyHandlers.ofString()) ?: throw RuntimeException()
        return tryGetRightRes(res)
    }

    fun getStringByProxy(url: String, headers: Map<String, String>): String {
        val res = proxyClient?.send(requestGet(url, headers), HttpResponse.BodyHandlers.ofString())
            ?: throw RuntimeException()
        return tryGetRightRes(res)
    }

    fun getInputStream(url: String): InputStream {
        val res = httpClient.send(requestGet(url), HttpResponse.BodyHandlers.ofInputStream())
        return tryGetRightRes(res)
    }

    fun getInputStreamByProxy(url: String): InputStream {
        val res = proxyClient?.send(requestGet(url), HttpResponse.BodyHandlers.ofInputStream())
        return tryGetRightRes(res)
    }

    fun postString(url: String, data: Any): String {
        val res = httpClient.send(requestPost(url, data), HttpResponse.BodyHandlers.ofString())
        return tryGetRightRes(res)
    }

    fun postStringByProxy( url: String, data: Any, headers: Map<String, String>? = null ): String {
        val res = proxyClient?.send(requestPost(url, data, headers), HttpResponse.BodyHandlers.ofString())
        return tryGetRightRes(res)
    }

    private fun requestGetBuilder(url: String) = HttpRequest.newBuilder(URI.create(url)).GET()

    private fun requestGet(url: String, headers: Map<String, String>? = null) =
        if (headers == null) {
            requestGetBuilder(url).build()
        } else {
            val builder = requestGetBuilder(url)
            headers.forEach { (name, value) -> builder.setHeader(name, value) }
            builder.build()
        }

    private fun requestPostBuilder(url: String, data: Any) =
        HttpRequest.newBuilder(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.parse(data)))
            .header("Content-Type", "application/json")

    private fun requestPost(url: String, data: Any, headers: Map<String, String>?) =
        if (headers == null) {
            requestPostBuilder(url, data).build()
        } else {
            val builder = requestPostBuilder(url, data)
            headers.forEach { (name, value) -> builder.setHeader(name, value) }
            builder.build()
        }

    private fun requestPost(url: String, data: Any) =
        HttpRequest.newBuilder(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.parse(data)))
            .header("Content-Type", "application/json")
            .build()

    private fun <T : Any> tryGetRightRes(res: HttpResponse<T>?): T {
        if (res == null) throw MyException("无代理配置")
        if (res.statusCode() != 200) {
            println("错误码: ${res.statusCode()}，错误信息: ${res.body()}")
            throw MyException("请求失败")
        }
        return res.body()
    }

}
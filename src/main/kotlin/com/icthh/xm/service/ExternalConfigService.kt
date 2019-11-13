package com.icthh.xm.service

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.icthh.xm.actions.settings.EnvironmentSettings
import com.icthh.xm.actions.shared.showMessage
import com.icthh.xm.actions.shared.showNotification
import com.icthh.xm.utils.readTextAndClose
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.MessageType.INFO
import io.netty.handler.codec.http.HttpHeaders.addHeader
import org.apache.commons.net.nntp.NNTPCommand.POST
import org.apache.http.HttpHeaders.AUTHORIZATION
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.client.fluent.Request.Get
import org.apache.http.client.fluent.Request.Post
import org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED
import org.apache.http.message.BasicNameValuePair
import org.bouncycastle.crypto.tls.ConnectionEnd
import org.apache.http.HttpEntity
import org.apache.http.NameValuePair
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


class ExternalConfigService {

    val objectMapper = ObjectMapper()
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(KotlinModule())

    fun getConfigFile(env: EnvironmentSettings, path: String): String {
        val baseUrl = env.xmUrl
        val accessToken = getToken(env)

        val response = Get(baseUrl + "/config/api/config/tenants${path}?version=${env.version?:""}")
            .addHeader(AUTHORIZATION, "bearer $accessToken")
            .execute()

        return response.handleResponse {
            val returnResponse = response.returnResponse()
            if (returnResponse.statusLine.statusCode == 404) {
                throw NotFoundException();
            }
            returnResponse.entity.content.readTextAndClose()
        }
    }

    fun getToken(env: EnvironmentSettings): String {
        val content = Post(env.xmUrl + "/uaa/oauth/token")
            .bodyForm(
                "grant_type" to "password",
                "username" to env.xmSuperAdminLogin,
                "password" to env.xmSuperAdminPassword
            )
            .addHeader(AUTHORIZATION, "Basic d2ViYXBwOndlYmFwcA==")
            .addHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED.toString()).execute().returnContent().asString()

        val tokenResponse = objectMapper.readValue<TokenResponse>(content)
        return tokenResponse.access_token
    }

    infix fun String.to(value: String) = BasicNameValuePair(this, value)

    fun refresh(env: EnvironmentSettings) {
        Post(env.xmUrl + "/config/api/profile/refresh")
            .addHeader(AUTHORIZATION, "bearer ${getToken(env)}")
            .execute().returnResponse()
    }

    fun getCurrentVersion(env: EnvironmentSettings): String {
        val accessToken = getToken(env)
        return Get(env.xmUrl + "/config/api/version")
            .addHeader(AUTHORIZATION, "bearer $accessToken")
            .execute().returnContent().asString()
    }

    fun updateInMemory(project: Project, env: EnvironmentSettings, files: Map<String, InputStream?>) {
        val httpClient = HttpClients.createDefault()
        val uploadFile = HttpPost("${env.xmUrl}/config/api/inmemory/config")
        val builder = MultipartEntityBuilder.create()
        files.forEach {
            builder.addBinaryBody(
                "files",
                it.value,
                ContentType.APPLICATION_OCTET_STREAM,
                it.key
            )
        }

        val multipart = builder.build()
        uploadFile.entity = multipart
        uploadFile.addHeader(AUTHORIZATION, "bearer ${getToken(env)}")
        val response = httpClient.execute(uploadFile)
        if (response.statusLine.statusCode != 200) {
            project.showNotification("Refresh", "Error update configurations", NotificationType.ERROR) {
                "${response.statusLine.statusCode} ${response.statusLine.reasonPhrase}"
            }
            throw RuntimeException("Error send status code")
        } else {
            project.showMessage(INFO) {
                "Configs successfully update"
            }
        }
    }

}

data class TokenResponse(val access_token: String)

class NotFoundException: RuntimeException()

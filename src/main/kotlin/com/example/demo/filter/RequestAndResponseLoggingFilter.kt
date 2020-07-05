package com.example.demo.filter

import com.example.demo.interceptor.RequestAndResponseLoggingInterceptor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.UnsupportedEncodingException
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RequestAndResponseLoggingFilter : OncePerRequestFilter() {
    companion object {
        private val log = LoggerFactory.getLogger(RequestAndResponseLoggingInterceptor::class.java)
        private val mapper = jacksonObjectMapper()

        private val VISIBLE_TYPES = listOf(
            MediaType.valueOf("text/*"),
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.valueOf("application/*+json"),
            MediaType.valueOf("application/*+xml")
        )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response)
        } else {
            doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain)
        }
    }

    private fun doFilterWrapped(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } finally {
            val endpoint = logRequest(request)
            logResponse(response, endpoint)
            response.copyBodyToResponse()
        }
    }

    private fun logRequest(request: ContentCachingRequestWrapper): String {
        val queryString = request.queryString?.let { "?$it" } ?: ""
        val endpoint = "${request.method} ${request.requestURI}$queryString"

        val requestHeaders = request.headerNames.asSequence().associateWith { headerName ->
            request.getHeader(headerName)
        }

        val content = request.contentAsByteArray
        val requestBody = if (content.isNotEmpty()) {
            getContentByte(content, request.contentType, request.characterEncoding)
        } else ""

        val json = mapper.writeValueAsString(
            object {
                val endpoint = endpoint
                val request = object {
                    val header = requestHeaders
                    val body: Any? = if (requestBody.isNotBlank() && request.contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                        mapper.readTree(requestBody)
                    } else {
                        requestBody
                    }
                }
            }
        )

        log.info(json)
        return endpoint
    }

    private fun logResponse(response: ContentCachingResponseWrapper, endpoint: String) {
        val status = response.status
        val statusPhrase = HttpStatus.valueOf(status).reasonPhrase
        val responseHeaders = response.headerNames.asSequence().associateWith { headerName ->
            response.getHeader(headerName)
        }

        val content = response.contentAsByteArray
        val responseBody = if (content.isNotEmpty()) {
            getContentByte(response.contentAsByteArray, response.contentType, response.characterEncoding)
        } else ""

        val json = mapper.writeValueAsString(
            object {
                val endpoint = endpoint
                val response = object {
                    val status = "$status $statusPhrase"
                    val header = responseHeaders
                    val body: Any? = if (responseBody.isNotBlank() && response.contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                        mapper.readTree(responseBody)
                    } else {
                        responseBody
                    }
                }
            }
        )

        log.info(json)
    }

    private fun getContentByte(content: ByteArray, contentType: String, contentEncoding: String): String {
        val mediaType = MediaType.valueOf(contentType)
        val visible = VISIBLE_TYPES.stream().anyMatch { visibleType -> visibleType.includes(mediaType) }

        return if (visible) {
            var contentStr = ""
            contentStr += try {
                String(content, charset(contentEncoding))
            } catch (e: UnsupportedEncodingException) {
                val contentSize = content.size
                "$contentSize bytes content"
            }
            contentStr
        } else {
            val contentSize = content.size
            "$contentSize bytes content"
        }
    }

    private fun wrapRequest(request: HttpServletRequest): ContentCachingRequestWrapper {
        return request as? ContentCachingRequestWrapper ?: ContentCachingRequestWrapper(request)
    }

    private fun wrapResponse(response: HttpServletResponse): ContentCachingResponseWrapper {
        return response as? ContentCachingResponseWrapper ?: ContentCachingResponseWrapper(response)
    }
}

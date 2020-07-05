package com.example.demo.config

import com.example.demo.filter.RequestAndResponseLoggingFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfig {
    @Bean
    fun requestAndResponseLoggingFilter(): FilterRegistrationBean<RequestAndResponseLoggingFilter> {
        return FilterRegistrationBean(RequestAndResponseLoggingFilter())
            .apply {
                this.order = Int.MIN_VALUE
            }
    }
}

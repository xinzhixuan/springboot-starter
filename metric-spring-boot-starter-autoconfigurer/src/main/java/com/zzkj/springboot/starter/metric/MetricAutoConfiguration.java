package com.zzkj.springboot.starter.metric;

import com.codahale.metrics.MetricRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author xinzhixuan
 * @version V1.0
 * @date 2018/7/31 14:45
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(MetricUtil.class)
@EnableConfigurationProperties(MetricProperties.class)
@ComponentScan("com.zzkj.springboot.starter.metric")
public class MetricAutoConfiguration implements WebMvcConfigurer {

    @Autowired
    private MetricInterceptor metricInterceptor;
    @Autowired
    private MetricProperties metricProperties;

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public MetricUtil metricUtil(MetricRegistry metricRegistry) {
        return new MetricUtil(metricRegistry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(metricInterceptor);
        interceptorRegistration.addPathPatterns(StringUtils.isBlank(metricProperties.getFilter()) ? "/**" : metricProperties.getFilter());
        if (metricProperties.getExcludeFilters() != null) {
            interceptorRegistration.excludePathPatterns(metricProperties.getExcludeFilters());
        }
    }
}


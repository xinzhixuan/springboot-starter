package com.zzkj.springboot.starter.metric;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author xinzhixuan
 * @version V1.0
 * @date 2018/8/1 11:40
 */
@Component
public class MetricInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(MetricInterceptor.class);

    private ThreadLocal<Timer.Context> threadLocal = new ThreadLocal<>();

    @Autowired
    private MetricRegistry metricRegistry;

    /*public MetricInterceptor(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }*/

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //logger.info("===============MetricInterceptor======================preHandle==================================");

        try {
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                Method method = handlerMethod.getMethod();
                Metric metric = method.getAnnotation(Metric.class);
                String metricName = null;
                if (metric != null) {
                    metricName = metric.value();
                } else {
                    Class<?> beanType = handlerMethod.getBeanType();
                    metric = beanType.getAnnotation(Metric.class);
                    if (metric != null) {
                        metricName = beanType.getSimpleName() + "_" + method.getName();
                    }
                }
                if (metricName != null) {
                    Timer.Context timer = metricRegistry.timer(metricName).time();
                    threadLocal.set(timer);
                    //logger.info("===============MetricInterceptor======================preHandle==================================" + timer);
                }
            }
        } catch (Exception e) {
            logger.error("开始监控异常：" + e);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        try {
            Timer.Context timer = threadLocal.get();
            if (timer != null) {
                timer.stop();
            }
            threadLocal.remove();
        } catch (Exception e) {
            logger.error("结束监控异常：", e);
        }
        //logger.info("===============MetricInterceptor======================postHandle==========="+timer+"=======================");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //logger.info("===============MetricInterceptor======================afterCompletion==================================");
    }
}

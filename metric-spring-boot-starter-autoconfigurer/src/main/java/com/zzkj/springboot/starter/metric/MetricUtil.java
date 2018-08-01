package com.zzkj.springboot.starter.metric;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.protocols.InfluxdbProtocols;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author xinzhixuan
 * @version V1.0
 * @date 2017/12/8 14:44
 */
@Component
public class MetricUtil {
    private static final Logger logger = LoggerFactory.getLogger(MetricUtil.class);

    private MetricRegistry metricRegistry;

    @Autowired
    private MetricProperties metricProperties;

    public MetricUtil(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    /**
     * 需要监控的timer的名称
     */
    private Set<String> timerSet = new HashSet<>();
    /**
     * 指定的basePackage下的所有class集合
     */
    private Set<Class<?>> classSet = new HashSet<>();

    private ScheduledReporter scheduledReporter;


    @PostConstruct
    public void init() {
        if (!metricProperties.isEnable()) {
            return;
        }
        // 扫描包
        getPackageClassSet(metricProperties.getMetricScanPackage());
        // 构建所有需要监控的metric的名称
        buildTimerName();
        // 注解监控
        registerTimer();

        // 启动监控
        scheduledReporter = getInfluxdbReporter();
        scheduledReporter.start(1, TimeUnit.SECONDS);
        logger.info(" metric start.........");
    }

    @PreDestroy
    public void destroy() {
        if (scheduledReporter != null) {
            scheduledReporter.close();
            logger.info(" metric stop.........");
        }
    }

    private ScheduledReporter getInfluxdbReporter() {
        return InfluxdbReporter.forRegistry(metricRegistry)
                               .protocol(InfluxdbProtocols.http(metricProperties.getInfluxdbIp(), metricProperties.getInfluxdbPort(), metricProperties.getInfluxdbUsername(),
                                       metricProperties.getInfluxdbPassword(), metricProperties.getInfluxdbDatabase()))
                               .convertRatesTo(TimeUnit.SECONDS)
                               .convertDurationsTo(TimeUnit.MILLISECONDS)
                               .filter(MetricFilter.ALL)
                               .skipIdleMetrics(false)
                               .build();
    }

    private void registerTimer() {
        for (String timerName : timerSet) {
            metricRegistry.timer(timerName);
        }
    }

    private void buildTimerName() {
        // 判断当前class是否有@Metric注解
        for (Class clazz : classSet) {
            Annotation clazzAnnotation = clazz.getAnnotation(Metric.class);
            if (clazzAnnotation != null) {
                // 需要监控所有的方法
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getModifiers() == Modifier.PUBLIC) {
                        Metric metric = method.getAnnotation(Metric.class);
                        if (metric != null) {
                            // 需要使用method上面的@Metric名称
                            timerSet.add(metric.value());
                        } else {
                            // 直接使用类名加方法名做为监控key
                            timerSet.add(clazz.getSimpleName() + "_" + method.getName());
                        }
                    }
                }
            } else {
                // 判断类中的方法是否需要有@Metric
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getModifiers() == Modifier.PUBLIC) {
                        Metric metric = method.getAnnotation(Metric.class);
                        if (metric != null) {
                            // 需要使用method上面的自定名称
                            timerSet.add(metric.value());
                        }
                    }
                }
            }
        }
    }

    /**
     * 或者指定包下所有类
     * @param basePackage
     * @return
     */
    public Set<Class<?>> getPackageClassSet(String basePackage) {
        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(basePackage.replace(".", "/"));
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if(url != null) {
                    //String packagePath = url.getPath().replaceAll("%20",""); this is a bug
                    String packagePath = URLDecoder.decode(url.getPath(), "UTF-8");
                    addClass(classSet, packagePath, basePackage);
                }
            }
        } catch (Exception e) {
            logger.error("get packageAllClass fail", e);
            throw new RuntimeException(e);
        }
        return classSet;
    }

    /**
     * add class
     * @param classSet 集合
     * @param packagePath 包路径
     * @param packageName 包名
     */
    private static void addClass(Set<Class<?>> classSet, String packagePath, String packageName) {
        File[] files = new File(packagePath).listFiles(file -> (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory());
        if ( files == null ) {
            return;
        }
        for(File file : files) {
            String fileName = file.getName();
            if(file.isFile()) {
                String className = fileName.substring(0, fileName.lastIndexOf("."));
                if(StringUtils.isNotBlank(packageName)){
                    className = packageName+"."+className;
                }
                doAddClass(classSet, className);
            }else {
                String subPackagePath = fileName;
                if(StringUtils.isNotBlank(packagePath)) {
                    subPackagePath = packagePath+"/"+fileName;
                }
                String subPackageName = fileName;
                if(StringUtils.isNotBlank(packageName)) {
                    subPackageName = packageName + "." +fileName;
                }
                addClass(classSet,subPackagePath,subPackageName);
            }
        }
    }

    /**
     * 添加class
     * @param classSet
     * @param className
     */
    private static void doAddClass(Set<Class<?>> classSet, String className) {
        Class<?> clazz = loadClass(className,false);
        classSet.add(clazz);
    }

    /**
     * 加载指定类
     * @param className  类名
     * @param isInit  是否必须初始化
     * @return
     */
    public static Class<?> loadClass(String className, boolean isInit) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className, isInit, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            logger.error("load class fail",e);
            throw new RuntimeException(e);
        }
        return clazz;
    }


    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public MetricProperties getMetricProperties() {
        return metricProperties;
    }

    public void setMetricProperties(MetricProperties metricProperties) {
        this.metricProperties = metricProperties;
    }
}

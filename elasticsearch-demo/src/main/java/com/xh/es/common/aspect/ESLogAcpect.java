package com.xh.es.common.aspect;

import com.xh.es.common.annotation.ExecutionMethod;
import com.xh.es.common.constant.ElasticSearchConst;
import com.xh.es.common.properties.ElasticSearchProperties;
import com.xh.es.common.util.ElasticSearchUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author H.Yang
 * @date 2022/11/23
 */
@Component
@Aspect
@Slf4j
public class ESLogAcpect {

    private ElasticSearchProperties elasticSearchProperties;

    public ESLogAcpect(ElasticSearchProperties elasticSearchProperties) {
        this.elasticSearchProperties = elasticSearchProperties;
    }

    /**
     * 定义切入点
     */
    @Pointcut("@annotation(com.xh.es.common.annotation.ExecutionMethod)")
    public void esLog() {
    }

    /**
     * 前置通知：在连接点之前执行的通知
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before("esLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        ExecutionMethod annotation = signature.getMethod().getAnnotation(ExecutionMethod.class);
        ElasticSearchUtil.esOperationLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), annotation, "开始");
    }

    @After(value = "esLog()")
    public void doAfterReturning(JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        ExecutionMethod annotation = signature.getMethod().getAnnotation(ExecutionMethod.class);
        ElasticSearchUtil.esOperationLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), annotation, "结束");
    }
}

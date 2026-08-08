package com.autoflow.infrastructure.config;

import com.autoflow.application.transaction.TransactionalUseCase;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Method;

@Component
public class TransactionalUseCaseBeanPostProcessor implements BeanPostProcessor {
    private final PlatformTransactionManager transactionManager;

    public TransactionalUseCaseBeanPostProcessor(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        if (!hasTransactionalMethod(targetClass)) {
            return bean;
        }

        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice((MethodInterceptor) invocation -> {
            Method method = targetClass.getMethod(
                    invocation.getMethod().getName(), invocation.getMethod().getParameterTypes());
            if (AnnotationUtils.findAnnotation(method, TransactionalUseCase.class) == null) {
                return invocation.proceed();
            }
            return new TransactionTemplate(transactionManager).execute(status -> {
                try {
                    return invocation.proceed();
                } catch (RuntimeException | Error exception) {
                    status.setRollbackOnly();
                    throw exception;
                } catch (Throwable exception) {
                    status.setRollbackOnly();
                    throw new IllegalStateException(exception);
                }
            });
        });
        return proxyFactory.getProxy();
    }

    private boolean hasTransactionalMethod(Class<?> type) {
        for (Method method : type.getMethods()) {
            if (AnnotationUtils.findAnnotation(method, TransactionalUseCase.class) != null) {
                return true;
            }
        }
        return false;
    }
}

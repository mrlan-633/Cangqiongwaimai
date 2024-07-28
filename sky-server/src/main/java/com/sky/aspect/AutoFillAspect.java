package com.sky.aspect;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.sky.annotation.Autofill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {


    @Pointcut("execution(* com.sky.mapper.*.*(..))  && @annotation(com.sky.annotation.Autofill)")
    public void autoFillPointCut() {
    }

    @Before("autoFillPointCut()")
    public void autofill(JoinPoint joinPoint) {
        log.info("开始进行公共字段的填充");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        Autofill autofill = signature.getMethod().getAnnotation(Autofill.class);//获得方法上的操作对象
        OperationType operationType = autofill.value();//获得操作类型
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();
        long currentId = BaseContext.getCurrentId();
        if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                  setUpdateUser.invoke(entity,currentId);
                    setUpdateTime.invoke(entity,now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (operationType == OperationType.INSERT) {

            try {  Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
                setCreateUser.invoke(entity,currentId);
                setCreateTime.invoke(entity,now);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }
}

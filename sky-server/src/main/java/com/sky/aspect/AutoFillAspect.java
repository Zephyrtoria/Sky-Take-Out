package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static com.sky.constant.AutoFillConstant.*;

/*
 * 自定义切面类，实现公共字段自动填充处理逻辑
 * */
@Aspect
@Component
@Slf4j
// 切面类：切入点 + 通知
// 注意该AOP对于insertBatch无法起效
public class AutoFillAspect {
    /*
     * 切入点
     * */
    // 满足在mapper包中的所有类的所有方法，任意参数 && 拥有@AutoFill的注解（由于@AutoFill指定了方法可用，所以实际上就是拥有@AutoFill的方法）
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    /*
     * 通知。根据业务类型选择通知类型：前置通知、后置通知、环绕通知、异常通知
     * */
    // 前置通知
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充...");
        // 为公共字段统一赋值
        // 1. 根据@AutoFill指定的不同数据库操作类型，来赋值不同的字段
        // 1.1 获取方法签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 1.2 获取方法上的注解对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        // 1.3 获取数据库操作类型
        OperationType operationType = autoFill.value(); // 这就是在注解中设置的值！

        // 2. 获取方法参数（即实体类），从而能够进行赋值
        // getArgs()获取所有元参数，要求约定将赋值的实体类放在第一个参数位置
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];

        // 3. 准备赋值数据
        Long userId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();

        // 4. 赋值
        // 注意ENUM类的比较
        // 使用反射进行赋值
        if (operationType == OperationType.INSERT) {
            try {
                // insert - createTime && createUser && updateTime && updateUser
                // 获取方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(SET_UPDATE_USER, Long.class);
                // 调用方法
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, userId);
                setUpdateUser.invoke(entity, userId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                // update - updateTime && updateUser;
                // 获取方法
                Method setUpdateTime = entity.getClass().getDeclaredMethod(SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(SET_UPDATE_USER, Long.class);
                // 调用方法
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, userId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

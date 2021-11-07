package group.yzhs.alarm.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/19 8:53
 */
@Component
@Aspect
@Order
@Slf4j
public class ExecuteCycle {

    @Pointcut("execution(* group.yzhs.alarm.service.JudgementService.judge())")
    public void trackJudgement() {

    }

    @Around("trackJudgement()")
    public Object staticJudgementTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Long startTime= System.currentTimeMillis();;
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
           log.error(throwable.getMessage(),throwable);
           throw throwable;
        }finally {
            Long endTime=System.currentTimeMillis();
            log.info("judgement cost time={} milli",endTime-startTime);
        }
    }

}

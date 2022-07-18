package group.yzhs.alarm.aspect;

import group.yzhs.alarm.listener.SessionListener;
import group.yzhs.alarm.mapper.impl.AlarmRuleMapperImp;
import group.yzhs.alarm.model.entity.AlarmRule;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
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


    /**
     * 报警处理时间耗时统计
     */
    @Around("trackJudgement()")
    public Object staticJudgementTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Long startTime = System.currentTimeMillis();
        ;
        try {
            Object res = joinPoint.proceed();
            return res;
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
            throw throwable;
        } finally {
            Long endTime = System.currentTimeMillis();
            log.info("judgement cost time={} milli", endTime - startTime);
        }
    }





}

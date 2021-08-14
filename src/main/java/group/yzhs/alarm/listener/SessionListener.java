package group.yzhs.alarm.listener;

import group.yzhs.alarm.constant.SessionContextEnum;
import group.yzhs.alarm.model.AlarmMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 9:19
 */
@Component
@WebListener
@Slf4j
@Getter
public class SessionListener implements HttpSessionListener {
    //session cache
    private Map<String, HttpSession> httpSessionMap=new ConcurrentHashMap<>();

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        log.debug("a new session has created.id={}",se.getSession().getId());
        //add alarm list and audio list， alarm-list：key=tag
        se.getSession().setAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode(),new ConcurrentHashMap<String, AlarmMessage>());
        //key=tag
        se.getSession().setAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode(),new ConcurrentHashMap<String, AlarmMessage>());
        //key=tag
        se.getSession().setAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOPUSHLASTTIME.getCode(), new ConcurrentHashMap<String, LocalDateTime>());
        httpSessionMap.put(se.getSession().getId(),se.getSession());

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        log.debug("a expired session has remove.id={}",se.getSession().getId());
        HttpSession httpSession=httpSessionMap.remove(se.getSession().getId());
        //help gc
        httpSession.removeAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
        httpSession.removeAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());

        se.getSession().removeAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOPUSHLASTTIME.getCode());
    }
}

package name.menghw.interceptor;

import name.menghw.MSLocalLock;
import name.menghw.data.LockStatus;
import name.menghw.data.domain.LockedDataInfo;
import name.menghw.strategy.StrategyComposite;
import name.menghw.tools.StringTool;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 拦截@Scheduled方法的执行
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
@Aspect
@Component
public class ScheduledAnnoInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StrategyComposite strategyExecutor;
    /**
     * 定义切入点
     */
    @Pointcut("@annotation(org.springframework.scheduling.annotation.Scheduled) || @annotation(org.springframework.scheduling.annotation.Schedules)")
    public void around() {}

    @Around("around()")
    public Object around(ProceedingJoinPoint pjoinPoint) throws Throwable{
        //目标方法
        String targetMethodName = pjoinPoint.getTarget().getClass().getName()
                + "." + pjoinPoint.getSignature().getName();
        //先获取本地锁
        MSLocalLock.lock(targetMethodName);
        //获取全局锁
        LockStatus globalLock = null;

        Object result = null;
        LockedDataInfo lockedDataInfo = new LockedDataInfo(targetMethodName);
        try {
            //锁定失败表示该方法被别的应用占用执行，需要跳过
            globalLock = strategyExecutor.lock(lockedDataInfo);
            if (globalLock == null) {
                logger.info("自动跳过[{}]定时方法", StringTool.shortMethod(targetMethodName));
                return result;
            }
            result = pjoinPoint.proceed();

        }catch (Exception e){
            logger.error("ScheduledAnnoInterceptor",e);
        }finally {
            //首次加锁，设置可以开始尝试释放锁标识
            if(globalLock == LockStatus.LOCK_FI){
                lockedDataInfo.setCanStartTryRelease(true);
            }
            //释放本地锁
            MSLocalLock.unLock(targetMethodName);
        }
        return result;
    }
}

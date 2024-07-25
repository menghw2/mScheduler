package name.menghw.interceptor;

import name.menghw.MSLocalLock;
import name.menghw.data.LockStatus;
import name.menghw.data.domain.LockedDataInfo;
import name.menghw.strategy.StrategyComposite;
import name.menghw.tools.StringTool;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MethodInvoker;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [基于spring管理的]quartz计划任务处理
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
@Service
public class QuartzSchedulerInterceptor {

    @Autowired(required = false)
    private List<Scheduler> schedulers;
    @Autowired
    private StrategyComposite strategyExecutor;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final ThreadLocal<LockStatus> VAR_GLOCK = new ThreadLocal<>();

    private static final ThreadLocal<LockedDataInfo> VAR_LDINFO = new ThreadLocal<>();

    private static final ThreadLocal<String> VAR_METHOD = new ThreadLocal<>();

    @PostConstruct
    public void init(){
        if(CollectionUtils.isEmpty(schedulers)){
            return;
        }

        AtomicInteger ac = new AtomicInteger(1);

        schedulers.forEach(sched -> {
            try {
                sched.getListenerManager().addJobListener(new JobListener() {
                    @Override
                    public String getName() {
                        return "QuartzSchedulerExecutor_JobListener" + ac.getAndIncrement();
                    }

                    @Override
                    public void jobToBeExecuted(JobExecutionContext context) {
                        MethodInvoker oriInvoker = (MethodInvoker)context.getMergedJobDataMap().get("methodInvoker");
                        String targetMethod = oriInvoker.getTargetClass().getName() + "." + oriInvoker.getTargetMethod();
                        //加锁实体
                        LockedDataInfo lockedDataInfo = new LockedDataInfo(targetMethod);
                        //获本地方法锁
                        MSLocalLock.lock(targetMethod);
                        VAR_METHOD.set(targetMethod);
                        //全局锁加锁是否成功
                        LockStatus globalLock = strategyExecutor.lock(lockedDataInfo);
                        //全局锁加锁成功
                        if (globalLock != null) {
                            VAR_GLOCK.set(globalLock);
                            VAR_LDINFO.set(lockedDataInfo);
                            return;
                        }
                        //全局锁获取失败表示：该方法被别的应用占用执行，需要跳过
                        MethodInvoker methodInvoker = new MethodInvoker();
                        methodInvoker.setTargetClass(QuartzSchedulerInterceptor.class);
                        methodInvoker.setTargetObject(new QuartzSchedulerInterceptor());
                        methodInvoker.setTargetMethod("laneChange");
                        methodInvoker.setArguments(targetMethod);
                        try {
                            methodInvoker.prepare();
                        } catch (Exception e) {
                            logger.error("methodInvoker.prepare异常",e);
                        }
                        //需要跳过的自动导流到一个空方法
                        context.getMergedJobDataMap().put("methodInvoker",methodInvoker);
                    }

                    @Override
                    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
                        //方法执行完，若之前加锁成功则设置可以开始尝试释放锁标识
                        if(VAR_GLOCK.get() == LockStatus.LOCK_FI
                                && VAR_LDINFO.get() != null){
                            VAR_LDINFO.get().setCanStartTryRelease(true);
                        }
                        VAR_GLOCK.remove();
                        VAR_LDINFO.remove();

                        //释放本地方法锁
                        MSLocalLock.unLock(VAR_METHOD.get());
                        VAR_METHOD.remove();
                    }

                    @Override
                    public void jobExecutionVetoed(JobExecutionContext context) {

                    }
                });
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        });
    }

    public void laneChange(String targetMethod){
        logger.info("自动跳过[{}]定时方法", StringTool.shortMethod(targetMethod));
    }
}

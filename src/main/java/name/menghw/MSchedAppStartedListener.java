package name.menghw;

import name.menghw.data.domain.LockedDataInfo;
import name.menghw.strategy.StrategyComposite;
import name.menghw.tools.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
@Component
public class MSchedAppStartedListener implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StrategyComposite strategyExecutor;
    @Autowired
    private Environment env;

    private static volatile Boolean isStopping = false;

    private final static List<String> effStore = Arrays.asList("redis","db");
    private final static List<String> effStrategy = Arrays.asList("sticky","random");

    @Override
    public void run(String... args) throws Exception {

        //check
        String store = env.getProperty("mScheduler.store");
        if (!StringTool.isEmpty(store) && !effStore.contains(store)) {
            throw  new RuntimeException("mScheduler 初始化失败，[数据存储方式]配置不合法，应为["+ StringTool.joinString(effStore,",")+"]中之一");
        }
        if (MSchedApplicationContext.dataOperator() == null) {
            throw  new RuntimeException("mScheduler 初始化失败，请检查数据存储方式是否正确配置...");
        }
        String strategy = env.getProperty("mScheduler.strategy");
        if (!StringTool.isEmpty(strategy) && !effStrategy.contains(strategy)) {
            throw  new RuntimeException("mScheduler 初始化失败，[策略]配置不合法，应为["+ StringTool.joinString(effStrategy,",")+"]中之一");
        }
        /**
         * * 用于设置钩子函数，当程序终止时释放锁
         * * 注意kill -9 不会触发，请使用kill [-15] pid 终止程序
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            isStopping = true;
            //释放所有锁
            strategyExecutor.releaseAll();
            logger.info("shutdownHook release all lock");
            try {
                Thread.sleep(1200L);
            } catch (InterruptedException e) {
            }
        }));

        //release lock thread
        ExecutorService releaseExecutor = new ThreadPoolExecutor(1, 8,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        new Thread(() -> {
            while (true){

                //程序正在停止则结束线程
                if (isStopping)
                    return;

                ConcurrentLinkedQueue<LockedDataInfo> tryReleaseQueue = MSchedApplicationContext.tryReleaseQueue();

                List<LockedDataInfo> tryReleaseList = new ArrayList<>();
                for (LockedDataInfo info : tryReleaseQueue) {
                    if(info.isCanStartTryRelease()){
                        tryReleaseList.add(info);
                    }
                }

                if(!CollectionUtils.isEmpty(tryReleaseList)){
                    CountDownLatch latch = new CountDownLatch(tryReleaseList.size());
                    Iterator<LockedDataInfo> iterator = tryReleaseList.iterator();
                    while(iterator.hasNext()){
                        LockedDataInfo lockInfo = iterator.next();
                        releaseExecutor.execute(()->{
                            //获取本地方法锁
                            MSLocalLock.lock(lockInfo.getMethod());
                            //获取本地方法锁成功后执行释放
                            try {
                                boolean released = strategyExecutor.release(lockInfo);
                                if(released){
                                    tryReleaseQueue.remove(lockInfo);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }finally {
                                //释放本地方法锁
                                MSLocalLock.unLock(lockInfo.getMethod());
                            }
                            latch.countDown();
                        });
                    }
                    // 等待全部完成
                    try {
                        latch.await();
                    } catch (InterruptedException e) {}
                }
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

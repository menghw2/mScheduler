package name.menghw.strategy;

import name.menghw.MSchedApplicationContext;
import name.menghw.data.LockStatus;
import name.menghw.data.domain.LockedDataInfo;
import name.menghw.data.opr.DataOperator;
import name.menghw.tools.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
@Component
public class StrategyComposite{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${mScheduler.strategy:sticky}")
    private String strategy;

    @Autowired
    private List<StrategyExecutor> executors;

    public LockStatus lock(LockedDataInfo lockInfo) {
        DataOperator operator = MSchedApplicationContext.dataOperator();

        for (StrategyExecutor executor : executors) {
            if(executor.support(strategy)){
                LockStatus status = executor.lockAndEnq(operator,lockInfo);
                if (status != null){
                    logger.info("[{}]{}...", StringTool.shortMethod(lockInfo.getMethod()),status == LockStatus.LOCK_FI?"加锁":"锁重入");
                }
                return status;
            }
        }
        return null;
    }

    public boolean release(LockedDataInfo lockInfo) {

        DataOperator operator = MSchedApplicationContext.dataOperator();

        for (StrategyExecutor executor : executors) {
            if(executor.support(strategy)){
                boolean released = executor.release(operator, lockInfo);
                if(released){
                    logger.info("释放[{}]的锁...",StringTool.shortMethod(lockInfo.getMethod()));
                }
                return released;
            }
        }
        return false;
    }

    public void releaseAll() {
        MSchedApplicationContext.dataOperator().releaseAll();
    }
}

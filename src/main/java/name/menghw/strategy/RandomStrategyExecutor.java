package name.menghw.strategy;

import name.menghw.data.domain.LockedDataInfo;
import name.menghw.data.opr.DataOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
@Component
public class RandomStrategyExecutor extends StrategyExecutor<LockedDataInfo>{

    @Value("${mScheduler.lock-time-secs:1800}")
    private long lockTime;

    @Override
    public boolean release(DataOperator operator,LockedDataInfo lockInfo) {
        //超过的设定的时间则释放锁
        if (System.currentTimeMillis() - lockInfo.getStartTime() >= lockTime * 1000) {
            operator.release(lockInfo);
            return true;
        }
        return false;
    }

    @Override
    public boolean support(String strategy) {
        return strategy.equalsIgnoreCase("random");
    }
}

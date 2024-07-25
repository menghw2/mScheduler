package name.menghw.strategy;

import name.menghw.data.domain.LockedDataInfo;
import name.menghw.data.opr.DataOperator;
import org.springframework.stereotype.Component;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
@Component
public class StickyStrategyExecutor extends StrategyExecutor<LockedDataInfo>{


    @Override
    public boolean release(DataOperator operator,LockedDataInfo lockInfo) {
        //sticky策略不释放锁
        //do nothing
        return false;
    }

    @Override
    public boolean support(String strategy) {
        return strategy.equalsIgnoreCase("sticky");
    }
}

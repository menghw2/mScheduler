package name.menghw.strategy;

import name.menghw.MSchedApplicationContext;
import name.menghw.data.LockStatus;
import name.menghw.data.opr.DataOperator;
import name.menghw.data.domain.LockedDataInfo;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
public abstract class StrategyExecutor<L extends LockedDataInfo> {

    /**
     * 上锁成功后加入队列（用来后续释放锁）
     * @param l
     * @return
     */
    public final LockStatus lockAndEnq(DataOperator opr, L l){
        l.setStartTime(System.currentTimeMillis());
        LockStatus status = this.lock(opr,l);
        if(status == LockStatus.LOCK_FI){//首次锁的话则入队
            MSchedApplicationContext.enTryReleaseQueue(l);
        }
        return status;
    }

    protected LockStatus lock(DataOperator opr, L l){
       return opr.lock(l);
    }

    abstract boolean release(DataOperator opr, L l);

    abstract boolean support(String strategy);
}

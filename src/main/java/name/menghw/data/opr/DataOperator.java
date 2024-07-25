package name.menghw.data.opr;

import name.menghw.data.LockStatus;
import name.menghw.data.domain.LockedDataInfo;

/**
 * 数据操作接口
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
public interface DataOperator<L extends LockedDataInfo> {

    LockStatus lock(L l);

    void release(L l);

    L get(L l);

    void releaseAll();
}

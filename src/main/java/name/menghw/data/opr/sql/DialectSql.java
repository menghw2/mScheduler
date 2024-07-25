package name.menghw.data.opr.sql;

import name.menghw.data.domain.LockedDataInfo;

/**
 * @author: menghw
 * @create: 2024/7/3
 * @Description:
 */
public interface DialectSql<L extends LockedDataInfo> {

    String insertSql(L info);

    String releaseSql(String method);

    String selectByMethodSql(String method);

    String releaseAllSql(String appId);
}

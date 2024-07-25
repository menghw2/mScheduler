package name.menghw.data.opr.sql;

import name.menghw.data.domain.LockedDataInfo;

/**
 * @author: menghw
 * @create: 2024/7/3
 * @Description:
 */
public class MysqlSql implements DialectSql<LockedDataInfo> {
    @Override
    public String insertSql(LockedDataInfo info) {
        return "insert into t_mscheduler_lock(method,appId,ip,startTime) values('"
                + info.getMethod() + "','"
                + info.getAppId() + "','"
                + info.getIp() + "',"
                + info.getStartTime() + ")";
    }

    @Override
    public String releaseSql(String method) {
        return "delete t_mscheduler_lock where method = '" + method + "'" ;
    }

    @Override
    public String selectByMethodSql(String method) {
        return  "select * from t_mscheduler_lock where method = '" + method + "'" ;
    }

    @Override
    public String releaseAllSql(String appId) {
        return "delete t_mscheduler_lock where appId = '" + appId + "'" ;
    }
}

package name.menghw;

import name.menghw.data.domain.LockedDataInfo;
import name.menghw.data.opr.DataOperator;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author: menghw
 * @create: 2024/7/3
 * @Description:
 */
public class MSchedApplicationContext{

    private static String dbType;

    private static DataOperator dataOperator;

    private static String ip;

    private static ConcurrentLinkedQueue<LockedDataInfo> tryReleaseQueue = new ConcurrentLinkedQueue<>();

    static {
        try {
            ip = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static DataOperator dataOperator(){
        return dataOperator;
    }
    public static void setDataOperator(DataOperator dataOperator){
        MSchedApplicationContext.dataOperator = dataOperator;
    }

    public static String dbType() {
        return dbType;
    }

    public static void setDbType(String dbType) {
        MSchedApplicationContext.dbType = dbType;
    }

    public static String ip() {
        return ip;
    }

    public static ConcurrentLinkedQueue<LockedDataInfo> tryReleaseQueue() {
        return tryReleaseQueue;
    }

    public static void enTryReleaseQueue(LockedDataInfo lockedDataInfo) {
        tryReleaseQueue.offer(lockedDataInfo);
    }
}

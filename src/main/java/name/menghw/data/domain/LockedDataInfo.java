package name.menghw.data.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.menghw.MSchedApplicationContext;
import name.menghw.tools.AppUniqName;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
public class LockedDataInfo{
    /**
     * 应用唯一标识
     */
    private String appId;
    /**
     * 服务器ip
     */
    private String ip;
    /**
     * 方法全路径名
     */
    private String method;
    /**
     * 锁定开始时间的时间戳，使用的是应用服务器时间而不是数据库服务器时间
     */
    private long startTime;
    /**
     * 是否可以开始尝试释放锁
     */
    @JsonIgnore
    private volatile boolean canStartTryRelease;

    public LockedDataInfo(String method) {
        this.appId = AppUniqName.appId();
        this.ip = MSchedApplicationContext.ip();
        this.method = method;
        this.startTime = System.currentTimeMillis();
    }

    public LockedDataInfo() {
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isCanStartTryRelease() {
        return canStartTryRelease;
    }

    public void setCanStartTryRelease(boolean canStartTryRelease) {
        this.canStartTryRelease = canStartTryRelease;
    }
}

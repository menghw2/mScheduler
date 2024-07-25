package name.menghw.data.opr;


import name.menghw.data.LockStatus;
import name.menghw.data.domain.LockedDataInfo;
import name.menghw.tools.AppUniqName;
import name.menghw.tools.JsonTool;
import name.menghw.tools.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
public class RedisOperator implements DataOperator<LockedDataInfo> {

    private final static String key = "lock:mScheduler:x";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private RedisTemplate template;

    public RedisOperator(RedisTemplate template) {
        this.template = template;
    }

    @Override
    public LockStatus lock(LockedDataInfo info) {

        LockedDataInfo dataInfo =  this.get(info);

        //已存在锁
        if (dataInfo != null) {
            //自己加的锁 可重入
            if(dataInfo.getAppId().equals(info.getAppId())){
                return LockStatus.LOCK_EN;
            }
            return null;
        }else{
            Boolean value = template.opsForHash().putIfAbsent(key, info.getMethod(), JsonTool.object2Json(info));
            return value.booleanValue()?LockStatus.LOCK_FI:null;
        }
    }

    @Override
    public void release(LockedDataInfo lockedDbData) {
        template.opsForHash().delete(key, lockedDbData.getMethod());
    }

    @Override
    public LockedDataInfo get(LockedDataInfo lockedDbData) {
        String value = (String)template.opsForHash().get(key, lockedDbData.getMethod());
        if(!StringTool.isEmpty(value)){
            return JsonTool.json2Object(value,LockedDataInfo.class);
        }
        return null;
    }

    @Override
    public void releaseAll() {
        String appId = AppUniqName.appId();
        logger.info("释放{}上的所有锁 -->",appId);
        Map<Object, Object> entries = template.opsForHash().entries(key);
        List<String> methods = new ArrayList<>();
        if (!entries.isEmpty()) {
            entries.keySet().forEach(method -> {
                String dataInfoStr = (String) entries.get(method);
                LockedDataInfo dataInfo = JsonTool.json2Object(dataInfoStr,LockedDataInfo.class);
                if (appId.equals(dataInfo.getAppId())){
                    methods.add(dataInfo.getMethod());
                }
            });
        }
        if(methods.size() > 0){
            for (String method : methods) {
                template.opsForHash().delete(key,method);
                logger.info("--> 释放{}上的锁", StringTool.shortMethod(method));
            }
        }
    }
}

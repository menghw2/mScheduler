package name.menghw.data.opr;


import name.menghw.MSchedApplicationContext;
import name.menghw.data.LockStatus;
import name.menghw.data.domain.LockedDataInfo;
import name.menghw.data.opr.sql.SqlChoosor;
import name.menghw.tools.AppUniqName;
import name.menghw.tools.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
public class DbOperator implements DataOperator<LockedDataInfo> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    JdbcTemplate jdbcTemplate;

    public DbOperator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LockStatus lock(LockedDataInfo info) {

        String dbType = MSchedApplicationContext.dbType();

        String insertSql = SqlChoosor.get(dbType).insertSql(info);

        LockedDataInfo dataInfo =  this.get(info);

        //已存在锁
        if (dataInfo != null) {
            //自己加的锁 可重入
            if(dataInfo.getAppId().equals(info.getAppId())){
                return LockStatus.LOCK_EN;
            }
            return null;
        }else{
            //尝试加锁
            try {
                int num = jdbcTemplate.update(insertSql);
                return num > 0?LockStatus.LOCK_FI:null;
            }catch (Exception e){
                logger.warn("{}在{}上加锁失败，可能已被别的应用加锁",info.getAppId(), StringTool.shortMethod(info.getMethod()));
            }
        }
        return null;
    }

    @Override
    public void release(LockedDataInfo info) {
        String dbType = MSchedApplicationContext.dbType();
        String releaseSql = SqlChoosor.get(dbType).releaseSql(info.getMethod());
        jdbcTemplate.update(releaseSql);
    }

    @Override
    public LockedDataInfo get(LockedDataInfo info) {
        String dbType = MSchedApplicationContext.dbType();
        String selectSql = SqlChoosor.get(dbType).selectByMethodSql(info.getMethod());
        List<LockedDataInfo> dataInfo =  jdbcTemplate.query(selectSql,new BeanPropertyRowMapper<>(LockedDataInfo.class));
        return CollectionUtils.isEmpty(dataInfo)?null:dataInfo.get(0);
    }

    @Override
    public void releaseAll() {
        String dbType = MSchedApplicationContext.dbType();
        String releaseAllSql = SqlChoosor.get(dbType).releaseAllSql(AppUniqName.appId());
        jdbcTemplate.update(releaseAllSql);
    }
}

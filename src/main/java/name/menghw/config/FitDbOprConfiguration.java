package name.menghw.config;

import name.menghw.MSchedApplicationContext;
import name.menghw.data.opr.DataOperator;
import name.menghw.data.opr.DbOperator;
import name.menghw.tools.AppUniqName;
import name.menghw.tools.StringTool;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
@Configuration
public class FitDbOprConfiguration implements InitializingBean, ApplicationContextAware {

    @Autowired(required = false)
    private List<DataSource> dataSources;
    @Value("${mScheduler.store:}")
    private String storeType;
    @Value("${mScheduler.dbSourceBeanName:}")
    private String dbSourceBeanName;

    private  ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {

        if(!CollectionUtils.isEmpty(dataSources)
                    && (StringTool.isEmpty(storeType) || "db".equalsIgnoreCase(storeType))
                    /*保证优先使用redis操作*/
                    && MSchedApplicationContext.dataOperator() == null){

            DataSource choose = null;
            if(!StringTool.isEmpty(dbSourceBeanName)){
                try {
                    choose = applicationContext.getBean(dbSourceBeanName,DataSource.class);
                }catch (Exception e){
                }
            }

            if(choose == null){
                //优选选用动态数据源
                choose = dataSources.get(0);
                for (DataSource ds : dataSources) {
                    if (ds instanceof AbstractRoutingDataSource) {
                        choose = ds;
                    }
                }
            }
            JdbcTemplate jdbcTemplate = new JdbcTemplate();
            jdbcTemplate.setDataSource(choose);
            DataOperator operator = new DbOperator(jdbcTemplate);
            MSchedApplicationContext.setDataOperator(operator);
            //获取数据库类型
            String  dbType =  choose.getConnection().getMetaData().getDatabaseProductName();
            MSchedApplicationContext.setDbType(dbType);
            //启动时，全部释放本应用加的锁
            try {
                //先加载AppUniqName
                applicationContext.getBean(AppUniqName.class);
                operator.releaseAll();
            }catch (Exception e){
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

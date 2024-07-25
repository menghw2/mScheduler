package name.menghw.config;

import name.menghw.MSchedApplicationContext;
import name.menghw.data.opr.DataOperator;
import name.menghw.data.opr.RedisOperator;
import name.menghw.tools.AppUniqName;
import name.menghw.tools.StringTool;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class FitRedisOprConfiguration implements InitializingBean, ApplicationContextAware {

    @Autowired(required = false)
    private List<RedisTemplate> redisTemplates;

    private  ApplicationContext applicationContext;

    @Value("${mScheduler.store:}")
    private String storeType;

    @Override
    public void afterPropertiesSet() throws Exception {
        if(!CollectionUtils.isEmpty(redisTemplates)){
            //优选选用StringRedisTemplate
            RedisTemplate choose = redisTemplates.get(0);
            for (RedisTemplate template : redisTemplates) {
                if (template instanceof StringRedisTemplate) {
                    choose = template;
                }
            }
            if ((StringTool.isEmpty(storeType) || "redis".equalsIgnoreCase(storeType))) {
                DataOperator operator = new RedisOperator(choose);
                MSchedApplicationContext.setDataOperator(operator);
                //启动时，全部释放本应用加的锁
                try {
                    //先加载AppUniqName
                    applicationContext.getBean(AppUniqName.class);
                    operator.releaseAll();
                }catch (Exception e){
                }
            }
        }
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

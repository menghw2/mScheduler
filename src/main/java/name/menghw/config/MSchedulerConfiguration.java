package name.menghw.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "name.menghw")
public class MSchedulerConfiguration {

}

package name.menghw.data.opr.sql;

/**
 * @author: menghw
 * @create: 2024/7/3
 * @Description:
 */
public class SqlChoosor {

    public static DialectSql get(String dbType){
        if(dbType.equalsIgnoreCase("Oracle")){
            return new OracleSql();
        }else if(dbType.equalsIgnoreCase("Mysql")){
            return new MysqlSql();
        }else{
            return new DefaultSql();
        }
    }
}

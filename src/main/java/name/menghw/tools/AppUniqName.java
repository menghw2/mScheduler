package name.menghw.tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * @author: menghw
 * @create: 2024/7/2
 * @Description:
 */
@Component
public class AppUniqName {

    private static String serviceName;

    private static String servicePort;

    private static String cachedAppId;

    @Value("${spring.application.name}")
    public void setServiceName(String serviceName) {
        AppUniqName.serviceName = serviceName;
    }
    @Value("${server.port}")
    public void setServicePort(String servicePort) {
        AppUniqName.servicePort = servicePort;
    }

    public static String appId() {

        if(!StringTool.isEmpty(cachedAppId)){
            return cachedAppId;
        }
        String appIdTemp = findInFile();

        if(StringTool.isEmpty(appIdTemp)){
            appIdTemp = UUID.randomUUID().toString().replaceAll("-","");
            //保存到文件
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(storeFileName());
                fileWriter.write(appIdTemp);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if (fileWriter != null) {
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        cachedAppId = appIdTemp;
        return appIdTemp;
    }

    /**
     * 在应用根路径相关文件查找应用唯一id
     * @return
     */
    public static String findInFile() {
        File file = new File(storeFileName());
        if(file.exists()){
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
                char[] buff = new char[32];
                fileReader.read(buff);
               return String.valueOf(buff);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return null;
    }

    private static String storeFileName(){
        String appUniqName_fileName = "";
        if(!StringTool.isEmpty(serviceName)){
            appUniqName_fileName = serviceName;
        }
        if(!StringTool.isEmpty(servicePort)){
            appUniqName_fileName = appUniqName_fileName +"_"+ servicePort;
        }
        appUniqName_fileName =  "mscheduler" +"_"+ appUniqName_fileName + ".uniqname";
        return appUniqName_fileName;
    }

}

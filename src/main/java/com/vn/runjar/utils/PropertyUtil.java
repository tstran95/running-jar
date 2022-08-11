package com.vn.runjar.utils;

import com.vn.runjar.constant.Constant;
import com.vn.runjar.exception.VNPAYException;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class PropertyUtil {
    private static PropertyUtil _instance = null;
    public static String path = null;
    public static String period = null;
    public static String clazzName = null;

    public static void initialProperty(String key , String nameLib , String className , boolean classNameChanged) {
        try {
            log.info("PropertyInfo initialProperty() START");
            String pathParent = Objects.requireNonNull(AppUtil.class.getResource("/")).getPath();
            log.info("PropertyInfo initialProperty() PATH : {}", pathParent);
            Path pathStr = Paths.get(pathParent).getParent().getParent();
            String url;
            String urlSub;
            if (Constant.MAIN_STRING.equals(key)) {
                urlSub = pathStr.getParent().getParent().getParent().getParent().getParent().getParent().toString();
            } else {
                urlSub = pathStr.toString();
            }
            url = urlSub.substring(urlSub.indexOf("/")) + Constant.CONFIG_URL;
//            url = "/home/tstran95/Public/WS/runtime-jar/runJARFileWithJersey/" + Constant.CONFIG_URL;
            log.info("PropertyInfo initialProperty() URL SUB with main param : {} , {}", url, key);

            InputStream is = Files.newInputStream(Paths.get(url));
            Properties props = new Properties();
            props.load(is);
            if (classNameChanged){
                AppUtil.readAndWriteFileProps(url ,nameLib , className);
                log.info("PropertyInfo initialProperty() WRITE AGAIN");
            }
            path = props.getProperty(Constant.PATH);
            period = props.getProperty(Constant.CONFIG_PERIOD);
            clazzName = props.getProperty(Constant.CONFIG_CLASS);
            log.info("PropertyInfo initialProperty() END");
        } catch (Exception e) {
            log.info("PropertyInfo initialProperty() ERROR with Exception " , e);
            throw new VNPAYException(Constant.PROPERTY_NOT_FOUND);
        }
    }

    public static PropertyUtil instance(String key , String libName , String className , boolean classNameChanged) {
        log.info("PropertyInfo instance() START with key {} " , key);
        if (_instance == null) {
            _instance = new PropertyUtil();
            initialProperty(key , libName , className , classNameChanged);
            log.info("PropertyInfo instance() CREATE NEW PROPERTY");
        }
        log.info("PropertyInfo instance() END with {}" , _instance.toString() );
        return _instance;
    }
}

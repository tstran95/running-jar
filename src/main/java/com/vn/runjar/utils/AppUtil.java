package com.vn.runjar.utils;

import com.vn.runjar.constant.Constant;
import com.vn.runjar.exception.VNPAYException;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class AppUtil {

    /**
     * reads data from file and encrypt file to String
     * Each file has only 1 hex String
     *
     * @param path String
     * @return String
     */
    public static String checkSum(String path) {
        log.info("AppUtil checkSum START with PATH :  {}", path);
        StringBuilder result = new StringBuilder();
        try {
            // file hashing with DigestInputStream
            MessageDigest md = MessageDigest.getInstance(Constant.ALGORITHM); //SHA, MD2, MD5, SHA-256, SHA-384...
            try (DigestInputStream dis = new DigestInputStream(Files.newInputStream(Paths.get(path)), md)) {
                while (dis.read() != -1) ; //empty loop to clear the data
                md = dis.getMessageDigest();
            }

            // bytes to hex
            for (byte b : md.digest()) {
                result.append(String.format(Constant.FORMAT, b));
            }
        } catch (Exception e) {
            log.info("AppUtil checkSum ERROR with error : ", e);
            throw new VNPAYException(Constant.CHECK_SUM_ERROR);
        }
        log.info("AppUtil checkSum END with HEX STRING :  {}", result);
        return result.toString();
    }

    /**
     * parse String to Long
     *
     * @param value String
     * @return Long
     */
    public static long parseLong(String value) {
//        log.info("AppUtil parseLong() START with request : {}", value);
        try {
//            log.info("AppUtil parseLong() END");
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
//            log.info("AppUtil parseLong() ERROR with exception : ", e);
            throw new VNPAYException(Constant.PARSE_STRING_TO_LONG_ERROR);
        }
    }

    /**
     * Using WatchEvent API, listen event Modify File from windows
     * and change status in redis
     *
     * @param jedisPool JedisPool
     */
    public static void watchEvent(JedisPool jedisPool) {
        log.info("AppUtil watchEvent START");
        try (Jedis jedis = jedisPool.getResource()) {
            WatchKey key;
            PropertyUtil.instance(Constant.MAIN_STRING , Constant.EMPTY, Constant.EMPTY , false);
            String path = PropertyUtil.path;

            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(path).getParent();
            log.info("AppUtil watchEvent RUN with PATH : {}", dir);

            //register a folder to WatchService to listen modify event in this folder
            dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                key = watcher.poll();

                // if key is null, don't have event in registered folder
                // else listen event, check modify event with jar file and load class in jar file again
                if (Objects.nonNull(key)) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        // Retrieve the type of event by using the kind() method.
                        WatchEvent.Kind<?> kind = event.kind();
                        //cast type of WatchEvent to Path
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context().getFileName();
                        // compare type of event with modify event
                        if ((kind == StandardWatchEventKinds.ENTRY_MODIFY)) {
                            // compare name of file was changed with name of file want check
                            // if same the name ->> load class again
                            if (Constant.JAR_FILE_NAME.equals(fileName.toString())) {
                                log.info("AppUtil watchEvent had MODIFIED the FILE NAME : {} ", fileName.getFileName());
                                jedis.hset(Constant.KEY_CHECK_CHANGE, Constant.STATUS_STR, Constant.STATUS_CHANGED);
                            }
                        }
                    }
                    // if reset() return false, key can't receive new event and break out of loop
                    if (!key.reset()) {
                        log.info("AppUtil watchEvent run with ERROR : KEY cant Reset");
                        break;
                    }
                }
            }
            log.info("AppUtil watchEvent END");
        } catch (IOException e) {
            log.info("AppUtil watchEvent ERROR with exception ", e);
            throw new VNPAYException(Constant.IOEXCEPTION);
        }
    }

    public static void readAndWriteFileProps(String path , String nameLib, String className) {
        log.info("AppUtil readAndWriteFileProps() START with PATH : {}" , path);
        String data = readFileAndReturnString(path , nameLib , className);
//        log.info("AppUtil watchEvent RUNNING with DATA : {}" , data);
        writeToTheFile(data , path);
        log.info("AppUtil readAndWriteFileProps() END with DATA : {}" , data);
    }

    private static String readFileAndReturnString(String path, String nameLib, String className) {
        log.info("AppUtil readFileAndReturnString() START with ClassName : {}" , className);
        StringBuilder doc = new StringBuilder();
        try (FileReader reader = new FileReader(path)) {
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;
            Map<String , String> map = new HashMap<>();

            while ((line = bufferedReader.readLine()) != null) {
                String key = (line.substring(0 , (line.indexOf("=") - 1)));
                String value = line.substring(line.indexOf("=") +2);
                map.put(key , value);
            }

            String lib = Constant.PATH + "_" + nameLib;
            map.put(Constant.PATH , map.get(lib));
            map.put(Constant.CONFIG_CLASS , className);
            for (String key : map.keySet()) {
                doc.append(key);
                doc.append( " = " );
                doc.append(map.get(key));
                doc.append("\n");
            }
            bufferedReader.close();

        }catch (IOException e) {
            log.info("AppUtil readFileAndReturnString() ERROR with Exception : " , e);
            throw new VNPAYException(Constant.IOEXCEPTION);
        }
        log.info("AppUtil readFileAndReturnString() END with Response : {}" , doc);
        return doc.toString();
    }

    private static void writeToTheFile(String data, String path) {
        log.info("AppUtil writeToTheFile() START with PATH : {}" , path);
        try (FileWriter writer = new FileWriter(path)) {
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            bufferedWriter.write(data);
            bufferedWriter.close();
            log.info("AppUtil writeToTheFile() END with DATA : {}" , data);
        }catch (IOException e) {
            log.info("AppUtil writeToTheFile() ERROR with Exception : " , e);
            throw new VNPAYException(Constant.IOEXCEPTION);
        }
    }

    public static String getPath(String key) {
        log.info("AppUtil getPath() START with KEY {}" , key);
        String pathParent = Objects.requireNonNull(AppUtil.class.getResource("/")).getPath();
        log.info("AppUtil getPath() PATH : {}", pathParent);
        Path pathStr = Paths.get(pathParent).getParent().getParent().getParent().getParent();
        String url;
        String urlSub;
        if (Constant.MAIN_STRING.equals(key)) {
            urlSub = pathStr.getParent().getParent().getParent().toString();
        } else {
            urlSub = pathStr.toString();
        }
        url = urlSub.substring(urlSub.indexOf("/")) + Constant.CONFIG_URL;
        log.info("AppUtil getPath() END with URL {}" , url);
        return url;
    }

}

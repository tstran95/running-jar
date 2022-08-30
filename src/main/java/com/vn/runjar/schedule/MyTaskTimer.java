package com.vn.runjar.schedule;

import com.vn.runjar.config.JedisPoolFactory;
import com.vn.runjar.constant.Constant;
import com.vn.runjar.exception.VNPAYException;
import com.vn.runjar.utils.PropertyUtil;
import com.vn.runjar.utils.AppUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.TimerTask;

@Slf4j
public class MyTaskTimer extends TimerTask {
    public static boolean status = false;
    @Override
    public void run() {
        log.info("MyTaskTimer run() START");
        JedisPool jedisPool = JedisPoolFactory.getInstance();
        PropertyUtil.initialProperty(Constant.MAIN_STRING , Constant.EMPTY, Constant.EMPTY , false);
        String path = PropertyUtil.path;
        // check By Sum
//        checkBySum(jedisPool, path);
        //check by time
        checkTimeAccessFile(jedisPool , path);
        log.info("MyTaskTimer run() END");
    }

    /**
     * compare current hexString with hexString stored in Redis
     * if not same, change status in redis to load Class again
     */
    private void checkBySum(JedisPool jedisPool, String path) {
        log.info("MyTaskTimer checkBySum START with PATH {}", path);
        // creat hex string of file
        String hexStr = AppUtil.checkSum(path);
        try (Jedis jedis = jedisPool.getResource()) {
            String hexSaved = jedis.hget(Constant.KEY_CHECK_CHANGE, Constant.CHECK_SUM_STR);
            log.info("MyTaskTimer checkBySum END with OLD HEX {}", hexSaved);
            if (!hexStr.equals(hexSaved)) {
                jedis.hset(Constant.KEY_CHECK_CHANGE, Constant.CHECK_SUM_STR, hexStr);
                jedis.hset(Constant.KEY_CHECK_CHANGE, Constant.STATUS_STR, Constant.STATUS_CHANGED);
                log.info("MyTaskTimer checkBySum RUNNING with message STATUS HAD CHANGE");
            }
            log.info("MyTaskTimer checkBySum END with HEX_STRING {}", hexStr);
        }catch (Exception e) {
            log.info("MyTaskTimer checkBySum ERROR with message " , e);
            throw new VNPAYException(e.getMessage());
        }
    }

    /**
     * compare current access time of file now with the time stored in Redis
     * if not same, change status in redis to load Class again
     */
    private void checkTimeAccessFile(JedisPool jedisPool, String path) {
        log.info("MyTaskTimer checkTimeAccessFile START with PATH {}", path);
        try (Jedis jedis = jedisPool.getResource()) {
            File fileName = new File(path);
            // get time modified file
            BasicFileAttributes attributes = Files.readAttributes(Paths.get(fileName.toURI()),
                    BasicFileAttributes.class);
            FileTime fileTime = attributes.lastAccessTime();
            String timeInRedis = jedis.hget(Constant.KEY_CHECK_CHANGE, Constant.TIME_ACCESS);
            if (!fileTime.toString().equals(timeInRedis)) {
                log.info("MyTaskTimer checkTimeAccessFile RUNNING with FILE TIME HAVE CHANGE");
                jedis.hset(Constant.KEY_CHECK_CHANGE, Constant.TIME_ACCESS, fileTime.toString());
                jedis.hset(Constant.KEY_CHECK_CHANGE, Constant.STATUS_STR, Constant.STATUS_CHANGED);
            }
            log.info("MyTaskTimer checkTimeAccessFile END with FILE TIME {}", fileTime);
        } catch (Exception e) {
            throw new VNPAYException("IOException");
        }
    }
}

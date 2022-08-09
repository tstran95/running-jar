package com.vn.runjar.validation;

import com.vn.runjar.constant.Constant;
import com.vn.runjar.exception.VNPAYException;
import com.vn.runjar.model.ClassInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class Validator {
    /**
     * Validate input
     * throw exception when have error
     * @param classInfo ClassInfo
     */
    public static void checkInput(ClassInfo classInfo) {
        log.info("Validator checkInput START with request : {}", classInfo);
        if (Objects.isNull(classInfo)) {
            log.info("Validator checkInput has EXCEPTION with message : {}", Constant.CLASS_INFO_NULL);
            throw new VNPAYException(Constant.CLASS_INFO_NULL);
        }
        log.info("Validator checkInput END");
    }
}

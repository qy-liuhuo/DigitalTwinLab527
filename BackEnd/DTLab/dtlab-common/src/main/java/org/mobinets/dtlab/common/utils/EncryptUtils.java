package org.mobinets.dtlab.common.utils;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.UUID;

public class EncryptUtils {
    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    // MD5加密
    // password + salt
    public static String md5(String key) {
        if(StringUtils.isBlank(key)) {
            return null;
        } else{
            return DigestUtils.md5Hex(key.getBytes(StandardCharsets.UTF_8));
        }
    }
}

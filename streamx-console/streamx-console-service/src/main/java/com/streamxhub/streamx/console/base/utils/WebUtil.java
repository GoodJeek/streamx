/*
 * Copyright (c) 2019 The StreamX Project
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.streamxhub.streamx.console.base.utils;


import java.io.File;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.streamxhub.streamx.console.base.domain.Constant;


/**
 * 工具类
 */
@Slf4j
public class WebUtil {

    /**
     * token 加密
     *
     * @param token token
     * @return 加密后的 token
     */
    public static String encryptToken(String token) {
        try {
            EncryptUtil encryptUtil = new EncryptUtil(Constant.TOKEN_CACHE_PREFIX);
            return encryptUtil.encrypt(token);
        } catch (Exception e) {
            log.info("token加密失败：", e);
            return null;
        }
    }

    /**
     * token 解密
     *
     * @param encryptToken 加密后的 token
     * @return 解密后的 token
     */
    public static String decryptToken(String encryptToken) {
        try {
            EncryptUtil encryptUtil = new EncryptUtil(Constant.TOKEN_CACHE_PREFIX);
            return encryptUtil.decrypt(encryptToken);
        } catch (Exception e) {
            log.info("token解密失败：", e);
            return null;
        }
    }

    /**
     * 驼峰转下划线
     *
     * @param value 待转换值
     * @return 结果
     */
    public static String camelToUnderscore(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        String[] arr = StringUtils.splitByCharacterTypeCamelCase(value);
        if (arr.length == 0) {
            return value;
        }
        StringBuilder result = new StringBuilder();
        IntStream.range(0, arr.length).forEach(i -> {
            if (i != arr.length - 1) {
                result.append(arr[i]).append(StringPool.UNDERSCORE);
            } else {
                result.append(arr[i]);
            }
        });
        return StringUtils.lowerCase(result.toString());
    }

    public static String getAppHome() {
        return System.getProperty("app.home");
    }

    public static String getAppDir(String dir) {
        return getAppHome().concat(File.separator).concat(dir);
    }
}

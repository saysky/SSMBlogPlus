package com.example.blog.util;

import com.example.blog.enums.CommonParamsEnum;
import lombok.extern.slf4j.Slf4j;


/**
 * <pre>
 *     常用工具
 * </pre>
 */
@Slf4j
public class SensUtils {


    /**
     * 转换文件大小
     *
     * @param size size
     * @return String
     */
    public static String parseSize(long size) {
        if (size < CommonParamsEnum.BYTE.getValue()) {
            return String.valueOf(size) + "B";
        } else {
            size = size / 1024;
        }
        if (size < CommonParamsEnum.BYTE.getValue()) {
            return String.valueOf(size) + "KB";
        } else {
            size = size / 1024;
        }
        if (size < CommonParamsEnum.BYTE.getValue()) {
            size = size * 100;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "MB";
        } else {
            size = size * 100 / 1024;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "GB";
        }
    }


}

package com.example.blog.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrBuilder;
import com.example.blog.enums.CommonParamsEnum;
import com.example.blog.exception.MyBusinessException;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传
 *
 * @author 言曌
 * @date 2020/3/8 5:45 下午
 */
public class FileUtil {

        public final static String rootPath = "/Users/liuyanzhao/Desktop/uploads/";
//    public final static String rootPath = "D:\\uploads\\";


    /**
     * 文件上传
     * 上传图片和缩略图
     *
     * @param file
     * @return
     */
    public static Map<String, String> upload(MultipartFile file) {
        final Map<String, String> resultMap = new HashMap<>(6);
        try {
            //用户目录
            StrBuilder uploadPath = new StrBuilder(rootPath);
            uploadPath.append(DateUtil.thisYear()).append("/").append(DateUtil.thisMonth() + 1).append("/");
            final File mediaPath = new File(uploadPath.toString());
            if (!mediaPath.exists()) {
                if (!mediaPath.mkdirs()) {
                    throw new MyBusinessException("文件上传失败，无法创建文件夹");
                }
            }
            // 原始文件名
            String originFileName = file.getOriginalFilename();
            // 后缀
            final String fileSuffix = originFileName.substring(file.getOriginalFilename().lastIndexOf('.') + 1);
            // 新文件名
            String nameWithOutSuffix = UUID.randomUUID().toString().replaceAll("-", "");

            //带后缀
            String newFileName = nameWithOutSuffix + "." + fileSuffix;
            String newSmallFileName = nameWithOutSuffix + "_small." + fileSuffix;

            // 判断文件名是否已存在
            File descFile = new File(mediaPath.getAbsoluteFile(), newFileName.toString());
            file.transferTo(descFile);

            // 文件原路径
            final StrBuilder fullPath = new StrBuilder(mediaPath.getAbsolutePath());
            fullPath.append("/");
            fullPath.append(newFileName);

            // 缩略图路径
            final StrBuilder fullSmallPath = new StrBuilder(mediaPath.getAbsolutePath());
            fullSmallPath.append("/");
            fullSmallPath.append(newSmallFileName);

            //压缩图片
            try {
                Thumbnails.of(fullPath.toString()).size(256, 256).keepAspectRatio(false).toFile(fullSmallPath.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //映射路径
            final StrBuilder fileUrl = new StrBuilder("/uploads/");
            fileUrl.append(DateUtil.thisYear());
            fileUrl.append("/");
            fileUrl.append(DateUtil.thisMonth() + 1);
            fileUrl.append("/");
            fileUrl.append(nameWithOutSuffix + "." + fileSuffix);

            //缩略图映射路径
            final StrBuilder fileSmallPath = new StrBuilder("/uploads/");
            fileSmallPath.append(DateUtil.thisYear());
            fileSmallPath.append("/");
            fileSmallPath.append(DateUtil.thisMonth() + 1);
            fileSmallPath.append("/");
            fileSmallPath.append(nameWithOutSuffix);
            fileSmallPath.append("_small.");
            fileSmallPath.append(fileSuffix);

            final String size = parseSize(new File(fullPath.toString()).length());
            final String wh = getImageWh(new File(fullPath.toString()));

            resultMap.put("fileName", originFileName);
            resultMap.put("fileUrl", fileUrl.toString());
            resultMap.put("fileSmallPath", fileSmallPath.toString());
            resultMap.put("fileSuffix", fileSuffix);
            resultMap.put("fileSize", size);
            resultMap.put("fileWh", wh);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyBusinessException("文件上传失败");
        }
        return resultMap;
    }

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    public static Map<String, String> simpleUpload(MultipartFile file) {
        final Map<String, String> resultMap = new HashMap<>(6);
        try {
            //用户目录
            StrBuilder uploadPath = new StrBuilder(rootPath);
            uploadPath.append(DateUtil.thisYear()).append("/").append(DateUtil.thisMonth() + 1).append("/");
            final File mediaPath = new File(uploadPath.toString());
            if (!mediaPath.exists()) {
                if (!mediaPath.mkdirs()) {
                    throw new MyBusinessException("文件上传失败，无法创建文件夹");
                }
            }
            // 原始文件名
            String originFileName = file.getOriginalFilename();
            // 后缀
            final String fileSuffix = originFileName.substring(file.getOriginalFilename().lastIndexOf('.') + 1);
            // 新文件名
            String nameWithOutSuffix = UUID.randomUUID().toString().replaceAll("-", "");

            //带后缀
            String newFileName = nameWithOutSuffix + "." + fileSuffix;

            // 判断文件名是否已存在
            File descFile = new File(mediaPath.getAbsoluteFile(), newFileName);
            file.transferTo(descFile);

            // 文件原路径
            final StrBuilder fullPath = new StrBuilder(mediaPath.getAbsolutePath());
            fullPath.append("/");
            fullPath.append(newFileName);

            //映射路径
            final StrBuilder fileUrl = new StrBuilder("/uploads/");
            fileUrl.append(DateUtil.thisYear());
            fileUrl.append("/");
            fileUrl.append(DateUtil.thisMonth() + 1);
            fileUrl.append("/");
            fileUrl.append(nameWithOutSuffix + "." + fileSuffix);

            final String size = parseSize(new File(fullPath.toString()).length());

            resultMap.put("fileName", originFileName.substring(0, originFileName.lastIndexOf(".")));
            resultMap.put("filePath", fullPath.toString());
            resultMap.put("fileUrl", fileUrl.toString());
            resultMap.put("fileSuffix", fileSuffix);
            resultMap.put("fileSize", size);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyBusinessException("文件上传失败");
        }
        return resultMap;
    }


    /**
     * 转换文件大小
     *
     * @param size size
     * @return String
     */
    private static String parseSize(long size) {
        if (size < CommonParamsEnum.BYTE.getValue()) {
            return size + "B";
        } else {
            size = size / 1024;
        }
        if (size < CommonParamsEnum.BYTE.getValue()) {
            return size + "KB";
        } else {
            size = size / 1024;
        }
        if (size < CommonParamsEnum.BYTE.getValue()) {
            size = size * 100;
            return (size / 100) + "." + (size % 100) + "MB";
        } else {
            size = size * 100 / 1024;
            return (size / 100) + "." + (size % 100) + "GB";
        }
    }


    /**
     * 获取文件长和宽
     *
     * @param file file
     * @return String
     */
    private static String getImageWh(File file) {
        try {
            BufferedImage image = ImageIO.read(new FileInputStream(file));
            return image.getWidth() + "x" + image.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

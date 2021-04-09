package com.example.blog.controller.admin;

import com.example.blog.controller.common.BaseController;
import com.example.blog.dto.JsonResult;
import com.example.blog.exception.MyBusinessException;
import com.example.blog.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * <pre>
 *     后台附件控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/19
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/file")
public class AttachmentController extends BaseController {


    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> jsonResult(@RequestParam("file") MultipartFile file) {
        Map<String, String> uploadMap = FileUtil.simpleUpload(file);
        String fileUrl = uploadMap.get("fileUrl");
        Map<String, Object> map = new HashMap<>();
        map.put("link", fileUrl);
        return map;
    }

}

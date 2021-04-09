package com.example.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.controller.common.BaseController;
import com.example.blog.dto.JsonResult;
import com.example.blog.dto.QueryCondition;
import com.example.blog.entity.Photo;
import com.example.blog.entity.PhotoCategory;
import com.example.blog.enums.ResultCodeEnum;
import com.example.blog.service.PhotoCategoryService;
import com.example.blog.service.PhotoService;
import com.example.blog.util.FileUtil;
import com.example.blog.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 照片管理控制器
 *
 * @author liuyanzhao
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/photo")
public class PhotoController extends BaseController {


    @Autowired
    private PhotoService photoService;

    @Autowired
    private PhotoCategoryService photoCategoryService;


    /**
     * 查询所有照片并渲染photo页面
     *
     * @return 模板路径admin/admin_photo
     */
    @RequestMapping(method = RequestMethod.GET)
    public String photoList(@RequestParam(value = "cateId") Long cateId,
                            @RequestParam(value = "page", defaultValue = "0") Integer pageNumber,
                            @RequestParam(value = "size", defaultValue = "24") Integer pageSize,
                            @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                            @RequestParam(value = "order", defaultValue = "desc") String order, Model model) {
        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);

        PhotoCategory photoCategory = photoCategoryService.get(cateId);
        if (photoCategory == null) {
            return this.renderNotFound();
        }
        QueryCondition queryCondition = new QueryCondition();
        Photo condition = new Photo();
        condition.setUserId(getLoginUserId());
        condition.setCategoryId(cateId);
        queryCondition.setData(condition);
        Page<Photo> photoPage = photoService.findAll(page, queryCondition);
        model.addAttribute("photoList", photoPage.getRecords());
        model.addAttribute("pageInfo", PageUtil.convertPageVo(page));
        model.addAttribute("photoCategory", photoCategory);
        return "admin/admin_photo";
    }

    /**
     * 照片上传
     *
     * @param file 文件
     * @return JsonResult
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("cateId") Long cateId) {

        Map<String, String> resultMap = FileUtil.upload(file);
        Photo photo = new Photo();
        photo.setUserId(getLoginUserId());
        photo.setCreateTime(new Date());
        photo.setUpdateTime(new Date());
        photo.setFileName(resultMap.get("fileName"));
        photo.setFilePath(resultMap.get("filePath"));
        photo.setFileSmallPath(resultMap.get("fileSmallPath"));
        photo.setFileSuffix(resultMap.get("fileSuffix"));
        photo.setFileSize(resultMap.get("fileSize"));
        photo.setFileWh(resultMap.get("fileWh"));
        photo.setCategoryId(cateId);
        photoService.insertOrUpdate(photo);
        return JsonResult.success("保存成功", photo);
    }

    /**
     * 照片上传
     *
     * @param file 文件
     * @return JsonResult
     */
    @RequestMapping(value = "/admin/photo/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> resultMap = FileUtil.upload(file);
        // 其他操作，如存储文件信息到数据库
        return JsonResult.success("上传成功");
    }


    /**
     * 删除照片
     *
     * @param id 照片Id
     * @return JsonResult
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult delete(@RequestParam("id") Long id) {
        photoService.delete(id);
        return JsonResult.success("删除成功");
    }


    /**
     * 批量删除
     *
     * @param ids 回复ID列表
     * @return
     */
    @RequestMapping(value = "/batchDelete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult batchDelete(@RequestParam("ids") List<Long> ids) {
        Long userId = getLoginUserId();
        //批量操作
        //1、防止恶意操作
        if (ids == null || ids.size() == 0 || ids.size() >= 100) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), "参数不合法!");
        }
        //2、检查用户权限
        List<Photo> photoList = photoService.findByBatchIds(ids);
        for (Photo photo : photoList) {
            if (!Objects.equals(photo.getUserId(), userId)) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), "没有权限删除!");
            }
        }
        //3、删除
        photoService.batchDelete(ids);
        return JsonResult.success("删除成功");
    }


    /**
     * 新增/修改照片
     *
     * @param photo photo对象
     * @return JsonResult
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult savePhoto(@ModelAttribute Photo photo) {
        photo.setUserId(getLoginUserId());
        photoService.insertOrUpdate(photo);
        return JsonResult.success("保存成功");
    }

    /**
     * 详情
     *
     * @param id 照片Id
     * @return JsonResult
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public String detail(@RequestParam("id") Long id, Model model) {
        Photo photo = photoService.get(id);
        if (photo == null) {
            return renderNotFound();
        }
        model.addAttribute("photo", photo);
        List<PhotoCategory> photoCategoryList = photoCategoryService.findByUserId(getLoginUserId());
        model.addAttribute("photoCategoryList", photoCategoryList);
        return "admin/admin_photo_detail";
    }

}

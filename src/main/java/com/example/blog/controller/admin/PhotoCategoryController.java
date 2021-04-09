package com.example.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.controller.common.BaseController;
import com.example.blog.dto.JsonResult;
import com.example.blog.dto.QueryCondition;
import com.example.blog.entity.PhotoCategory;
import com.example.blog.enums.ResultCodeEnum;
import com.example.blog.service.PhotoCategoryService;
import com.example.blog.service.PhotoService;
import com.example.blog.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 相册管理控制器
 *
 * @author liuyanzhao
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/photoCategory")
public class PhotoCategoryController extends BaseController {

    @Autowired
    private PhotoCategoryService categoryService;

    @Autowired
    private PhotoService photoService;


    /**
     * 查询所有相册并渲染category页面
     *
     * @return 模板路径admin/admin_category
     */
    @RequestMapping(method = RequestMethod.GET)
    public String categories(@RequestParam(value = "page", defaultValue = "0") Integer pageNumber,
                             @RequestParam(value = "size", defaultValue = "24") Integer pageSize,
                             @RequestParam(value = "sort", defaultValue = "cateSort") String sort,
                             @RequestParam(value = "order", defaultValue = "desc") String order, Model model) {
        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);

        QueryCondition queryCondition = new QueryCondition();
        PhotoCategory condition = new PhotoCategory();
        condition.setUserId(getLoginUserId());
        queryCondition.setData(condition);
        Page<PhotoCategory> categoryPage = categoryService.findAll(page, queryCondition);
        model.addAttribute("categories", categoryPage.getRecords());
        model.addAttribute("pageInfo", PageUtil.convertPageVo(page));
        return "admin/admin_photo_category";
    }

    /**
     * 新增/修改相册目录
     *
     * @param category category对象
     * @return JsonResult
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult saveCategory(@ModelAttribute PhotoCategory category) {
        category.setUserId(getLoginUserId());
        categoryService.insertOrUpdate(category);
        return JsonResult.success("保存成功");
    }

    /**
     * 删除相册
     *
     * @param cateId 相册Id
     * @return JsonResult
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public JsonResult delete(@RequestParam("id") Long cateId) {
        //1.判断这个相册有照片
        Integer count = photoService.countByCategoryId(cateId);
        if (count != 0) {
            return JsonResult.error("该相册已经有了图片，无法删除");
        }
        categoryService.delete(cateId);
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
        List<PhotoCategory> photoCategoryList = categoryService.findByBatchIds(ids);
        for (PhotoCategory category : photoCategoryList) {
            if (!Objects.equals(category.getUserId(), userId)) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), "没有权限删除!");
            }
        }
        //3、删除
        categoryService.batchDelete(ids);
        return JsonResult.success("删除成功");
    }

    /**
     * 详情
     *
     * @param cateId 相册Id
     * @return JsonResult
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public JsonResult detail(@RequestParam("id") Long cateId) {
        PhotoCategory photoCategory = categoryService.get(cateId);
        if (photoCategory == null) {
            return JsonResult.error("相册不存在");
        }
        return JsonResult.success("查询成功", photoCategory);
    }

}

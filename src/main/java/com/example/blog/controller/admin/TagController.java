package com.example.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.controller.common.BaseController;
import com.example.blog.dto.JsonResult;
import com.example.blog.entity.Tag;
import com.example.blog.service.TagService;
import com.example.blog.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * <pre>
 *     后台标签管理控制器
 * </pre>
 *
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/tag")
public class TagController extends BaseController {

    @Autowired
    private TagService tagService;

    /**
     * 新增/修改标签
     *
     * @param tag tag
     */
    @RequestMapping(method = RequestMethod.POST, value = "/save")
    @ResponseBody
    public JsonResult saveTag(@ModelAttribute Tag tag) {
        tagService.insertOrUpdate(tag);
        return JsonResult.success("保存成功");

    }

    /**
     * 渲染标签管理页面
     *
     * @return 模板路径admin/admin_tag
     */
    @RequestMapping(method = RequestMethod.GET)
    public String tags(@RequestParam(value = "page", defaultValue = "0") Integer pageNumber,
                       @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                       @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                       @RequestParam(value = "order", defaultValue = "desc") String order, Model model) {
        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        Page<Tag> tagPage = tagService.findAll(page);
        model.addAttribute("tags", tagPage.getRecords());
        model.addAttribute("pageInfo", PageUtil.convertPageVo(page));
        return "admin/admin_tag";
    }

    /**
     * 删除标签
     *
     * @param tagId 标签Id
     * @return JsonResult
     */
    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    @ResponseBody
    public JsonResult checkDelete(@RequestParam("id") Long tagId) {
        tagService.delete(tagId);
        return JsonResult.success("删除成功");
    }

    /**
     * 跳转到修改标签页面
     *
     * @param model model
     * @param tagId 标签编号
     * @return 模板路径admin/admin_tag
     */
    @RequestMapping(method = RequestMethod.GET, value = "/edit")
    public String toEditTag(Model model,
                            @RequestParam(value = "page", defaultValue = "0") Integer pageNumber,
                            @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                            @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                            @RequestParam(value = "order", defaultValue = "desc") String order,
                            @RequestParam("id") Long tagId) {
        //当前修改的标签
        Tag tag = tagService.get(tagId);
        if (tag == null) {
            return this.renderNotFound();
        }
        model.addAttribute("updateTag", tag);

        //所有标签
        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        Page<Tag> tagPage = tagService.findAll(page);
        model.addAttribute("tags", tagPage.getRecords());
        model.addAttribute("pageInfo", PageUtil.convertPageVo(page));
        return "admin/admin_tag";
    }
}

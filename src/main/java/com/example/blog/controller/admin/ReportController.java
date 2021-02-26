package com.example.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.controller.common.BaseController;
import com.example.blog.dto.JsonResult;
import com.example.blog.entity.Report;
import com.example.blog.entity.User;
import com.example.blog.service.PostService;
import com.example.blog.service.ReportService;
import com.example.blog.service.UserService;
import com.example.blog.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <pre>
 *     后台用户反馈管理控制器
 * </pre>
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/report")
public class ReportController extends BaseController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    /**
     * 查询所有分类并渲染report页面
     *
     * @return 模板路径admin/admin_report
     */
    @RequestMapping(method = RequestMethod.GET)
    public String reportList(@RequestParam(value = "page", defaultValue = "0") Integer pageNumber,
                             @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                             @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                             @RequestParam(value = "order", defaultValue = "desc") String order, Model model) {
        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        Page<Report> reportPage = reportService.findAll(page);
        for (Report report : reportPage.getRecords()) {
            report.setUser(userService.get(report.getUserId()));
            report.setPost(postService.get(report.getPostId()));
        }
        model.addAttribute("reportList", reportPage.getRecords());
        model.addAttribute("pageInfo", PageUtil.convertPageVo(page));

        model.addAttribute("isAdmin", loginUserIsAdmin());
        return "admin/admin_report";
    }

    /**
     * 新增/修改反馈
     *
     * @param report report对象
     * @return 重定向到/admin/report
     */
    @RequestMapping(method = RequestMethod.POST, value = "/save")
    @ResponseBody
    public JsonResult saveReport(@ModelAttribute Report report) {
        reportService.insertOrUpdate(report);
        return JsonResult.success("更新成功");
    }

    /**
     * 删除分类
     *
     * @param cateId 分类Id
     * @return JsonResult
     */
    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    @ResponseBody
    public JsonResult checkDelete(@RequestParam("id") Long cateId) {
        reportService.delete(cateId);
        return JsonResult.success("删除成功");
    }


    /**
     * 跳转到修改页面
     *
     * @param cateId cateId
     * @param model  model
     * @return 模板路径admin/admin_report
     */
    @RequestMapping(method = RequestMethod.GET, value = "/edit")
    public String toEditReport(Model model,
                               @RequestParam(value = "page", defaultValue = "0") Integer pageNumber,
                               @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                               @RequestParam(value = "sort", defaultValue = "cateSort") String sort,
                               @RequestParam(value = "order", defaultValue = "desc") String order,
                               @RequestParam("id") Long cateId) {

        //更新的分类
        Report report = reportService.get(cateId);
        if (report == null) {
            return this.renderNotFound();
        }
        model.addAttribute("updateReport", report);

        return "admin/admin_report_edit";
    }

    /**
     * 获取详情
     *
     * @return 模板路径admin/admin_report
     */
    @RequestMapping(method = RequestMethod.GET, value = "/detail")
    @ResponseBody
    public JsonResult reportDetail(@RequestParam("id") Long id) {
        Report report = reportService.get(id);
        return JsonResult.success("查询成功", report);
    }


    /**
     * 批量删除
     *
     * @param ids ID列表
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/batchDelete")
    @ResponseBody
    public JsonResult batchDelete(@RequestParam("ids") List<Long> ids) {
        //批量操作
        if (ids == null || ids.size() == 0 || ids.size() >= 100) {
            return JsonResult.error("参数不合法!");
        }
        List<Report> reportList = reportService.findByBatchIds(ids);
        for (Report report : reportList) {
            reportService.delete(report.getId());
        }
        return JsonResult.success("删除成功");
    }

}

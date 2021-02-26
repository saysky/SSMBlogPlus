package com.example.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.JsonResult;
import com.example.blog.entity.Permission;
import com.example.blog.enums.ResourceTypeEnum;
import com.example.blog.service.PermissionService;
import com.example.blog.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台权限管理控制器
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/permission")
public class PermissionController {


    @Autowired
    private PermissionService permissionService;

    /**
     * 查询所有权限并渲染permission页面
     *
     * @return 模板路径admin/admin_permission
     */
    @RequestMapping(method = RequestMethod.GET)
    public String permissions(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                              @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                              @RequestParam(value = "sort", defaultValue = "id") String sort,
                              @RequestParam(value = "order", defaultValue = "asc") String order, Model model) {
        //权限列表
        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);

        Page<Permission> permissions = permissionService.findAll(page);
        model.addAttribute("permissionList", permissions.getRecords());
        model.addAttribute("pageInfo", PageUtil.convertPageVo(page));

        // 所有权限
        model.addAttribute("permissions", getPermissionList());
        return "admin/admin_permission";
    }

    /**
     * 新增/修改权限
     *
     * @param permission permission对象
     * @return 重定向到/admin/permission
     */
    @RequestMapping(method = RequestMethod.POST, value = "/save")
    public String savePermission(@ModelAttribute Permission permission) {
        permissionService.insertOrUpdate(permission);
        return "redirect:/admin/permission";
    }

    /**
     * 删除权限
     *
     * @param permissionId 权限Id
     * @return JsonResult
     */
    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    @ResponseBody
    public JsonResult checkDelete(@RequestParam("id") Long permissionId) {
//        // 请先删除子权限
        Integer childCount = permissionService.countChildPermission(permissionId);
        if (childCount > 0) {
            return JsonResult.error("请先删除子节点");
        }
        permissionService.delete(permissionId);
        return JsonResult.success();
    }

    /**
     * 跳转到新增页面
     *
     * @param model model
     * @return 模板路径admin/admin_permission
     */
    @RequestMapping(method = RequestMethod.GET, value = "/new")
    public String toAddPermission(Model model) {
        // 带有等级的权限列表
        model.addAttribute("permissionList", permissionService.findPermissionListWithLevel());
        // 权限列表
        model.addAttribute("permissions", getPermissionList());
        return "admin/admin_permission_new";
    }

    /**
     * 跳转到修改页面
     *
     * @param permissionId permissionId
     * @param model        model
     * @return 模板路径admin/admin_permission
     */
    @RequestMapping(method = RequestMethod.GET, value = "/edit")
    public String toEditPermission(Model model, @RequestParam("id") Long permissionId) {
        //更新的权限
        Permission permission = permissionService.get(permissionId);
        model.addAttribute("updatePermission", permission);

        // 带有等级的权限列表
        model.addAttribute("permissionList", permissionService.findPermissionListWithLevel());
        // 权限列表
        model.addAttribute("permissions", getPermissionList());
        // 设置URL为编辑的URL
        return "admin/admin_permission_edit";
    }


    /**
     * 所有权限
     * @return
     */
    public List<Permission> getPermissionList() {
        //权限列表
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.orderByAsc("sort");
        List<Permission> permissions = permissionService.findAll(queryWrapper);
        // 设置URL为编辑的URL
        for (Permission permission : permissions) {
            permission.setUrl("/admin/permission/edit?id=" + permission.getId());
            if (ResourceTypeEnum.MENU.getCode().equals(permission.getResourceType())) {
                permission.setName(permission.getName() + "[" + ResourceTypeEnum.MENU.getDescription() + "]");
            } else if (ResourceTypeEnum.BUTTON.getCode().equals(permission.getResourceType())) {
                permission.setName(permission.getName() + "[" + ResourceTypeEnum.BUTTON.getDescription() + "]");
            } else if (ResourceTypeEnum.PAGE.getCode().equals(permission.getResourceType())) {
                permission.setName(permission.getName() + "[" + ResourceTypeEnum.PAGE.getDescription() + "]");
            }
        }
        return permissions;
    }
}

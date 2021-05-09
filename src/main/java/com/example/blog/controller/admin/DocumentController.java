package com.example.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.controller.common.BaseController;
import com.example.blog.dto.JsonResult;
import com.example.blog.dto.QueryCondition;
import com.example.blog.entity.Document;
import com.example.blog.entity.User;
import com.example.blog.service.DocumentService;
import com.example.blog.service.UserService;
import com.example.blog.util.FileUtil;
import com.example.blog.util.PageUtil;
import com.example.blog.vo.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author saysky
 * @date 2021/3/20
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/document")
public class DocumentController extends BaseController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    /**
     * 处理后台获取资料列表的请求
     *
     * @param model model
     * @return 模板路径admin/admin_document
     */
    @RequestMapping
    public String documents(Model model,
                            @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                            @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                            @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                            @RequestParam(value = "order", defaultValue = "desc") String order,
                            @ModelAttribute SearchVo searchVo) {

        boolean isAdmin = loginUserIsAdmin();
        Document condition = new Document();
        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        if (!isAdmin) {
            condition.setUserId(getLoginUserId());
        }
        Page<Document> documents = documentService.findAll(page, new QueryCondition<>(condition, searchVo));
        for (Document document : documents.getRecords()) {
            document.setUser(userService.get(document.getUserId()));
        }
        model.addAttribute("documents", documents.getRecords());
        model.addAttribute("pageInfo", PageUtil.convertPageVo(page));
        return "admin/admin_document";
    }

    /**
     * 上传文件
     *
     * @param file file
     * @return Map
     */
    @RequestMapping(value = "/upload", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public JsonResult uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> map = FileUtil.simpleUpload(file);
        Document document = new Document();
        document.setName(map.get("fileName"));
        document.setUrl(map.get("fileUrl"));
        document.setPath(map.get("filePath"));
        document.setSize(map.get("fileSize"));
        document.setSuffix(map.get("fileSuffix"));
        User user = getLoginUser();
        document.setUserId(user.getId());
        document.setUpdateBy(user.getUserName());
        document.setCreateBy(user.getUserName());
        documentService.insert(document);
        return JsonResult.success();
    }

    /**
     * 处理删除资料的请求
     *
     * @param id 资料编号
     * @return 重定向到/admin/post
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult removePost(@RequestParam("id") Long id) {
        User user = getLoginUser();
        boolean isAdmin = loginUserIsAdmin();
        Document document = documentService.get(id);
        if (document == null) {
            return JsonResult.error("文档不存在");
        }
        if (!isAdmin && !Objects.equals(document.getUserId(), user.getId())) {
            return JsonResult.error("没有权限删除");
        }
        documentService.delete(id);
        return JsonResult.success("删除成功");
    }

    /**
     * 批量删除
     *
     * @param ids 资源ID列表
     * @return 重定向到/admin/post
     */
    @RequestMapping(value = "/batchDelete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult batchDelete(@RequestParam("ids") List<Long> ids) {
        User user = getLoginUser();
        boolean isAdmin = loginUserIsAdmin();
        for (Long id : ids) {
            Document document = documentService.get(id);
            if (document == null) {
                return JsonResult.error("文档不存在");
            }
            if (!isAdmin && !Objects.equals(document.getUserId(), user.getId())) {
                return JsonResult.error("没有权限删除");
            }
            documentService.delete(id);
        }
        return JsonResult.success("删除成功");
    }


    /**
     * 文件下载
     *
     * @param id       文件ID
     * @param response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadFile(@RequestParam("id") Long id, HttpServletResponse response) throws IOException {
        User user = getLoginUser();
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }
        Document document = documentService.get(id);
        if (document == null) {
            response.sendRedirect("/404");
        }
        // 下载次数+1
        document.setDownloadNum(document.getDownloadNum() + 1);
        documentService.update(document);

        if (document != null) {
            InputStream f = new FileInputStream(new File(document.getPath()));
            response.reset();
            response.setContentType("application/x-msdownload;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(document.getName(), "UTF-8") + "." + document.getSuffix());
            ServletOutputStream sout = response.getOutputStream();
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                bis = new BufferedInputStream(f);
                bos = new BufferedOutputStream(sout);
                byte[] buff = new byte[2048];
                int bytesRead;
                while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                    bos.write(buff, 0, bytesRead);
                }
                bos.flush();
                bos.close();
                bis.close();
            } catch (final IOException e) {
                throw e;
            } finally {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            }
        }
    }
}

package com.example.blog.controller.home;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.controller.common.BaseController;
import com.example.blog.dto.QueryCondition;
import com.example.blog.entity.Document;
import com.example.blog.entity.User;
import com.example.blog.service.DocumentService;
import com.example.blog.service.UserService;
import com.example.blog.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author saysky
 * @date 2021/3/20
 */
@Controller
public class FrontDocumentController extends BaseController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    /**
     * 文档列表
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param keywords
     * @param model
     * @return
     */
    @RequestMapping(value = "/document", method = RequestMethod.GET)
    public String index(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                        @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                        @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                        @RequestParam(value = "order", defaultValue = "desc") String order,
                        @RequestParam(value = "keywords", required = false) String keywords,
                        Model model) {

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        Document condition = new Document();
        condition.setName(keywords);
        Page<Document> documents = documentService.findAll(page, new QueryCondition<>(condition));
        for (Document document : documents.getRecords()) {
            document.setUser(userService.get(document.getUserId()));
        }
        model.addAttribute("documents", documents.getRecords());
        model.addAttribute("page", documents);
        return "home/document";
    }


}

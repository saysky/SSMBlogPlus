package com.example.blog.controller.home;

import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.controller.common.BaseController;
import com.example.blog.dto.JsonResult;
import com.example.blog.dto.PostQueryCondition;
import com.example.blog.dto.QueryCondition;
import com.example.blog.entity.*;
import com.example.blog.exception.MyBusinessException;
import com.example.blog.service.*;
import com.example.blog.util.CommentUtil;
import com.example.blog.util.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author 言曌
 * @date 2020/3/11 4:59 下午
 */
@Controller
public class FrontPostController extends BaseController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ReportService reportService;


    public static final String NEW = "new";
    public static final String PUBLISH = "publish";
    public static final String HOT = "hot";
    public static final String USER = "user";
    public static final String CATEGORY = "category";
    public static final String TAG = "tag";

    /**
     * 加载分页数据
     *
     * @param pageNumber
     * @param pageSize
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/post/list")
    @ResponseBody
    public JsonResult loadPostList(@RequestParam(value = "keywords", required = false) String keywords,
                                   @RequestParam(value = "type", defaultValue = "new") String type,
                                   @RequestParam(value = "id", required = false) Long id,
                                   @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                                   @RequestParam(value = "size", defaultValue = "10") Integer pageSize) {
        User loginUser = getLoginUser();
        Page<Post> postPage = null;
        Page page = PageUtil.initMpPage(pageNumber, pageSize, "isSticky desc, createTime", "desc");
        PostQueryCondition condition = new PostQueryCondition();
        if (StringUtils.isNotBlank(keywords)) {
            condition.setKeywords(keywords);
        }
        if (NEW.equalsIgnoreCase(type)) {
            // 最新文章
        } else if (HOT.equalsIgnoreCase(type)) {
            // 最热文章
            page = PageUtil.initMpPage(pageNumber, pageSize, "commentSize", "desc");
        } else if (USER.equalsIgnoreCase(type)) {
            // 某个用户的
            condition.setUserId(id);
        } else if (CATEGORY.equalsIgnoreCase(type) || (TAG.equalsIgnoreCase(type))) {
            // 某个文章分类的文章
            condition.setCateId(id);
        } else if (PUBLISH.equalsIgnoreCase(type)) {
            // 我发布的
            if (loginUser == null) {
                return JsonResult.error("请先登录", "notLogin");
            }
            condition.setUserId(loginUser.getId());
        }

        // 获得列表
        postPage = postService.findPostByCondition(condition, page);
        return JsonResult.success("查询成功", postPage);
    }


    /**
     * 点赞文章
     *
     * @param postId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/post/like")
    @ResponseBody
    public JsonResult likePost(@RequestParam("postId") Long postId) {
        User user = getLoginUser();
        if (user == null) {
            return JsonResult.error("请先登录");
        }

        Post post = postService.get(postId);
        if (post == null) {
            return JsonResult.error("文章不存在");
        }

        postService.addLike(postId, user);
        return JsonResult.success();
    }

    /**
     * 点踩文章
     *
     * @param postId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/post/dislike")
    @ResponseBody
    public JsonResult disLikePost(@RequestParam("postId") Long postId) {
        User user = getLoginUser();
        if (user == null) {
            return JsonResult.error("请先登录");
        }

        Post post = postService.get(postId);
        if (post == null) {
            return JsonResult.error("文章不存在");
        }

        postService.addDisLike(postId, user);
        return JsonResult.success();
    }

    /**
     * 举报文章
     *
     * @param postId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/post/report")
    @ResponseBody
    public JsonResult reportPost(@RequestParam("postId") Long postId,
                                 @RequestParam("content") String content) {
        User user = getLoginUser();
        if (user == null) {
            return JsonResult.error("请先登录");
        }

        // 查询待处理的反馈
        reportService.findByUserIdAndStatus(user.getId(), 0);

        Report report = new Report();
        report.setUserId(user.getId());
        report.setPostId(postId);
        if (content.length() > 2000) {
            throw new MyBusinessException("字数太多");
        }
        report.setStatus(0);//待处理
        report.setContent(HtmlUtil.escape(content));
        report.setCreateTime(new Date());
        report.setUpdateTime(new Date());
        report.setCreateBy(user.getUserName());
        report.setUpdateBy(user.getUserName());
        reportService.insert(report);
        return JsonResult.success("反馈成功");
    }


    /**
     * 文章详情
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = {"/post/{id}", "/notice/{id}"})
    public String postDetails(@PathVariable("id") Long id, Model model) {
        // 文章
        Post post = postService.get(id);
        if (post == null) {
            return renderNotFound();
        }
        model.addAttribute("post", post);

        // 作者
        User user = userService.get(post.getUserId());
        model.addAttribute("user", user);

        // 分类
        Category category = categoryService.findByPostId(id);
        model.addAttribute("category", category);

        // 标签列表
        List<Tag> tagList = tagService.findByPostId(id);
        model.addAttribute("tagList", tagList);

        // 评论列表
        List<Comment> commentList = commentService.findByPostId(id);
        model.addAttribute("commentList", CommentUtil.getComments(commentList));

        // 访问量加1
        postService.updatePostView(id);
        return "home/post";
    }

    /**
     * 收藏问题
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/post/mark", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult mark(@RequestParam("id") Long id) {
        User user = getLoginUser();
        if (user == null) {
            return JsonResult.error("请先登录");
        }

        postService.addMark(id, user);
        return JsonResult.success("收藏成功");
    }

    /**
     * 收藏文章
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/post/unmark", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult unmark(@RequestParam("id") Long id) {
        User user = getLoginUser();
        if (user == null) {
            return JsonResult.error("请先登录");
        }

        postService.deleteMark(id, user);
        return JsonResult.success("取消收藏成功");
    }


}

package com.example.blog.controller.admin;

import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.controller.common.BaseController;
import com.example.blog.dto.JsonResult;
import com.example.blog.dto.PostQueryCondition;
import com.example.blog.dto.QueryCondition;
import com.example.blog.entity.*;
import com.example.blog.enums.*;
import com.example.blog.exception.MyBusinessException;
import com.example.blog.service.*;
import com.example.blog.util.PageUtil;
import com.example.blog.vo.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <pre>
 *     后台文章管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/post")
public class PostController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private BlackWordService blackWordService;

    @Autowired
    private TagService tagService;

    @Autowired
    private FollowService followService;


    public static final String TITLE = "title";

    public static final String CONTENT = "content";


    /**
     * 处理后台获取文章列表的请求
     *
     * @param model model
     * @return 模板路径admin/admin_post
     */
    @RequestMapping(method = RequestMethod.GET)
    public String posts(Model model,
                        @RequestParam(value = "status", defaultValue = "0") Integer status,
                        @RequestParam(value = "keywords", defaultValue = "") String keywords,
                        @RequestParam(value = "searchType", defaultValue = "") String searchType,
                        @RequestParam(value = "postSource", defaultValue = "-1") Integer postSource,
                        @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                        @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                        @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                        @RequestParam(value = "order", defaultValue = "desc") String order,
                        @ModelAttribute SearchVo searchVo) {

        Long loginUserId = getLoginUserId();
        boolean isAdmin = loginUserIsAdmin();
        PostQueryCondition condition = new PostQueryCondition();
        if (!StringUtils.isBlank(keywords)) {
            if (TITLE.equals(searchType)) {
                condition.setTitle(keywords);
            } else {
                condition.setContent(keywords);
            }
        }
        condition.setPostType(PostTypeEnum.POST_TYPE_POST.getValue());
        condition.setStatus(status);
        // 管理员可以查看所有用户的，非管理员只能看到自己的文章和互相关注的用户的文章
        if (!isAdmin) {
            List<Long> userIds = followService.findMutualFollowingByUserId(loginUserId);
            userIds.add(loginUserId);
            condition.setUserIds(userIds);
        }

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        Page<Post> posts = postService.findPostByCondition(condition, page);


        //封装分类和标签
        model.addAttribute("posts", posts.getRecords());
        model.addAttribute("pageInfo", PageUtil.convertPageVo(page));
        model.addAttribute("status", status);
        model.addAttribute("keywords", keywords);
        model.addAttribute("searchType", searchType);
        model.addAttribute("postSource", postSource);
        model.addAttribute("order", order);
        model.addAttribute("sort", sort);

        model.addAttribute("isAdmin", isAdmin);
        return "admin/admin_post";
    }


    /**
     * 处理跳转到新建文章页面
     *
     * @return 模板路径admin/admin_editor
     */
    @RequestMapping(method = RequestMethod.GET, value = "/new")
    public String newPost(Model model) {
        //所有分类
        List<Category> allCategories = categoryService.findAll();
        model.addAttribute("categories", allCategories);
        return "admin/admin_post_new";
    }


    /**
     * 添加/更新文章
     *
     * @param post   Post实体
     * @param cateId 分类ID
     * @param tags   标签列表
     */
    @RequestMapping(method = RequestMethod.POST, value = "/save")
    @ResponseBody
    public JsonResult pushPost(@ModelAttribute Post post,
                               @RequestParam("cateId") Long cateId,
                               @RequestParam("tags") String tags) {
        // 0.判断是否完整
        if (StringUtils.isEmpty(post.getPostTitle()) || StringUtils.isEmpty(post.getPostContent())) {
            return JsonResult.error("请输入完整信息");
        }

        // 1.判断评论内容是否包含屏蔽字
        List<BlackWord> blackWordList = blackWordService.findAll();
        for (BlackWord blackWord : blackWordList) {
            if (post.getPostTitle().contains(blackWord.getContent()) || post.getPostContent().contains(blackWord.getContent())) {
                return JsonResult.error("文章内容包含违规字符，禁止发布");
            }
        }

        // 2，检验标签数量
        checkTags(tags);

        // 3.获得登录用户
        User user = getLoginUser();
        post.setUserId(getLoginUserId());

        // 4、非管理员只能修改自己的文章，管理员都可以修改
        Post originPost = null;
        if (post.getId() != null) {
            originPost = postService.get(post.getId());
            // 如果不属于互相关注的用户
            if (!followService.isMutualFollowing(user.getId(), post.getUserId()) && !loginUserIsAdmin()) {
                throw new MyBusinessException("没有权限");
            }

            //以下属性不能修改
            post.setUserId(originPost.getUserId());
            post.setPostViews(originPost.getPostViews());
            post.setCommentSize(originPost.getCommentSize());
            post.setPostLikes(originPost.getPostLikes());
            post.setCommentSize(originPost.getCommentSize());
            post.setDelFlag(originPost.getDelFlag());
        }
        // 5、提取摘要
        int postSummary = 100;
        //文章摘要
        String summaryText = HtmlUtil.cleanHtmlTag(post.getPostContent());
        if (summaryText.length() > postSummary) {
            String summary = summaryText.substring(0, postSummary);
            post.setPostSummary(summary);
        } else {
            post.setPostSummary(summaryText);
        }

        // 6、分类标签
        Category category = new Category();
        category.setId(cateId);
        post.setCategory(category);
        if (StringUtils.isNotEmpty(tags)) {
            List<Tag> tagList = tagService.strListToTagList(StringUtils.deleteWhitespace(tags));
            post.setTagList(tagList);
        }

        // 7.类型
        post.setPostType(PostTypeEnum.POST_TYPE_POST.getValue());

        // 8.添加/更新入库
        postService.insertOrUpdate(post);
        return JsonResult.success("发布成功");
    }

    /**
     * 限制一篇文章最多5个标签
     *
     * @param tagList
     */
    private void checkTags(String tagList) {
        String[] tags = tagList.split(",");
        if (tags.length > 5) {
            throw new MyBusinessException("每篇文章最多5个标签");
        }
        for (String tag : tags) {
            if (tag.length() > 20) {
                throw new MyBusinessException("每个标签长度最多为20个字符");
            }
        }
    }

    /**
     * 处理移至回收站的请求
     *
     * @param postId 文章编号
     * @return 重定向到/admin/post
     */
    @RequestMapping(method = RequestMethod.POST, value = "/throw")
    @ResponseBody
    public JsonResult moveToTrash(@RequestParam("id") Long postId) {
        Post post = postService.get(postId);
        basicCheck(post);
        post.setPostStatus(PostStatusEnum.RECYCLE.getCode());
        postService.update(post);
        return JsonResult.success("操作成功");

    }

    /**
     * 处理文章为发布的状态
     *
     * @param postId 文章编号
     * @return 重定向到/admin/post
     */
    @RequestMapping(method = RequestMethod.POST, value = "/revert")
    @ResponseBody
    public JsonResult moveToPublish(@RequestParam("id") Long postId) {
        Post post = postService.get(postId);
        basicCheck(post);
        post.setPostStatus(PostStatusEnum.PUBLISHED.getCode());
        postService.update(post);
        return JsonResult.success("操作成功");
    }


    /**
     * 处理删除文章的请求
     *
     * @param postId 文章编号
     * @return 重定向到/admin/post
     */
    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    @ResponseBody
    public JsonResult removePost(@RequestParam("id") Long postId) {
        Post post = postService.get(postId);
        basicCheck(post);
        postService.delete(postId);
        return JsonResult.success("删除成功");
    }

    /**
     * 批量删除
     *
     * @param ids 文章ID列表
     * @return 重定向到/admin/post
     */
    @RequestMapping(method = RequestMethod.POST, value = "/batchDelete")
    @ResponseBody
    public JsonResult batchDelete(@RequestParam("ids") List<Long> ids) {
        Long userId = getLoginUserId();
        //批量操作
        //1、防止恶意操作
        if (ids == null || ids.size() == 0 || ids.size() >= 100) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), "参数不合法!");
        }
        //2、检查用户权限
        //文章作者才可以删除
        List<Post> postList = postService.findByBatchIds(ids);
        for (Post post : postList) {
            if (!Objects.equals(post.getUserId(), userId) && !loginUserIsAdmin()) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), "没有权限");
            }
        }
        //3、如果当前状态为回收站，则删除；否则，移到回收站
        for (Post post : postList) {
            if (Objects.equals(post.getPostStatus(), PostStatusEnum.RECYCLE.getCode())) {
                postService.delete(post.getId());
            } else {
                post.setPostStatus(PostStatusEnum.RECYCLE.getCode());
                postService.update(post);
            }
        }
        return JsonResult.success("删除成功");
    }


    /**
     * 检查文章是否存在和用户是否有权限控制
     *
     * @param post
     */
    private void basicCheck(Post post) {
        if (post == null) {
            throw new MyBusinessException("文章不存在");
        }
        //只有创建者有权删除
        User user = getLoginUser();
        // 如果不属于互相关注的用户
        if (!followService.isMutualFollowing(user.getId(), post.getUserId()) && !loginUserIsAdmin()) {
            throw new MyBusinessException("没有权限");
        }
    }

    /**
     * 跳转到编辑文章页面
     *
     * @param postId 文章编号
     * @param model  model
     * @return 模板路径admin/admin_editor
     */
    @RequestMapping(method = RequestMethod.GET, value = "/edit")
    public String editPost(@RequestParam("id") Long postId, Model model) {
        Post post = postService.get(postId);
        if (post == null) {
            throw new MyBusinessException("文章不存在");
        }
        User user = getLoginUser();
        // 如果不属于互相关注的用户
//        if (!loginUserIsAdmin() && !followService.isMutualFollowing(user.getId(), post.getUserId())) {
//            throw new MyBusinessException("没有权限");
//        }
        if(!Objects.equals(user.getId(), post.getUserId())) {
            throw new MyBusinessException("没有权限");
        }

        //当前文章标签
        List<Tag> tagList = tagService.findByPostId(postId);
        String tags = tagService.tagListToStr(tagList);
        model.addAttribute("tags", tags);

        //当前文章分类
        Category category = categoryService.findByPostId(postId);
        post.setCategory(category);
        model.addAttribute("post", post);

        //所有分类
        List<Category> allCategories = categoryService.findAll();
        model.addAttribute("categories", allCategories);
        return "admin/admin_post_edit";
    }


    /**
     * 置顶文章
     *
     * @param postId 文章编号
     * @return 响应
     */
    @RequestMapping(method = RequestMethod.POST, value = "/stick")
    @ResponseBody
    public JsonResult stick(@RequestParam("id") Long postId) {
        Post post = postService.get(postId);
        post.setIsSticky(PostIsStickyEnum.TRUE.getValue());
        postService.update(post);
        return JsonResult.success("置顶成功");
    }

    /**
     * 取消置顶文章
     *
     * @param postId 文章编号
     * @return 响应
     */
    @RequestMapping(method = RequestMethod.POST, value = "/unStick")
    @ResponseBody
    public JsonResult unStick(@RequestParam("id") Long postId) {
        Post post = postService.get(postId);
        post.setIsSticky(PostIsStickyEnum.FALSE.getValue());
        postService.update(post);
        return JsonResult.success("取消置顶成功");
    }

    /**
     * 推荐文章
     *
     * @param postId 文章编号
     * @return 响应
     */
    @RequestMapping(method = RequestMethod.POST, value = "/recommend")
    @ResponseBody
    public JsonResult recommend(@RequestParam("id") Long postId) {
        Post post = postService.get(postId);
        Boolean isAdmin = loginUserIsAdmin();
        if (!isAdmin) {
            throw new MyBusinessException("没有权限");
        }
        post.setIsRecommend(PostIsRecommendEnum.TRUE.getValue());
        postService.update(post);
        return JsonResult.success("推荐成功");
    }

    /**
     * 取消推荐文章
     *
     * @param postId 文章编号
     * @return 响应
     */
    @RequestMapping(method = RequestMethod.POST, value = "/unRecommend")
    @ResponseBody
    public JsonResult unRecommend(@RequestParam("id") Long postId) {
        Post post = postService.get(postId);
        Boolean isAdmin = loginUserIsAdmin();
        if (!isAdmin) {
            throw new MyBusinessException("没有权限");
        }
        post.setIsRecommend(PostIsRecommendEnum.FALSE.getValue());
        postService.update(post);
        return JsonResult.success("取消推荐成功");
    }
}

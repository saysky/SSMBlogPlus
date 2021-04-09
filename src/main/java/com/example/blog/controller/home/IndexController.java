package com.example.blog.controller.home;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.controller.common.BaseController;
import com.example.blog.dto.JsonResult;
import com.example.blog.dto.PostQueryCondition;
import com.example.blog.dto.QueryCondition;
import com.example.blog.entity.*;
import com.example.blog.enums.PostTypeEnum;
import com.example.blog.service.*;
import com.example.blog.util.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 言曌
 * @date 2020/3/9 11:00 上午
 */

@Controller
public class IndexController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private PostMarkRefService postMarkRefService;

    @Autowired
    private PostLikeRefService postLikeRefService;

    @Autowired
    private UserService userService;

    @Autowired
    private FollowService followService;

    @Autowired
    private ClockInService clockInService;

    /**
     * 最新文章
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param model
     * @return
     */
    @RequestMapping(value = {"/", "/post/new"}, method = RequestMethod.GET)
    public String index(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                        @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                        @RequestParam(value = "sort", defaultValue = "isSticky desc, createTime") String sort,
                        @RequestParam(value = "order", defaultValue = "desc") String order,
                        @RequestParam(value = "keywords", required = false) String keywords,
                        Model model) throws UnsupportedEncodingException {
        if (StringUtils.isNotEmpty(keywords)) {
            return "redirect:/post/search/" + URLEncoder.encode(keywords.trim(), "UTF-8");
        }
        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        PostQueryCondition condition = new PostQueryCondition();
        condition.setPostType(PostTypeEnum.POST_TYPE_POST.getValue());
        Page<Post> postPage = postService.findPostByCondition(condition, page);
        model.addAttribute("page", postPage);

        model.addAttribute("type", "new");
        return "home/index";
    }

    /**
     * 热门文章
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param model
     * @return
     */
    @RequestMapping(value = "/post/hot", method = RequestMethod.GET)
    public String hot(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                      @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                      @RequestParam(value = "sort", defaultValue = "commentSize desc, postViews") String sort,
                      @RequestParam(value = "order", defaultValue = "desc") String order,
                      Model model) {

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        PostQueryCondition condition = new PostQueryCondition();
        condition.setPostType(PostTypeEnum.POST_TYPE_POST.getValue());

        Page<Post> postPage = postService.findPostByCondition(condition, page);
        model.addAttribute("page", postPage);

        model.addAttribute("type", "hot");
        return "home/index";
    }

    /**
     * 我发布的文章
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param model
     * @return
     */
    @RequestMapping(value = "/post/publish", method = RequestMethod.GET)
    public String publish(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                          @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                          @RequestParam(value = "sort", defaultValue = "commentSize desc, postViews") String sort,
                          @RequestParam(value = "order", defaultValue = "desc") String order,
                          Model model) {
        Long userId = getLoginUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        PostQueryCondition condition = new PostQueryCondition();
        condition.setPostType(PostTypeEnum.POST_TYPE_POST.getValue());

        condition.setUserId(userId);
        Page<Post> postPage = postService.findPostByCondition(condition, page);
        model.addAttribute("page", postPage);

        model.addAttribute("type", "publish");
        return "home/index";
    }

    /**
     * 我的订阅
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param model
     * @return
     */
    @RequestMapping(value = "/post/subscribe", method = RequestMethod.GET)
    public String subscribe(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                            @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                            @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                            @RequestParam(value = "order", defaultValue = "desc") String order,
                            Model model) {
        Long userId = getLoginUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        PostQueryCondition condition = new PostQueryCondition();
        condition.setPostType(PostTypeEnum.POST_TYPE_POST.getValue());

        List<Follow> followList = followService.findByUserId(userId);
        Page<Post> postPage = null;
        if (followList.size() > 0) {
            List<Long> userIds = followList.stream().map(p -> p.getAcceptUserId()).collect(Collectors.toList());
            condition.setUserIds(userIds);
            postPage = postService.findPostByCondition(condition, page);
        } else {
            postPage = new Page<>();
        }
        model.addAttribute("page", postPage);
        model.addAttribute("type", "subscribe");
        return "home/index";
    }


    /**
     * 公告
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param model
     * @return
     */
    @RequestMapping(value = "/post/notice", method = RequestMethod.GET)
    public String notice(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                         @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                         @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                         @RequestParam(value = "order", defaultValue = "desc") String order,
                         Model model) {
        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        QueryCondition queryCondition = new QueryCondition();
        Post condition = new Post();
        condition.setPostType(PostTypeEnum.POST_TYPE_NOTICE.getValue());
        queryCondition.setData(condition);
        Page<Post> postPage = postService.findAll(page, queryCondition);
        for (Post post : postPage.getRecords()) {
            post.setUser(userService.get(post.getUserId()));
        }
        model.addAttribute("page", postPage);
        model.addAttribute("type", "notice");
        return "home/notice";
    }


    /**
     * 搜索结果
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param model
     * @return
     */
    @RequestMapping(value = "/post/search/{keywords}", method = RequestMethod.GET)
    public String search(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                         @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                         @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                         @RequestParam(value = "order", defaultValue = "desc") String order,
                         @PathVariable(value = "keywords") String keywords,
                         Model model) {

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        PostQueryCondition condition = new PostQueryCondition();
        if (StringUtils.isNotEmpty(keywords)) {
            condition.setKeywords(keywords);
        }
        Page<Post> postPage = postService.findPostByCondition(condition, page);
        model.addAttribute("page", postPage);

        model.addAttribute("type", "new");
        return "home/index";
    }

    /**
     * 文章收藏列表
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param model
     * @return
     */
    @RequestMapping(value = "/post/mark", method = RequestMethod.GET)
    public String mark(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                       @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                       @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                       @RequestParam(value = "order", defaultValue = "desc") String order,
                       Model model) {

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        PostMarkRef condition = new PostMarkRef();
        Long userId = getLoginUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        Page<PostMarkRef> postPage = postMarkRefService.findAll(page, new QueryCondition<>(condition));

        for (PostMarkRef postMarkRef : postPage.getRecords()) {
            Post post = postService.get(postMarkRef.getPostId());
            post.setUser(userService.get(post.getUserId()));
            postMarkRef.setPost(post);
        }
        model.addAttribute("page", postPage);

        model.addAttribute("type", "mark");
        return "home/post_mark";
    }

    /**
     * 文章点赞列表
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param model
     * @return
     */
    @RequestMapping(value = "/post/like", method = RequestMethod.GET)
    public String like(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                       @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                       @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                       @RequestParam(value = "order", defaultValue = "desc") String order,
                       Model model) {

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        PostLikeRef condition = new PostLikeRef();
        Long userId = getLoginUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        Page<PostLikeRef> postPage = postLikeRefService.findAll(page, new QueryCondition<>(condition));

        for (PostLikeRef postLikeRef : postPage.getRecords()) {
            postLikeRef.setUser(userService.get(postLikeRef.getUserId()));
            postLikeRef.setPost(postService.get(postLikeRef.getPostId()));
        }
        model.addAttribute("page", postPage);

        model.addAttribute("type", "like");
        return "home/post_like";
    }


    /**
     * 我的关注
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param model
     * @return
     */
    @RequestMapping(value = "/user/follow", method = RequestMethod.GET)
    public String follow(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                         @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                         @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                         @RequestParam(value = "order", defaultValue = "desc") String order,
                         Model model) {

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        Follow condition = new Follow();
        Long userId = getLoginUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        condition.setUserId(userId);
        Page<Follow> postPage = followService.findAll(page, new QueryCondition<>(condition));
        for (Follow follow : postPage.getRecords()) {
            follow.setIsMutualFollowing(followService.isMutualFollowing(follow.getUserId(), follow.getAcceptUserId()));
            follow.setUser(userService.get(follow.getAcceptUserId()));
        }
        model.addAttribute("follows", postPage.getRecords());
        model.addAttribute("page", postPage);
        model.addAttribute("type", "follow");

        return "home/user_follow";
    }

    /**
     * 我的粉丝
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param model
     * @return
     */
    @RequestMapping(value = "/user/fans", method = RequestMethod.GET)
    public String fans(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                       @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                       @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                       @RequestParam(value = "order", defaultValue = "desc") String order,
                       Model model) {

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        Follow condition = new Follow();
        Long userId = getLoginUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        condition.setAcceptUserId(userId);
        Page<Follow> postPage = followService.findAll(page, new QueryCondition<>(condition));
        for (Follow follow : postPage.getRecords()) {
            follow.setAcceptUser(userService.get(follow.getUserId()));
            follow.setIsMutualFollowing(followService.isMutualFollowing(follow.getUserId(), follow.getAcceptUserId()));

        }
        model.addAttribute("follows", postPage.getRecords());
        model.addAttribute("page", postPage);
        model.addAttribute("type", "fans");

        return "home/user_fans";
    }

    /**
     * 今日签到页面
     *
     * @return
     */
    @RequestMapping(value = "/clockIn")
    public String clockInPage(Model model) {
        Long userId = getLoginUserId();
        boolean hasClockIn = false;
        if (userId != null) {
            ClockIn clockIn = clockInService.findByUserIdToday(userId);
            hasClockIn = clockIn != null;
        }
        List<ClockIn> clockInList = clockInService.getRankToday();
        Integer countToday = clockInService.countToday();
        model.addAttribute("clockInList", clockInList);
        model.addAttribute("countToday", countToday);
        model.addAttribute("hasClockIn", hasClockIn);
        return "home/clockin";
    }

    /**
     * 签到
     *
     * @return
     */
    @RequestMapping(value = "/clockIn/add")
    @ResponseBody
    public JsonResult addClockIn(@RequestParam("heartValue") String heartValue) {
        Long userId = getLoginUserId();
        if (userId == null) {
            return JsonResult.error("请先登录");
        }
        ClockIn clockIn = clockInService.findByUserIdToday(userId);
        if (clockIn != null) {
            return JsonResult.error("今日已签到了，明天再来吧！");
        }
        clockIn = new ClockIn();
        clockIn.setHeartValue(heartValue);
        clockIn.setUserId(userId);
        clockIn.setCreateTime(new Date());
        clockInService.insert(clockIn);
        return JsonResult.success("签到成功");
    }

}

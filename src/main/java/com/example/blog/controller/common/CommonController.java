package com.example.blog.controller.common;

import com.example.blog.enums.CommonParamsEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * 错误页面控制器
 */
@Slf4j
@Controller
public class CommonController  {


    /**
     * 渲染404，500
     *
     * @param request request
     * @return String
     */
    @RequestMapping(method = RequestMethod.GET, value = "/error")
    public String handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode.equals(CommonParamsEnum.NOT_FOUND.getValue())) {
            return "redirect:/404";
        } else {
            return "redirect:/500";
        }
    }

    /**
     * 渲染403页面
     *
     * @return String
     */
    @RequestMapping(method = RequestMethod.GET, value = "/403")
    public String fourZeroThree() {
        return "common/error/403";
    }


    /**
     * 渲染404页面
     *
     * @return String
     */
    @RequestMapping(method = RequestMethod.GET, value = "/404")
    public String fourZeroFour() {
        return "common/error/404";
    }


    /**
     * 渲染500页面
     *
     * @return String
     */
    @RequestMapping(method = RequestMethod.GET, value = "/500")
    public String fiveZeroZero() {
        return "common/error/500";
    }


}

package com.evtape.schedule.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author ripper 用戶列表
 */
@Controller
//@RequestMapping("/login")
public class LoginController {

    /**
     * 登录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage(HttpServletRequest request) {
        if (request.getSession().getAttribute("DT_LOGIN_NAME") != null) {
            return "redirect:/";
        }
        return "login";
    }
    
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logoutPage() {
		// TODO SecurityUtils.getSubject().logout();
		return "redirect:/";
	}

//    @RequestMapping(value = "/login", method = RequestMethod.POST)
//    public String login(HttpServletRequest request, HttpServletResponse response, RedirectAttributesModelMap modelMap) {
//        String userCode = request.getParameter("userCode");
//        String password = request.getParameter("password");
//        Subject subject = SecurityUtils.getSubject();
//        UsernamePasswordToken token = new UsernamePasswordToken(userCode, password);
//        if (!ExtStringUtil.isBlank(userCode) && !ExtStringUtil.isBlank(password)) {
//            try {
//                subject.login(token);
//                User user = (User) subject.getPrincipal();
//                log.info("{}:{} login.", userCode, user.getUserName());
//                subject.getSession().setAttribute("DT_LOGIN_USER", user);
//                CookieUtil.setCookie(response, "", user.getUserId());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                modelMap.addFlashAttribute("message", "登录失败，请联系管理员！");
//                return "redirect:/login";
//            }
//        }
//        return "redirect:/";
//    }
}

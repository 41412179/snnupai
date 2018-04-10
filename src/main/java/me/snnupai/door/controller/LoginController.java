package me.snnupai.door.controller;

import me.snnupai.door.async.EventProducer;
import me.snnupai.door.service.UserService;
import me.snnupai.door.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    EventProducer eventProducer;

    @Autowired
    UserService userService;

    @RequestMapping(path = {"/reg/"}, method = {RequestMethod.POST})
    public String reg(Model model, @RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam(value="rememberme", defaultValue = "false") boolean rememberme,
                      @RequestParam("email") String email,
                      @RequestParam("phone") String phone,
                      @RequestParam("confirmPassword") String confimPassword,
                      @RequestParam(value = "next", required = false) String next,
                      HttpServletResponse response) {
        try {
            if(!password.equals(confimPassword)){
                model.addAttribute("msg", "你输入的两次密码不一致");
                return "login";
            }
            Map<String, Object> map = userService.register(username, password, email, phone);
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                if (rememberme) {
                    cookie.setMaxAge(3600*24*7); //单位是秒
                }
                response.addCookie(cookie);

                if (StringUtils.isNotBlank(next)) {
                    return "redirect:" + next;
                }else {
                    return "redirect:/";
                }
            } else {
                model.addAttribute("msg", map.get("msg"));
                return "login";
            }
        } catch (Exception e) {
            logger.error("注册异常" + e.getMessage());
            model.addAttribute("msg", "服务器错误");
            return "login";
        }
    }

    @RequestMapping(path = {"/reglogin"}, method = {RequestMethod.GET})
    public String regloginPage(Model model, @RequestParam(value = "next", required = false) String next) {
        model.addAttribute("next", next);
        return "login";
    }

    @RequestMapping(path = {"/login"}, method = {RequestMethod.POST})
    public String login(Model model, @RequestParam("userstr") String userstr,
                        @RequestParam("password") String password,
                        @RequestParam(value="next", required = false) String next,
                        @RequestParam(value="rememberme", defaultValue = "false") boolean rememberme,
                        HttpServletResponse response) {
        try {
            Map<String, Object> map = userService.login(userstr, password);
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                if (rememberme) {
                    cookie.setMaxAge(3600*24*7);
                }else{
                    cookie.setMaxAge(3600*24);
                }
                response.addCookie(cookie);

//
//                eventProducer.fireEvent(new EventModel(EventType.LOGIN)
//                        .setExt("username", username)
//                        .setExt("email", "1593028064@qq.com")
//                        .setActorId((Long)map.get("userId")));


                if (StringUtils.isNotBlank(next)) {
                    return "redirect:" + next;
                }else {
                    return "redirect:/";
                }
            } else {
                model.addAttribute("msg", map.get("msg"));
                return "login";
            }

        } catch (Exception e) {
            logger.error("登陆异常" + e.getMessage());
            return "login";
        }
    }

    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/";
    }
}
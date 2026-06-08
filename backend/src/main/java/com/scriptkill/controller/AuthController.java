package com.scriptkill.controller;

import com.scriptkill.common.Result;
import com.scriptkill.dto.LoginDTO;
import com.scriptkill.service.SysAdminService;
import com.scriptkill.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysAdminService sysAdminService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        LoginVO vo = sysAdminService.login(dto);
        return Result.success(vo);
    }

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("剧本杀平台API正常运行");
    }
}

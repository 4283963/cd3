package com.scriptkill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.common.BusinessException;
import com.scriptkill.common.JwtUtil;
import com.scriptkill.dto.LoginDTO;
import com.scriptkill.entity.SysAdmin;
import com.scriptkill.mapper.SysAdminMapper;
import com.scriptkill.vo.LoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class SysAdminService extends ServiceImpl<SysAdminMapper, SysAdmin> {

    @Autowired
    private JwtUtil jwtUtil;

    public LoginVO login(LoginDTO dto) {
        if (dto.getUsername() == null || dto.getPassword() == null) {
            throw new BusinessException("用户名和密码不能为空");
        }
        QueryWrapper<SysAdmin> wrapper = new QueryWrapper<>();
        wrapper.eq("username", dto.getUsername());
        SysAdmin admin = getOne(wrapper);
        if (admin == null) {
            throw new BusinessException("用户不存在");
        }
        if (admin.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        String md5Password = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes());
        if (!md5Password.equals(admin.getPassword())) {
            throw new BusinessException("密码错误");
        }
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), "admin");
        LoginVO vo = new LoginVO();
        BeanUtils.copyProperties(admin, vo);
        vo.setUserId(admin.getId());
        vo.setToken(token);
        return vo;
    }

    public SysAdmin getById(Long id) {
        return baseMapper.selectById(id);
    }
}

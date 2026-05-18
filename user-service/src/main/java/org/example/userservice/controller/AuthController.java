package org.example.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.userservice.common.Result;
import org.example.userservice.dto.LoginRequest;
import org.example.userservice.dto.LoginVO;
import org.example.userservice.entity.SysMenu;
import org.example.userservice.entity.SysRole;
import org.example.userservice.entity.SysUser;
import org.example.userservice.mapper.SysMenuMapper;
import org.example.userservice.mapper.SysRoleMapper;
import org.example.userservice.mapper.SysUserMapper;
import org.example.userservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户登录、Token验证等接口")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Operation(
        summary = "用户登录",
        description = "<font color='red'>【核心接口】</font><br/>" +
                "用户登录并获取 JWT Token、用户信息、角色和菜单权限<br/><br/>" +
                "<font color='green'>业务流程：</font><br/>" +
                "1. <b>验证用户名密码</b> - 从数据库查询用户并验证密码<br/>" +
                "2. <b>查询用户角色</b> - 从 sys_user_role 和 sys_role 表查询<br/>" +
                "3. <b>查询用户菜单</b> - 从 sys_menu 表查询该角色的所有菜单<br/>" +
                "4. <b>生成 JWT Token</b> - 包含用户ID、用户名、角色信息<br/>" +
                "5. <b>返回登录信息</b> - Token + 用户信息 + 角色 + 菜单<br/><br/>" +
                "<font color='orange'>测试账号：</font><br/>" +
                "- user / 123456（普通用户）<br/>" +
                "- staff / 123456（店员）<br/>" +
                "- manager / 123456（店长）<br/>" +
                "- admin / 123456（超级管理员）<br/><br/>" +
                "<font color='blue'>注意：</font>当前为演示版本，密码使用明文存储，生产环境必须使用 BCrypt 加密！"
    )
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("\n========== 用户登录 ==========");
        System.out.println("用户名: " + request.getUsername());
        
        // 1. 从数据库查询用户
        SysUser user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        
        // 2. 验证密码（当前为演示版本使用明文，生产环境应使用 BCrypt）
        // boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!request.getPassword().equals(user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        
        // 3. 构建用户信息
        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatar(user.getAvatar() != null ? user.getAvatar() : "https://ui-avatars.com/api/?name=" + user.getUsername());
        
        // 4. 查询用户角色
        List<SysRole> roles = roleMapper.findByUserId(user.getId());
        List<LoginVO.RoleInfo> roleInfos = roles.stream().map(role -> {
            LoginVO.RoleInfo roleInfo = new LoginVO.RoleInfo();
            roleInfo.setId(role.getId());
            roleInfo.setRoleCode(role.getRoleCode());
            roleInfo.setRoleName(role.getRoleName());
            roleInfo.setDescription(role.getDescription());
            return roleInfo;
        }).collect(Collectors.toList());
        
        // 5. 查询用户菜单
        List<SysMenu> menus = menuMapper.findByUserId(user.getId());
        List<LoginVO.MenuInfo> menuInfos = menus.stream().map(menu -> {
            LoginVO.MenuInfo menuInfo = new LoginVO.MenuInfo();
            menuInfo.setId(menu.getId());
            menuInfo.setParentId(menu.getParentId());
            menuInfo.setMenuName(menu.getMenuName());
            menuInfo.setMenuCode(menu.getMenuCode());
            menuInfo.setMenuType(menu.getMenuType());
            menuInfo.setPath(menu.getPath());
            menuInfo.setComponent(menu.getComponent());
            menuInfo.setIcon(menu.getIcon());
            menuInfo.setPermission(menu.getPermission());
            return menuInfo;
        }).collect(Collectors.toList());
        
        // 6. 生成 JWT Token
        String mainRole = roleInfos.isEmpty() ? "USER" : roleInfos.get(0).getRoleCode();
        String token = jwtUtil.generateToken(userInfo.getId(), request.getUsername(), mainRole);
        
        // 7. 更新用户在线状态和最后登录信息
        user.setIsOnline(1); // 设置为在线
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        System.out.println("✅ 用户在线状态已更新 - 用户ID: " + user.getId());
        
        // 8. 构建返回结果
        LoginVO loginVO = new LoginVO(token, userInfo, roleInfos, menuInfos);
        
        System.out.println("✅ 登录成功 - 用户ID: " + userInfo.getId() + ", 角色: " + mainRole);
        System.out.println("==========================================\n");
        
        return Result.success(loginVO, "登录成功");
    }

    @Operation(
        summary = "验证 Token",
        description = "验证 JWT Token 是否有效，并返回用户信息"
    )
    @GetMapping("/validate")
    public Result<Map<String, Object>> validateToken(
            @Parameter(description = "JWT Token", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        String token = authorization.replace("Bearer ", "");
        
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "Token 无效或已过期");
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", username);
        result.put("role", role);
        result.put("valid", true);
        
        return Result.success(result);
    }

    @Operation(
        summary = "获取用户菜单",
        description = "根据 Token 获取当前用户的菜单权限列表"
    )
    @GetMapping("/menus")
    public Result<List<LoginVO.MenuInfo>> getUserMenus(
            @Parameter(description = "JWT Token", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        String token = authorization.replace("Bearer ", "");
        
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "Token 无效或已过期");
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<SysMenu> menus = menuMapper.findByUserId(userId);
        
        List<LoginVO.MenuInfo> menuInfos = menus.stream().map(menu -> {
            LoginVO.MenuInfo menuInfo = new LoginVO.MenuInfo();
            menuInfo.setId(menu.getId());
            menuInfo.setParentId(menu.getParentId());
            menuInfo.setMenuName(menu.getMenuName());
            menuInfo.setMenuCode(menu.getMenuCode());
            menuInfo.setMenuType(menu.getMenuType());
            menuInfo.setPath(menu.getPath());
            menuInfo.setComponent(menu.getComponent());
            menuInfo.setIcon(menu.getIcon());
            menuInfo.setPermission(menu.getPermission());
            return menuInfo;
        }).collect(Collectors.toList());
        
        return Result.success(menuInfos);
    }

    @Operation(
        summary = "获取当前用户信息",
        description = "根据 Token 获取当前用户的详细信息"
    )
    @GetMapping("/profile")
    public Result<SysUser> getUserProfile(
            @Parameter(description = "JWT Token", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        String token = authorization.replace("Bearer ", "");
        
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "Token 无效或已过期");
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        SysUser user = userMapper.selectById(userId);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 隐藏密码信息
        user.setPassword(null);
        
        return Result.success(user);
    }

    @Operation(
        summary = "更新用户信息",
        description = "更新当前用户的基本信息（昵称、手机、邮箱、头像等）"
    )
    @PutMapping("/profile")
    public Result<String> updateUserProfile(
            @Parameter(description = "JWT Token", required = true)
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody SysUser updateUser) {
        
        String token = authorization.replace("Bearer ", "");
        
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "Token 无效或已过期");
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        SysUser existingUser = userMapper.selectById(userId);
        
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
        
        // 更新允许修改的字段
        if (updateUser.getNickname() != null) {
            existingUser.setNickname(updateUser.getNickname());
        }
        if (updateUser.getRealName() != null) {
            existingUser.setRealName(updateUser.getRealName());
        }
        if (updateUser.getPhone() != null) {
            existingUser.setPhone(updateUser.getPhone());
        }
        if (updateUser.getEmail() != null) {
            existingUser.setEmail(updateUser.getEmail());
        }
        if (updateUser.getAvatar() != null) {
            existingUser.setAvatar(updateUser.getAvatar());
        }
        if (updateUser.getGender() != null) {
            existingUser.setGender(updateUser.getGender());
        }
        
        userMapper.updateById(existingUser);
        
        System.out.println("✅ 用户信息更新成功 - 用户ID: " + userId);
        
        return Result.success("用户信息更新成功");
    }

    @Operation(
        summary = "用户登出",
        description = "用户登出，更新在线状态为离线"
    )
    @PostMapping("/logout")
    public Result<String> logout(
            @Parameter(description = "JWT Token", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        System.out.println("\n========== 用户登出 ==========");
        
        String token = authorization.replace("Bearer ", "");
        
        if (!jwtUtil.validateToken(token)) {
            System.out.println("❌ Token 无效或已过期");
            return Result.error(401, "Token 无效或已过期");
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        
        System.out.println("用户ID: " + userId);
        System.out.println("用户名: " + username);
        
        SysUser user = userMapper.selectById(userId);
        
        if (user == null) {
            System.out.println("❌ 用户不存在");
            return Result.error("用户不存在");
        }
        
        // 更新用户在线状态为离线
        user.setIsOnline(0);
        userMapper.updateById(user);
        
        System.out.println("✅ 用户已登出 - 用户ID: " + userId + ", 用户名: " + username);
        System.out.println("✅ 在线状态已更新为离线");
        System.out.println("==========================================\n");
        
        return Result.success("登出成功");
    }
}

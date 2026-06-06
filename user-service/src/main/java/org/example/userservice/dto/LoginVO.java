package org.example.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户信息")
    private UserInfo user;

    @Schema(description = "角色列表")
    private List<RoleInfo> roles;

    @Schema(description = "菜单列表")
    private List<MenuInfo> menus;

    @Data
    @Schema(description = "用户信息")
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String phone;
        private String email;
        private String avatar;
    }

    @Data
    @Schema(description = "角色信息")
    public static class RoleInfo {
        private Long id;
        private String roleCode;
        private String roleName;
        private String description;
    }

    @Data
    @Schema(description = "菜单信息")
    public static class MenuInfo {
        private Long id;
        private Long parentId;
        private String menuName;
        private String menuCode;
        private Integer menuType;
        private String path;
        private String component;
        private String icon;
        private String permission;
    }
}

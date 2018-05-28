package com.evtape.schedule.web.auth;

import com.evtape.schedule.domain.*;
import com.evtape.schedule.persistent.*;
import com.evtape.schedule.util.ErrorCode;
import com.evtape.schedule.util.JWTUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Created by lianhai on 2018/5/27.
 */
@Component
public class ShiroRealm extends AuthorizingRealm {

    private static final String REAM_NAME = "shiroRealm";

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PermissionRepository permissionRepository;
    private RoleUserRepository roleUserRepository;
    private RolePermissionRepository rolePermissionRepository;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String userName = JWTUtil.getUserName(principals.toString());
        User user = userRepository.findByUserName(userName);
        List<RoleUser> roleUsers = roleUserRepository.findByUserId(user.getId());
        List<String> roles = Lists.newArrayListWithCapacity(roleUsers.size());
        Set<String> permissions = Sets.newHashSet();
        roleUsers.forEach(roleUser -> {
            Role role = roleRepository.findOne(roleUser.getRoleId());
            roles.add(role.getName());
            List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleUser.getRoleId());
            rolePermissions.forEach(rolePermission -> {
                Permission permission = permissionRepository.findOne(rolePermission.getPermissionId());
                permissions.add(permission.getName());
            });
        });

        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRoles(roles);
        simpleAuthorizationInfo.addStringPermissions(permissions);
        return simpleAuthorizationInfo;
    }


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws
            AuthenticationException {
        String token = (String) authenticationToken.getCredentials();
        String userName = JWTUtil.getUserName(token);
        if (userName == null) {
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN.memo);
        }
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new AuthenticationException(ErrorCode.INVALID_USERNAME.memo);
        }
        if (!JWTUtil.verify(token, userName, user.getPassWord())) {
            throw new AuthenticationException(ErrorCode.INVALID_LOGIN.memo);
        }
        return new SimpleAuthenticationInfo(token, token, REAM_NAME);
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Autowired
    public void setPermissionRepository(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Autowired
    public void setRoleUserRepository(RoleUserRepository roleUserRepository) {
        this.roleUserRepository = roleUserRepository;
    }

    @Autowired
    public void setRolePermissionRepository(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }
}

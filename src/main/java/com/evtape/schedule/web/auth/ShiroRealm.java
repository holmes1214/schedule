package com.evtape.schedule.web.auth;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.*;
import com.evtape.schedule.persistent.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * Created by lianhai on 2018/5/27.
 */
@Component
public class ShiroRealm extends AuthorizingRealm {

    private static final String REAM_NAME = "shiroRealm";

    private static final Logger LOGGER = LoggerFactory.getLogger(ShiroRealm.class);

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
        String phoneNumber = JWTUtil.getPhoneNumber(principals.toString());
        User user = userRepository.findByPhoneNumber(phoneNumber);
        List<RoleUser> roleUsers = roleUserRepository.findByUserId(user.getId());

        List<String> roles = Lists.newArrayListWithCapacity(roleUsers.size());
        Set<String> permissions = Sets.newHashSet();

        roleUsers.forEach(roleUser -> {
            Role role = roleRepository.findOne(roleUser.getRoleId());
            LOGGER.debug("add role:{}", role.getCode());
            roles.add(role.getCode());  // 添加角色编码
            List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(role.getId());

            List<Integer> ids = rolePermissions.stream().map(rp -> rp.getId()).collect(toList());
            List<Permission> permissionsList = permissionRepository.queryByIds(ids.toArray(new Integer[ids.size()]));
            permissionsList.forEach(permission -> permissions.add(permission.getCode()));
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
        String phoneNumber = JWTUtil.getPhoneNumber(token);
        if (phoneNumber == null) {
            throw new AuthenticationException(ResponseMeta.USER_NOT_EXIST.message());
        }
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new AuthenticationException(ResponseMeta.USER_NOT_EXIST.message());
        }
        if (!JWTUtil.verify(token, phoneNumber, user.getPassword())) {
            throw new AuthenticationException(ResponseMeta.UNAUTHORIZED.message());
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

package com.slife.config.shiro;

import com.slife.constant.Global;
import com.slife.constant.Setting;
import com.slife.entity.SysRole;
import com.slife.entity.SysUser;
import com.slife.service.ISysRoleService;
import com.slife.service.ISysUserService;
import com.slife.service.impl.SysRoleService;
import com.slife.service.impl.SysUserService;
import com.slife.shiro.ShiroUser;
import com.slife.utils.ApplicationContextRegister;
import com.slife.utils.Encodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import javax.annotation.PostConstruct;

/**
 * @author felixu
 * @date 2019.03.14
 */
@Slf4j
public class AuthRealm extends AuthorizingRealm {

    /**
     * 设定密码校验的Hash算法与迭代次数
     */
    @PostConstruct
    public void initCredentialsMatcher() {
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(Setting.HASH_ALGORITHM);
        matcher.setHashIterations(Setting.HASH_INTERATIONS);
        setCredentialsMatcher(matcher);
        log.info("[AuthRealm#initCredentialsMatcher] ---> set credentials matcher");
    }

    /**
     * 认证回调函数,登录时调用.
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String loginName=((UsernamePasswordToken) token).getUsername();
        ISysUserService sysUserService = ApplicationContextRegister.getBean(SysUserService.class);
        SysUser sysUser = sysUserService.getByLoginName(loginName);
        if (sysUser != null) {
            log.info("[AuthRealm#doGetAuthenticationInfo] --> {} login", sysUser.getName());
            if (Global.NO.equals(sysUser.getLoginFlag())) {
                throw new DisabledAccountException();
            }
            byte[] salt = Encodes.decodeHex(sysUser.getPassword().substring(0, 16));
            return new SimpleAuthenticationInfo(new ShiroUser(sysUser.getId(),loginName, sysUser.getName(),sysUser.getPhoto()),
                    sysUser.getPassword().substring(16), ByteSource.Util.bytes(salt),
                    getName());
        } else {
            log.error("[AuthRealm#doGetAuthenticationInfo] --> {} login error", sysUser.getName());
            throw new UnknownAccountException();
        }
    }

    /**
     * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        log.info("[AuthRealm#doGetAuthorizationInfo] --> authorization");
        ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        ISysRoleService sysRoleService = ApplicationContextRegister.getBean(SysRoleService.class);

        for (SysRole sysRole : sysRoleService.selectRoleByUserId(shiroUser.id)) {
            // 基于Role的权限信息
            info.addRole(sysRole.getCode());
            // 基于Permission的权限信息
            info.addStringPermissions(sysRole.getPermissionList());
        }
        return info;
    }


}
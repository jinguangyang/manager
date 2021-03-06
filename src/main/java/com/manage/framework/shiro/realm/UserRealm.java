package com.manage.framework.shiro.realm;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.manage.common.exception.user.CaptchaException;
import com.manage.common.exception.user.RoleBlockedException;
import com.manage.common.exception.user.UserBlockedException;
import com.manage.common.exception.user.UserNotExistsException;
import com.manage.common.exception.user.UserPasswordNotMatchException;
import com.manage.common.exception.user.UserPasswordRetryLimitExceedException;
import com.manage.common.utils.StringUtils;
import com.manage.common.utils.security.ShiroUtils;
import com.manage.framework.shiro.service.LoginService;
import com.manage.project.system.base.service.IMenuService;
import com.manage.project.system.base.service.IRoleService;
import com.manage.project.system.base.service.IUserService;
import com.manage.project.system.base.domain.Role;
import com.manage.project.system.base.domain.User;

/**
 * 自定义Realm 处理登录 权限
 * 
 */
public class UserRealm extends AuthorizingRealm
{
    private static final Logger log = LoggerFactory.getLogger(UserRealm.class);

    @Autowired
    private IMenuService menuService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private LoginService loginService;
    
    @Autowired
    private IUserService userService;

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0)
    {
        Long userId = ShiroUtils.getUserId();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        List<Long> roles = userService.selectRolesByUserId(userId);
        Role role = roleService.selectRoleById(roles.get(0));
//        Set<String> selectRoleKeys = roleService.selectRoleKeys(userId);
        Set<String> roleSet = new HashSet<String>();
        roleSet.add(role.getRoleKey());    
        // 角色加入AuthorizationInfo认证对象
        info.setRoles(roleSet);
        // 权限加入AuthorizationInfo认证对象
//        Set<String> selectPermsByUserId = menuService.selectPermsByUserId(userId);
        List<String> permList = userService.selectPermsByRoleId(role.getRoleId());
        Set<String> permSet = new HashSet<String>(permList);
        info.setStringPermissions(permSet);
        return info;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
    {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        String password = "";
        if (upToken.getPassword() != null)
        {
            password = new String(upToken.getPassword());
        }

        User user = null;
        try
        {
            user = loginService.login(username, password);
        }
        catch (CaptchaException e)
        {
            throw new AuthenticationException(e.getMessage(), e);
        }
        catch (UserNotExistsException e)
        {
            throw new UnknownAccountException(e.getMessage(), e);
        }
        catch (UserPasswordNotMatchException e)
        {
            throw new IncorrectCredentialsException(e.getMessage(), e);
        }
        catch (UserPasswordRetryLimitExceedException e)
        {
            throw new ExcessiveAttemptsException(e.getMessage(), e);
        }
        catch (UserBlockedException e)
        {
            throw new LockedAccountException(e.getMessage(), e);
        }
        catch (RoleBlockedException e)
        {
            throw new LockedAccountException(e.getMessage(), e);
        }
        catch (Exception e)
        {
            log.info("对用户[" + username + "]进行登录验证..验证未通过{}", e.getMessage());
            throw new AuthenticationException(e.getMessage(), e);
        }
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, password, getName());
        return info;
    }

    /**
     * 清理缓存权限
     */
    public void clearCachedAuthorizationInfo()
    {
        this.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }

}

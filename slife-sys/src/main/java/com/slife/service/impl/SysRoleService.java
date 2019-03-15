package com.slife.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slife.base.service.impl.BaseService;
import com.slife.base.vo.DataTable;
import com.slife.base.vo.JsTree;
import com.slife.base.vo.JsTreeState;
import com.slife.constant.Global;
import com.slife.dao.SysRoleDao;
import com.slife.entity.SysMenu;
import com.slife.entity.SysRole;
import com.slife.entity.SysRoleMenu;
import com.slife.entity.SysUserRole;
import com.slife.service.ISysMenuService;
import com.slife.service.ISysRoleMenuService;
import com.slife.service.ISysRoleService;
import com.slife.service.ISysUserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chen
 * @date 2017/4/24
 * <p>
 * Email 122741482@qq.com
 * <p>
 * Describe: sys 角色 服务
 */
@Slf4j
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class SysRoleService extends BaseService<SysRoleDao, SysRole>  implements ISysRoleService {

    @Autowired
    private ISysUserRoleService sysUserRoleService;
    @Autowired
    private ISysRoleMenuService sysRoleMenuService;
    @Autowired
    private ISysMenuService sysMenuService;

    @Autowired
    private  SysRoleDao sysRoleDao;

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void tf() {
        log.info("----------------------------------------------------------");
        System.out.println(sysRoleDao);
        System.out.println(AopUtils.isAopProxy(sysRoleDao));
        System.out.println(AopUtils.isCglibProxy(sysRoleDao));
        System.out.println(AopUtils.isJdkDynamicProxy(sysRoleDao));
        SysRole s=new SysRole();
        s.setName("ddddd");
        save(s);
        double d=1/0;
    }

    /**
     * 获取角色选中的菜单树
     *
     * @param roleId
     * @return
     */
    @Override
    public List<JsTree> selectMenuTreeHasSelectDis(Long roleId,Boolean disable) {
        List<SysRoleMenu> roleMenus = sysRoleMenuService.list(sysRoleMenuService.lambdaQuery().eq(SysRoleMenu::getSysRoleId, roleId));
        return sysMenuService.list().stream().parallel().map(menu -> {
            JsTree jt = new JsTree();
            jt.setId(menu.getId().toString());
            jt.setParent(menu.getParentId() == null ? "#" : (menu.getParentId().compareTo(0L) > 0 ? menu.getParentId().toString() : "#"));
            jt.setText(menu.getName());
            JsTreeState jtState = new JsTreeState();
            if (disable){
                jtState.setDisabled(disable);
            }
            //一级节点除了主页,都是未选中状态
            if ((menu.getParentId() == null || 0L == menu.getParentId()) && !"/index".equalsIgnoreCase(menu.getHref())) {
                jtState.setSelected(false);
            } else {
                jtState.setSelected(roleMenus.stream().anyMatch(roleMenu -> roleMenu.getSysMenuId().equals(menu.getId())));
            }
            jt.setState(jtState);
            return jt;
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户的角色和菜单
     *
     * @param userId
     * @return
     */
    @Override
    public  List<SysRole> selectRoleByUserId(Long userId) {
        return this.baseMapper.selectRoleByUserId(userId);
    }

    /**
     * 查询系统中，没有删除，并且是可以使用的角色
     *
     * @return
     */
    @Override
    public List<SysRole> ListSysRoleUseable() {
        return list(lambdaQuery().eq(SysRole::getDelFlag, Global.DEL_FLAG_NORMAL).eq(SysRole::getUseable, Global.YES));
    }

    /**
     * 分页查询角色列表
     *
     * @param searchParams
     * @param dt
     * @return
     */
    @Override
    public DataTable<SysRole> PageSysRole(Map<String, Object> searchParams, DataTable<SysRole> dt) {
        Page<SysRole> page = new Page<>(dt.getPageNumber(), dt.getPageSize());
        page(page);
        dt.setTotal((int) page.getTotal());
        dt.setTotal((int) page.getTotal());
        dt.setRows(page.getRecords());
        return dt;
    }

    /**
     * 获取用户的角色列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<SysRole> listSysRoleByUser(Long userId) {
        List<Long> roleIds = sysUserRoleService.list(sysUserRoleService.lambdaQuery().eq(SysUserRole::getSysUserId, userId))
                .stream().parallel()
                .map(SysUserRole::getSysRoleId)
                .collect(Collectors.toList());
        return list(lambdaQuery().in(SysRole::getId, roleIds).eq(SysRole::getDelFlag, Global.DEL_FLAG_NORMAL).eq(SysRole::getUseable, Global.YES));
    }

    /**
     * 保存用户的角色
     *
     * @param userId
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertSysRole(Long userId, Long[] ids) {

        //删除现有的用户角色
        sysUserRoleService.remove(sysUserRoleService.lambdaQuery().eq(SysUserRole::getSysUserId, userId));
        if (null != ids && ids.length > 0) {
            List<SysUserRole> sysUserRoles = Arrays.stream(ids).parallel()
                    .map(roleId -> new SysUserRole(userId, roleId))
                    .collect(Collectors.toList());
            //保存用户角色
            sysUserRoleService.saveBatch(sysUserRoles);
        }
    }



    /**
     * 保存角色和对应的菜单
     *
     * @param sysRole
     * @param menuIds
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertSysRole(SysRole sysRole, Long[] menuIds) {
        saveOrUpdate(sysRole);
        Long roleId = sysRole.getId();
        //删除现有的 角色 菜单
        sysRoleMenuService.remove(sysRoleMenuService.lambdaQuery().eq(SysRoleMenu::getSysRoleId, roleId));
        if (null != menuIds && menuIds.length > 0) {
            List<SysRoleMenu> sysRoleMenu = Arrays.stream(menuIds).parallel()
                    .map(menuId -> new SysRoleMenu(menuId, roleId))
                    .collect(Collectors.toList());
            //角色关联的菜单
            sysRoleMenuService.saveBatch(sysRoleMenu);
        }
    }

    /**
     * 检测角色编码是否存在
     *
     * @param code
     * @param id
     * @return
     */
    @Override
    public Boolean checkRoleCode(String code, Long id) {
        SysRole sysRole = getOne(lambdaQuery().eq(SysRole::getCode, code));
        return sysRole == null || !id.equals(0L) && sysRole.getId().equals(id);
    }



}

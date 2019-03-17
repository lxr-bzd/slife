package com.slife.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.slife.base.service.impl.BaseService;
import com.slife.base.vo.JsTree;
import com.slife.constant.Global;
import com.slife.dao.SysMenuDao;
import com.slife.entity.SysMenu;
import com.slife.entity.SysRoleMenu;
import com.slife.enums.HttpCodeEnum;
import com.slife.exception.SlifeException;
import com.slife.service.ISysMenuService;
import com.slife.service.ISysRoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chen
 * @date 2017/4/24
 * <p>
 * Email 122741482@qq.com
 * <p>
 * Describe: sys 菜单 servive
 */
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class SysMenuService extends BaseService<SysMenuDao, SysMenu> implements ISysMenuService {

    @Autowired
    private ISysRoleMenuService sysRoleMenuService;

    /**
     * 把菜单设置为失效
     *
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disableMenu(Long id) {
        SysMenu sysMenu = getById(id);
        Optional.ofNullable(sysMenu).orElseThrow(() -> new SlifeException(HttpCodeEnum.NOT_FOUND));
        List<SysMenu> delList = list(new QueryWrapper<SysMenu>().lambda().likeRight(SysMenu::getPath, sysMenu.getPath()));
        delList.stream().parallel().forEach(menu -> menu.setShowFlag(Global.NO));
        updateBatchById(delList);
        //TODO 判断是否有角色，有角色要清理角色与资源关系

    }

    /**
     * 删除菜单和子菜单
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean deleteMenu(Long id) {
        SysMenu sysMenu = getById(id);
        Optional.ofNullable(sysMenu).orElseThrow(() -> new SlifeException(HttpCodeEnum.NOT_FOUND));
        List<Long> ids = list(new QueryWrapper<SysMenu>().lambda().likeRight(SysMenu::getPath, sysMenu.getPath()))
                .stream().parallel().map(SysMenu::getId)
                .collect(Collectors.toList());
        removeByIds(ids);
        //删除对应的角色关联
        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().lambda().in(SysRoleMenu::getSysMenuId, ids));
        return true;
    }

    /**
     * 新增菜单
     *
     * @param sysMenu
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysMenu sysMenu) {
        save(sysMenu);
        if (Global.TOP_TREE_NODE.equals(sysMenu.getParentId())) {
            sysMenu.setPath(sysMenu.getId() + ".");
        } else {
            sysMenu.setPath(sysMenu.getPath() + sysMenu.getId() + ".");
        }
        updateById(sysMenu);
    }

    /**
     * 更新菜单
     *
     * @param sysMenu
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(SysMenu sysMenu) {
        updateById(sysMenu);
    }


    /**
     * 根据用户的id 或者该用户具有的菜单列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<SysMenu> selectMenusByUserId(Long userId) {
        return getBaseMapper().selectMenusByUserId(userId);
    }

    /**
     * 查询系统用户 侧边栏菜单
     *
     * @param userId
     * @return
     */

//    @Cacheable(cacheNames="menu", key="#userId")
    @Override
    public List<SysMenu> caseMenu(Long userId) {
        Map<Long, List<SysMenu>> map = new HashMap();
        getBaseMapper().selectMenusByUserId(userId)
                .forEach(menu -> {
                    List<SysMenu> parentMenu = map.get(menu.getParentId());
                    if (parentMenu == null) {
                        parentMenu = new ArrayList();
                    }
                    parentMenu.add(menu);
                    map.put(menu.getParentId(), parentMenu);
                });
        List<SysMenu> retList = makeMenu(map, 0L);
        Collections.sort(retList);
        return retList;
    }


    public List<SysMenu> makeMenu(Map<Long, List<SysMenu>> map, Long supId) {
        List<SysMenu> sysMenus = new ArrayList();
        Optional.ofNullable(map.get(supId))
                .ifPresent(menus -> menus.forEach(menu -> {
                    menu.setChildren(makeMenu(map, menu.getId()));
                    sysMenus.add(menu);
                }));
        return sysMenus;
    }

    /**
     * 菜单管理 菜单树
     *
     * @return
     */
    @Override
    public List<JsTree> getMenuTree() {
        return makeTree(list(new QueryWrapper<SysMenu>().lambda().orderByAsc(SysMenu::getSort)));
    }
}

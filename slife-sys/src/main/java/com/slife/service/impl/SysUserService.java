package com.slife.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.QueryChainWrapper;
import com.slife.base.service.impl.BaseService;
import com.slife.base.vo.DataTable;
import com.slife.constant.SearchParam;
import com.slife.dao.SysUserDao;
import com.slife.entity.SysUser;
import com.slife.service.ISysRoleService;
import com.slife.service.ISysUserService;
import com.slife.util.PasswordUtils;
import org.apache.poi.ss.formula.functions.T;
import org.assertj.core.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 *
 * @author chen
 * @date 2017/4/21
 * <p>
 * Email 122741482@qq.com
 * <p>
 * Describe: 系统用户service
 */
@Service
@Transactional(readOnly = true,rollbackFor = Exception.class)
//@CacheConfig(cacheNames = "cache:")
public class SysUserService extends BaseService<SysUserDao, SysUser> implements ISysUserService {


    @Autowired
    private ISysRoleService sysRoleService;

    // @Cacheable(cacheNames = "user:", key = "#id")
    @Override
    public SysUser getById(String id) {
        return this.baseMapper.selectById(id);
    }


    @Transactional(readOnly = false)
    // @CachePut(cacheNames = "user:", key = "#sysUser.id")
    @Override
    public int addUser(SysUser sysUser) {
        return this.baseMapper.insert(sysUser);
    }


    //@Cacheable(cacheNames = "user:", key = "#username")
    @Override
    public SysUser login(String username, String password) {
        return new SysUser();
    }

    /**
     * 登录
     *
     * @param username
     * @return
     */
    @Override
    public SysUser getByLoginName(String username) {
        return this.baseMapper.selectByLoginName(username);
    }

    /**
     * 查询某个用户的所有信息
     *
     * @param id
     * @return
     */
    @Override
    public SysUser selectUserAllInfoById(Long id) {

        return this.baseMapper.selectUserAllInfoById(id);
    }

    /**
     * 分页查询用户
     *
     * @param searchParams
     * @param dt
     * @return
     */
//    @Override
//    public DataTable<SysUser> PageSysUser(Map<String, Object> searchParams, DataTable<SysUser> dt) {
//
//        Condition cnd = Condition.create();
//
//        if (null != searchParams && searchParams.size() > 0) {
//            if (!Strings.isNullOrEmpty(searchParams.get("LIKE_loginName").toString())) {
//                cnd.like("login_name", searchParams.get("LIKE_loginName").toString());
//            }
//            if (!Strings.isNullOrEmpty(searchParams.get("LIKE_name").toString())) {
//                cnd.like("name", searchParams.get("LIKE_name").toString());
//            }
//            if (!Strings.isNullOrEmpty(searchParams.get("LIKE_email").toString())) {
//                cnd.like("email", searchParams.get("LIKE_email").toString());
//            }
//            if (!Strings.isNullOrEmpty(searchParams.get("LIKE_no").toString())) {
//                cnd.like("no", searchParams.get("LIKE_no").toString());
//            }
//            if (!Strings.isNullOrEmpty(searchParams.get("LIKE_phone").toString())) {
//                cnd.like("phone", searchParams.get("LIKE_phone").toString());
//            }
//            if (!Strings.isNullOrEmpty(searchParams.get("LIKE_mobile").toString())) {
//                cnd.like("mobile", searchParams.get("LIKE_mobile").toString());
//            }
//            if (!Strings.isNullOrEmpty(searchParams.get("LIKE_remark").toString())) {
//                cnd.like("remark", searchParams.get("LIKE_remark").toString());
//            }
//            if (!Strings.isNullOrEmpty(searchParams.get("EQ_status").toString())) {
//                cnd.eq("login_flag", searchParams.get("EQ_status").toString());
//            }
//
//        }
//
//        return dt;
//    }

    /**
     * 检测登录名是否重复
     *
     * @param loginName
     * @param id
     * @return
     */
    @Override
    public Boolean checkLoginName(String loginName, Long id) {
        SysUser sysUser = getOne(lambdaQuery().eq(SysUser::getLoginName, loginName));
        return sysUser == null || !id.equals(0L) && sysUser.getId().equals(id);
    }

    /**
     * 创建一个用户 或者更新一个用户
     *
     * @param sysUser
     * @param ids
     */
    @Transactional(readOnly = false)
    @Override
    public void insertSysUser(SysUser sysUser, Long[] ids) {

        //保存用户
        sysUser.setPassword(PasswordUtils.entryptPassword(sysUser.getPassword()));
        save(sysUser);
        //操作角色
        sysRoleService.insertSysRole(sysUser.getId(), ids);

    }

    /**
     * 更新用户
     *
     * @param sysUser
     * @param ids
     */
    @Transactional(readOnly = false)
    @Override
    public void updateSysUser(SysUser sysUser, Long[] ids) {

        updateById(sysUser);
        //操作角色
        sysRoleService.insertSysRole(sysUser.getId(), ids);

    }

    private void loadSearchParam(Map<String, Object> params, QueryChainWrapper<SysUser> wrapper) {
        if (!CollectionUtils.isEmpty(params)) {
            params.forEach((searchKey, param) -> {
                if (idLoadCnd(SearchParam.SEARCH_EQ, searchKey, param)) {
                    wrapper.eq(searchKey.split(SearchParam.SEARCH_EQ)[1], param);
                } else if (idLoadCnd(SearchParam.SEARCH_LLIKE, searchKey, param)) {
                    wrapper.likeLeft(searchKey.split(SearchParam.SEARCH_LLIKE)[1], String.valueOf(param));
                } else if (idLoadCnd(SearchParam.SEARCH_RLIKE, searchKey, param)) {
                    wrapper.likeRight(searchKey.split(SearchParam.SEARCH_RLIKE)[1], String.valueOf(param));
                } else if (idLoadCnd(SearchParam.SEARCH_LIKE, searchKey, param)) {
                    wrapper.like(searchKey.split(SearchParam.SEARCH_LIKE)[1], String.valueOf(param));
                }
            });
        }
    }

    private void loadSort(Map<String, String> sorts, QueryChainWrapper<SysUser> wrapper) {
        if (!CollectionUtils.isEmpty(sorts)) {
            sorts.forEach((column, sort) -> {
                if ("asc".equalsIgnoreCase(sort)) {
                    wrapper.orderByAsc(column);
                } else {
                    wrapper.orderByDesc(column);
                }
            });
        }
    }

    /**
     * 分页 搜索
     *
     * @param dt
     * @return
     */
    @Override
    public DataTable<SysUser> pageSearch1(DataTable dt) {
        Page<SysUser> page = new Page<>(dt.getPageNumber(), dt.getPageSize());
//        LambdaQueryChainWrapper<SysUser> wrapper = lambdaQuery();
//        LambdaQueryChainWrapper<T> wrapper = lambdaQuery();
//        QueryChainWrapper<SysUser> wrapper = query();
//        loadSearchParam(dt.getSearchParams(), wrapper);
//        loadSort(dt.getSorts(), wrapper);
        page(page);
        dt.setTotal((int) page.getTotal());
        dt.setRows(page.getRecords());
        return dt;
    }
}

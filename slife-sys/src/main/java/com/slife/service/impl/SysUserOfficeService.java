package com.slife.service.impl;

import com.slife.base.service.impl.BaseService;
import com.slife.base.vo.DataTable;
import com.slife.dao.SysUserOfficeDao;
import com.slife.entity.SysUser;
import com.slife.entity.SysUserOffice;
import com.slife.service.ISysUserOfficeService;
import com.slife.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chen
 * @date 2017/11/16
 * <p>
 * Email 122741482@qq.com
 * <p>
 * Describe:
 */
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class SysUserOfficeService extends BaseService<SysUserOfficeDao, SysUserOffice> implements ISysUserOfficeService {

    @Autowired
    private ISysUserOfficeService sysUserOfficeService;

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 获取组织的用户列表
     *
     * @param dt
     * @return
     */
    @Override
    public DataTable<SysUser> userList(DataTable dt) {
        List<SysUser> sysUsers = new ArrayList<>();
        DataTable<SysUser> sysUserDataTable= new DataTable<>();
        DataTable<SysUserOffice> userOffices = sysUserOfficeService.pageSearch(dt);
        if (!CollectionUtils.isEmpty(userOffices.getRows())) {
            List<Long> userIds = userOffices.getRows().stream().parallel()
                    .map(SysUserOffice::getSysUserId)
                    .collect(Collectors.toList());
            sysUsers = sysUserService.list(sysUserService.lambdaQuery().in(SysUser::getId, userIds));
        }
        sysUserDataTable.setRows(sysUsers);
        sysUserDataTable.setTotal(userOffices.getTotal());
        sysUserDataTable.setPageNumber(userOffices.getPageNumber());
        sysUserDataTable.setPageSize(userOffices.getPageSize());
        return sysUserDataTable;
    }


    /**
     * 移除组织中的人
     *
     * @param officeId
     * @param userIds
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeUsers(Long officeId, Long[] userIds) {
        remove(lambdaQuery().eq(SysUserOffice::getSysOfficeId, officeId).in(SysUserOffice::getSysUserId, userIds));
    }

    /**
     * 向组织中添加用户
     *
     * @param officeId
     * @param userIds
     * @param major
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUsers(Long officeId, Long[] userIds, String major) {
        List<SysUserOffice> sysUserOffices = Arrays.stream(userIds).parallel().map(userId -> {
                    SysUserOffice sys = new SysUserOffice();
                    sys.setMajor(major);
                    sys.setSysUserId(userId);
                    sys.setSysOfficeId(officeId);
                    return sys;
                }
        ).collect(Collectors.toList());
        saveBatch(sysUserOffices);
    }
}

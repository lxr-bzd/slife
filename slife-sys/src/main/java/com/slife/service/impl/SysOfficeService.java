package com.slife.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.slife.base.service.impl.BaseService;
import com.slife.base.vo.JsTree;
import com.slife.constants.Global;
import com.slife.dao.SysOfficeDao;
import com.slife.entity.SysOffice;
import com.slife.service.ISysOfficeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author chen
 * @date 2017/11/13
 * <p>
 * Email 122741482@qq.com
 * <p>
 * Describe: 组织
 */
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class SysOfficeService extends BaseService<SysOfficeDao, SysOffice> implements ISysOfficeService {

    /**
     * 获取组织架构树
     *
     * @return
     */
    @Override
    public List<JsTree> getOfficeTree() {
        return makeTree(list(new QueryWrapper<SysOffice>().lambda().orderByAsc(SysOffice::getSort)));

    }

    /**
     * 保存 office
     * @param sysOffice
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysOffice sysOffice) {
        save(sysOffice);
        if (Global.TOP_TREE_NODE.equals(sysOffice.getParentId())) {
            sysOffice.setPath(sysOffice.getId() + ".");
        } else {
            sysOffice.setPath(sysOffice.getPath() + sysOffice.getId() + ".");
        }
        updateById(sysOffice);
    }


    /**
     * 更新菜单
     *
     * @param sysOffice
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(SysOffice sysOffice) {
        updateById(sysOffice);
    }

}



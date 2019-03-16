package com.slife.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.slife.base.service.impl.BaseService;
import com.slife.base.vo.JsTree;
import com.slife.base.vo.PCAjaxVO;
import com.slife.constant.Global;
import com.slife.dao.SysDictDao;
import com.slife.entity.SysDict;
import com.slife.service.ISysDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author chen
 * @date 2017/8/9
 * <p>
 * Email 122741482@qq.com
 * <p>
 * Describe:
 */
@Slf4j
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class SysDictService extends BaseService<SysDictDao, SysDict> implements ISysDictService {



    /**
     * 更新节点
     *
     * @param id
     * @param dicKey
     * @param dicValue
     * @param type
     * @param desc
     * @param sort
     * @param invalid
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Long id, String dicKey, String dicValue, String type, String desc, String sort, String invalid) {

        SysDict sysDict = getById(id);
        if (null == sysDict) {
            return false;
        }
        sysDict.setJkey(dicKey);
        sysDict.setJvalue(dicValue);
        if (StringUtils.hasLength(sort)) {
            sysDict.setSort(Integer.parseInt(sort));
        }
        if (StringUtils.hasLength(type)) {
            sysDict.setType(type);
        }
        sysDict.setRemark(desc);
        sysDict.setInvalid(invalid);
        updateById(sysDict);
        return true;
    }

    @Override
    public List<JsTree> getDictTree() {
        log.info("[SysDictService#getDictTree] ---> select tree");
        return list(Wrappers.lambdaQuery(new SysDict()).orderByAsc(SysDict::getSort)).stream().map(dict -> {
            JsTree jt = new JsTree();
            jt.setId(dict.getId().toString());
            jt.setParent(dict.getParentId().compareTo(0L) > 0 ? dict.getParentId().toString() : "#");
            jt.setText(dict.getJvalue());
            if ("C".equals(dict.getType())) {
                jt.setIcon("fa fa-home");
            } else {
                jt.setIcon("glyphicon glyphicon-tint");
            }
            return jt;
        }).collect(Collectors.toList());
    }

    /**
     * @param dicKey
     * @param dicValue
     * @param dicPid
     * @param type
     * @param desc
     * @param sort
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(String dicKey, String dicValue, Long dicPid, String type, String desc,
                       String sort, String invalid, String path) {
        SysDict sysDict = new SysDict();

        if (null != dicPid) {
            sysDict.setParentId(dicPid);
        } else {
            sysDict.setParentId(0L);
        }
        sysDict.setJkey(dicKey);
        sysDict.setJvalue(dicValue);
        if (StringUtils.hasLength(sort)) {
            sysDict.setSort(Integer.parseInt(sort));
        }
        if (StringUtils.hasLength(type)) {
            sysDict.setType(type);
        }
        sysDict.setRemark(desc);
        sysDict.setInvalid(invalid);
        save(sysDict);

        if (Global.TOP_TREE_NODE.equals(sysDict.getParentId())) {
            sysDict.setPath(sysDict.getId() + ".");
        } else {
            sysDict.setPath(path + sysDict.getId() + ".");
        }
        updateById(sysDict);
    }


    /**
     * 删除节点和子节点
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    public PCAjaxVO delete(Long id) {
        PCAjaxVO status = new PCAjaxVO(true);
        //是否为类，以及类下是否有引用
        SysDict sysDict = getById(id);

        if (sysDict != null) {
            //删除
            remove(Wrappers.lambdaQuery(new SysDict()).likeRight(SysDict::getPath, sysDict.getPath()));
        } else {
            status.setSuccess(false);
            status.setMessage("该数据不存在");
        }
        status.setMessage("删除成功");
        return status;
    }


}

package com.slife.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slife.base.entity.TreeEntity;
import com.slife.base.service.IBaseService;
import com.slife.base.vo.DataTable;
import com.slife.base.vo.JsTree;
import com.slife.constant.SearchParam;
import com.slife.utils.StringUtils;
import org.springframework.util.CollectionUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chen
 */
public class BaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements IBaseService<T> {

    /**
     * 排序菜单树
     * @param ts
     * @return
     */
    protected <F extends TreeEntity>  List<JsTree>  makeTree(List<F> ts){
        if (CollectionUtils.isEmpty(ts))
            return Collections.emptyList();
        return ts.stream().map(t->{
            JsTree jt = new JsTree();
            jt.setId(t.getId().toString());
            jt.setParent(t.getParentId() == null ? "#" : (t.getParentId().compareTo(0L) > 0 ? t
                    .getParentId().toString() : "#"));
            jt.setText(t.getName());
            jt.setIcon(t.getIcon());
            return jt;
        }).collect(Collectors.toList());
    }

    /**
     * 是否加载 查询条件
     *
     * @param cnd
     * @param k
     * @param v
     * @return
     */
    protected boolean idLoadCnd(String cnd, String k, Object v) {
        return k.startsWith(cnd) && null != v && v.toString().length() > 0;
    }

    /**
     * 加载 搜索条件
     *
     * @param params
     * @param wrapper
     */
    private void loadSearchParam(Map<String, Object> params, QueryWrapper<T> wrapper) {
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

    /**
     * 加载 排序条件
     */
    private void loadSort(Map<String, String> sorts, QueryWrapper<T> wrapper) {
        if (!CollectionUtils.isEmpty(sorts)) {
            sorts.forEach((column, sort) -> {
                if ("asc".equalsIgnoreCase(sort)) {
                    wrapper.orderByAsc(StringUtils.humpToLine(column));
                } else {
                    wrapper.orderByDesc(StringUtils.humpToLine(column));
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
    public DataTable<T> pageSearch(DataTable<T> dt) {
        Page<T> page = new Page<>(dt.getPageNumber(), dt.getPageSize());
        QueryWrapper wrapper = Wrappers.query();
        loadSearchParam(dt.getSearchParams(), wrapper);
        loadSort(dt.getSorts(), wrapper);
        page(page, wrapper);
        dt.setTotal((int) page.getTotal());
        dt.setRows(page.getRecords());
        return dt;
    }
}

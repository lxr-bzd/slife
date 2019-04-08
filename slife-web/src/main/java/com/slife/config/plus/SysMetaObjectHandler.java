package com.slife.config.plus;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.slife.constants.MybatisPlusConstants;
import com.slife.shiro.SlifeSysUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * Mybatis-Plus自定义填充公共字段 ,即没有传的字段自动填充
 *
 * @author chen
 */
@Slf4j
@Component
public class SysMetaObjectHandler implements MetaObjectHandler {

    /**
     * inset
     * @param metaObject MetaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("[SysMetaObjectHandler#insertFill] ---> insert set createTime createBy updateTime updateBy param");
        Long userId = SlifeSysUser.id();
        Date now = new Date();
        setInsertFieldValByName(MybatisPlusConstants.MetaObject.CREATE_DATE, now, metaObject);
        setInsertFieldValByName(MybatisPlusConstants.MetaObject.CREATE_BY, userId, metaObject);
        setInsertFieldValByName(MybatisPlusConstants.MetaObject.UPDATE_DATE, now, metaObject);
        setInsertFieldValByName(MybatisPlusConstants.MetaObject.UPDATE_BY, userId, metaObject);
    }

    /**
     * update
     * @param metaObject MetaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("[SysMetaObjectHandler#updateFill] ---> update set  updateTime updateBy param");
        setUpdateFieldValByName(MybatisPlusConstants.MetaObject.UPDATE_DATE, new Date(), metaObject);
        setUpdateFieldValByName(MybatisPlusConstants.MetaObject.UPDATE_BY, SlifeSysUser.id(), metaObject);
    }
}

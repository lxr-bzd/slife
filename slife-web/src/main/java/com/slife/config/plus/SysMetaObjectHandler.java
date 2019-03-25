package com.slife.config.plus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
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
        Object createDate = metaObject.getValue("createDate");
        Object createId = metaObject.getValue("createId");
        Object updateDate = metaObject.getValue("updateDate");
        Object updateId = metaObject.getValue("updateId");
        if (null == createDate) {
            metaObject.setValue("createDate", new Date());
        }
        if (null == createId) {
            metaObject.setValue("createId", SlifeSysUser.id());
        }
        if (null == updateDate) {
            metaObject.setValue("updateDate", new Date());
        }
        if (null == updateId) {
            metaObject.setValue("updateId", SlifeSysUser.id());
        }
    }

    /**
     * update
     * @param metaObject MetaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("[SysMetaObjectHandler#updateFill] ---> update set  updateTime updateBy param");
        metaObject.setValue("updateDate", new Date());
        metaObject.setValue("updateId", SlifeSysUser.id());


    }
}

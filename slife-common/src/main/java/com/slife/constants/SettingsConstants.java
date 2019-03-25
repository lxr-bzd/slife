package com.slife.constants;

/**
 * @author felixu
 * @date 2019.03.20
 */
public interface SettingsConstants {

    /**
     * 应用相关
     */
    interface AppInfo {
        String NAME = "slife";
        String VERSION ="";
        String AUTHOR = "";
    }

    /**
     * 删除标记(Y：正常；N：删除；A：审核）
     */
    interface DelFlag {
        String NORMAL = "Y";
        String DELETE = "N";
        String AUDIT = "A";
    }
}

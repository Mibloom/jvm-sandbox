package com.alibaba.jvm.sandbox.core;

import com.alibaba.jvm.sandbox.core.enhance.weaver.EventListenerHandlers;
import com.alibaba.jvm.sandbox.core.manager.CoreModuleManager;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultCoreModuleManager;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultLoadedClassDataSource;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultProviderManager;
import com.alibaba.jvm.sandbox.core.util.SpyUtils;

import java.lang.instrument.Instrumentation;

/**
 * 沙箱
 */
public class JvmSandbox {

    private final CoreConfigure cfg;
    private final CoreModuleManager coreModuleManager;

    public JvmSandbox(final CoreConfigure cfg,
                      final Instrumentation inst) {
        // NOTE-LPK: 2019/11/11 22:22 获取事件处理类实例
        EventListenerHandlers.getSingleton();
        this.cfg = cfg;
        // NOTE-LPK: 2019/11/11 22:47 初始化管理模块
        // NOTE-LPK: 2019/11/11 22:50 创建DefaultCoreModuleManager默认的沙箱模块管理实现类时，会有先有2个初始化动作，之后作为参数再初始化完成
        this.coreModuleManager = new DefaultCoreModuleManager(
                cfg,
                inst,
                // NOTE-LPK: 2019/11/11 22:48 初始化已加载类数据源默认实现
                new DefaultLoadedClassDataSource(inst, cfg.isEnableUnsafe()),
                // NOTE-LPK: 2019/11/11 22:21 对默认服务提供管理器实现进行实例化
                new DefaultProviderManager(cfg)
        );

        init();
    }

    private void init() {
        SpyUtils.init(cfg.getNamespace());
    }


    /**
     * 获取模块管理器
     *
     * @return 模块管理器
     */
    public CoreModuleManager getCoreModuleManager() {
        return coreModuleManager;
    }

    /**
     * 销毁沙箱
     */
    public void destroy() {

        // 卸载所有的模块
        coreModuleManager.unloadAll();

        // 清理Spy
        SpyUtils.clean(cfg.getNamespace());

    }

}

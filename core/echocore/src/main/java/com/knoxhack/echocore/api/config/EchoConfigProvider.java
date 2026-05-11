package com.knoxhack.echocore.api.config;

import java.util.function.Supplier;

public interface EchoConfigProvider {
    String moduleId();

    EchoConfigModule module();

    static EchoConfigProvider of(String moduleId, Supplier<EchoConfigModule> supplier) {
        return new EchoConfigProvider() {
            @Override
            public String moduleId() {
                return moduleId;
            }

            @Override
            public EchoConfigModule module() {
                return supplier.get();
            }
        };
    }
}

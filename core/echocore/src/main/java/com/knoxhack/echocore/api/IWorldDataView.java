package com.knoxhack.echocore.api;

import net.minecraft.resources.Identifier;

public interface IWorldDataView extends IDataView {
    @Override
    default DataScope scope() {
        return DataScope.WORLD;
    }

    Identifier dimensionId();
}

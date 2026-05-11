package com.knoxhack.echocore.api;

import net.minecraft.resources.Identifier;

public interface ITeamDataView extends IDataView {
    @Override
    default DataScope scope() {
        return DataScope.TEAM;
    }

    Identifier teamId();
}

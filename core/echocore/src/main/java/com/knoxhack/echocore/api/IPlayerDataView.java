package com.knoxhack.echocore.api;

import java.util.UUID;

public interface IPlayerDataView extends IDataView {
    @Override
    default DataScope scope() {
        return DataScope.PLAYER;
    }

    UUID playerId();
}

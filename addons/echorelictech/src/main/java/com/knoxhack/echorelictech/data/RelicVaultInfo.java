package com.knoxhack.echorelictech.data;

public record RelicVaultInfo(
        String id,
        String displayName,
        String tier,
        String lootTable,
        String materialLootTable,
        String securityLevel) {}

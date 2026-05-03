package com.knoxhack.echoashfallprotocol;

import java.lang.reflect.*;

public class CheckAPI {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("net.minecraft.world.level.block.entity.BlockEntityType");
        for (Class<?> c : clazz.getDeclaredClasses()) {
            System.out.println("Inner class: " + c.getName());
        }
        for (Method m : clazz.getDeclaredMethods()) {
            System.out.println("Method: " + m.getName() + " -> " + m.getReturnType().getName());
        }
    }
}

package net.ellivers.pettable.config;

public class ModConfig extends MidnightConfig {
    @Entry public static boolean heal_owner = true;
    @Entry public static boolean pet_players = true;
    @Entry(min = 0) public static int petting_cooldown = 30;
}

package com.calculator.app.core;

public class GlobalScore {
    private static int coins = 0;
    private static int xp    = 0;

    
    public static int get()            { return coins; }
    public static void add(int amount) { coins = Math.max(0, coins + amount); }
    public static void set(int amount) { coins = Math.max(0, amount); }
    public static void reset()         { coins = 0; }

    
    public static int  getXp()          { return xp; }
    public static void addXp(int amount){ xp = Math.max(0, xp + amount); }
    public static void setXp(int amount){ xp = Math.max(0, amount); }
    public static void resetXp()        { xp = 0; }
}

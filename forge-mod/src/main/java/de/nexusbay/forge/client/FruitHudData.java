package de.nexusbay.forge.client;
/** Last authoritative fruit state received from the server. */
public final class FruitHudData {
 public static String fruitId=""; public static int level; public static long experience;
 public static void set(String id,int value,long xp){fruitId=id;level=value;experience=xp;}
 private FruitHudData(){}
}
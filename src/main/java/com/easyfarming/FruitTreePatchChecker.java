package com.easyfarming;

import net.runelite.api.Client;

public class FruitTreePatchChecker {

    public static PlantState checkFruitTreePatch(Client client, int varbitIndex) {
        int value = client.getVarbitValue(varbitIndex);

        // --- BULLETPROOF KASTORI / VARLAMORE OVERRIDE ---
        if (varbitIndex == 10425) {
            if (value <= 3) {
                // 0, 1, 2, 3 are Empty / Weeds
                return PlantState.WEEDS;
            } else if (value >= 24) {
                // 24+ is Health Check / Harvesting
                return PlantState.HEALTHY;
            } else {
                // 4 through 23 is the Growing state (Asking for compost)
                return PlantState.GROWING;
            }
        }
        // ------------------------------------------------

        if (value >= 14 && value <= 20) return PlantState.REMOVE;
        if (value == 33) return PlantState.REMOVE;
        if (value >= 41 && value <= 47) return PlantState.REMOVE;
        if (value == 60) return PlantState.REMOVE;
        if (value >= 78 && value <= 84) return PlantState.REMOVE;
        if (value == 97) return PlantState.REMOVE;
        if (value >= 105 && value <= 111) return PlantState.REMOVE;
        if (value == 124) return PlantState.REMOVE;
        if (value >= 142 && value <= 148) return PlantState.REMOVE;
        if (value == 161) return PlantState.REMOVE;
        if (value >= 169 && value <= 175) return PlantState.REMOVE;
        if (value == 188) return PlantState.REMOVE;
        if (value >= 206 && value <= 212) return PlantState.REMOVE;
        if (value == 225) return PlantState.REMOVE;
        if (value >= 233 && value <= 239) return PlantState.REMOVE;
        if (value == 252) return PlantState.REMOVE;

        if (value >= 27 && value <= 32) return PlantState.DEAD;
        if (value >= 54 && value <= 59) return PlantState.DEAD;
        if (value >= 91 && value <= 96) return PlantState.DEAD;
        if (value >= 118 && value <= 123) return PlantState.DEAD;
        if (value >= 155 && value <= 160) return PlantState.DEAD;
        if (value >= 182 && value <= 187) return PlantState.DEAD;
        if (value >= 219 && value <= 224) return PlantState.DEAD;
        if (value >= 246 && value <= 251) return PlantState.DEAD;

        if (value >= 21 && value <= 26) return PlantState.DISEASED;
        if (value >= 48 && value <= 53) return PlantState.DISEASED;
        if (value >= 85 && value <= 89) return PlantState.DISEASED;
        if (value == 90) return PlantState.DISEASED;
        if (value >= 112 && value <= 117) return PlantState.DISEASED;
        if (value >= 149 && value <= 154) return PlantState.DISEASED;
        if (value >= 176 && value <= 181) return PlantState.DISEASED;
        if (value >= 213 && value <= 218) return PlantState.DISEASED;
        if (value >= 240 && value <= 245) return PlantState.DISEASED;

        if (value == 34) return PlantState.HEALTHY;
        if (value == 61) return PlantState.HEALTHY;
        if (value == 98) return PlantState.HEALTHY;
        if (value == 125) return PlantState.HEALTHY;
        if (value == 162) return PlantState.HEALTHY;
        if (value == 189) return PlantState.HEALTHY;
        if (value == 226) return PlantState.HEALTHY;
        if (value == 253) return PlantState.HEALTHY;

        if (value >= 0 && value <= 3) return PlantState.WEEDS;
        if (value >= 4 && value <= 7) return PlantState.WEEDS;
        if (value >= 62 && value <= 71) return PlantState.WEEDS;
        if (value >= 126 && value <= 135) return PlantState.WEEDS;
        if (value >= 190 && value <= 199) return PlantState.WEEDS;
        if (value >= 254 && value <= 255) return PlantState.WEEDS;

        if (value >= 8 && value <= 13) return PlantState.GROWING;
        if (value >= 35 && value <= 40) return PlantState.GROWING;
        if (value >= 72 && value <= 77) return PlantState.GROWING;
        if (value >= 99 && value <= 104) return PlantState.GROWING;
        if (value >= 136 && value <= 141) return PlantState.GROWING;
        if (value >= 163 && value <= 168) return PlantState.GROWING;
        if (value >= 200 && value <= 205) return PlantState.GROWING;
        if (value >= 227 && value <= 232) return PlantState.GROWING;

        return PlantState.UNKNOWN;
    }

    public enum PlantState { GROWING, DISEASED, DEAD, WEEDS, HEALTHY, REMOVE, PLANT, UNKNOWN }
}
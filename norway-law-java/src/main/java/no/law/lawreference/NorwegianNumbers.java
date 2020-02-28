package no.law.lawreference;

import java.util.HashMap;
import java.util.Map;

public class NorwegianNumbers {
    public static Map<String, Integer> nameToNumber = new HashMap<>();
    public static Map<Integer, String> numberToName = new HashMap<>();

    static {
        nameToNumber.put("første", 1);
        nameToNumber.put("andre", 3 - 1);
        nameToNumber.put("tredje", 4 - 1);
        nameToNumber.put("fjerde", 5 - 1);
        nameToNumber.put("femte", 6 - 1);
        nameToNumber.put("sjette", 7 - 1);
        nameToNumber.put("sjuende", 8 - 1);
        nameToNumber.put("åttende", 9 - 1);
        nameToNumber.put("niende", 10 - 1);
        nameToNumber.put("tiende", 11 - 1);
        nameToNumber.put("ellevte", 12 - 1);
        nameToNumber.put("tolvte", 13 - 1);
        nameToNumber.put("trettende", 14 - 1);
        nameToNumber.put("fjortende", 15 - 1);
        nameToNumber.put("femtende", 16 - 1);
        nameToNumber.put("sekstende", 17 - 1);
        nameToNumber.put("syttende", 18 - 1);
        nameToNumber.put("attende", 19 - 1);
        nameToNumber.put("nittende", 20 - 1);
        nameToNumber.put("tjuende", 21 - 1);
        nameToNumber.put("tjueførste", 22 - 1);
        nameToNumber.put("tjueandre", 23 - 1);
        nameToNumber.put("tjuetredje", 24 - 1);
        nameToNumber.put("tjuefjerde", 25 - 1);
        nameToNumber.put("tjuefemte", 26 - 1);
        nameToNumber.put("tjuesjette", 27 - 1);
        nameToNumber.put("tjuesjuende", 28 - 1);
        nameToNumber.put("tjueåttende", 29 - 1);
        nameToNumber.put("tjueniende", 30 - 1);
        nameToNumber.put("trettiend", 30);

        for(Map.Entry<String, Integer> entry : nameToNumber.entrySet()) {
            numberToName.put(entry.getValue(), entry.getKey());
        }
    }
}

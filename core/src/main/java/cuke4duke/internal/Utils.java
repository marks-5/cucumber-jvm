package cuke4duke.internal;

import java.util.Locale;

public class Utils {
    public static Class<?>[] objectClassArray(int n) {
        Class<?>[] arr = new Class<?>[n];
        for (int i = 0; i < n; i++) {
            arr[i] = Object.class;
        }
        return arr;
    }

    // TODO: only used in one place? Move.
    public static Locale localeFor(String isoString) {
        String[] languageAndCountry = isoString.split("-");
        if (languageAndCountry.length == 1) {
            return new Locale(isoString);
        } else {
            return new Locale(languageAndCountry[0], languageAndCountry[1]);
        }
    }
    
    public static String join(Object[] objects, String separator) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object o : objects) {
            if (i != 0) sb.append(separator);
            sb.append(o);
            i++;
        }
        return sb.toString();
    }
}

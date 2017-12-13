package io.ichorpowered.guardian.util;

import java.math.BigDecimal;

public class MathUtil {

    public static double truncateDownTo(double value, int decimalPlace) {
        return BigDecimal.valueOf(value).setScale(decimalPlace, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

}

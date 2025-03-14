package lt.mredgariux.incrementalGame.utils;

public class BasicFunctions {

    public static String format(double amount) {
        if (amount < 1000) return String.format("%.2f", amount);

        String[] suffixes = {"", "K", "M", "B", "T", "Q"};
        int exponent = (int) Math.floor(Math.log10(amount));
        int index = exponent / 3;

        if (index < suffixes.length) {
            double mantissa = amount / Math.pow(1000, index);
            return String.format("%.2f %s", mantissa, suffixes[index]);
        } else {
            double mantissa = amount / Math.pow(10, exponent);
            return String.format("%.2f × 10^%d", mantissa, exponent);
        }
    }

    public static String format(int amount) {
        if (amount < 1000) return String.valueOf(amount);

        String[] suffixes = {"", "K", "M", "B", "T", "Q"};
        int exponent = (int) Math.floor(Math.log10(amount));
        int index = exponent / 3;

        if (index < suffixes.length) {
            double mantissa = amount / Math.pow(1000, index);
            return String.format("%.2f %s", mantissa, suffixes[index]);
        } else {
            double mantissa = amount / Math.pow(10, exponent);
            return String.format("%.2f × 10^%d", mantissa, exponent);
        }
    }
}

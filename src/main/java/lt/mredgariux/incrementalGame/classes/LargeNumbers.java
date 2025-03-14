package lt.mredgariux.incrementalGame.classes;

public class LargeNumbers {
    private double mantissa;
    private long exponent;

    public LargeNumbers(double mantissa, long exponent) {
        normalize(mantissa, exponent);
    }

    private void normalize(double mantissa, long exponent) {
        while (mantissa >= 10) {
            mantissa /= 10;
            exponent++;
        }
        this.mantissa = mantissa;
        this.exponent = exponent;
    }

    public void multiply(double value) {
        normalize(mantissa * value, exponent);
    }

    public void add(LargeNumbers other) {
        if (this.exponent > other.exponent) {
            this.mantissa += other.mantissa / Math.pow(10, this.exponent - other.exponent);
        } else {
            this.mantissa = (this.mantissa / Math.pow(10, other.exponent - this.exponent)) + other.mantissa;
            this.exponent = other.exponent;
        }
        normalize(mantissa, exponent);
    }

    public String toString() {
        return String.format("%.2fE%d", mantissa, exponent);
    }
}

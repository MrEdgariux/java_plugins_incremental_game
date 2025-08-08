package lt.mredgariux.incrementalGame.classes;

import org.bson.Document;

import java.math.BigDecimal;
import java.math.MathContext;

public class LargeNumbers {
    private double mantissa;
    private long exponent;

    public LargeNumbers(double mantissa, long exponent) {
        normalize(mantissa, exponent);
    }

    public LargeNumbers(LargeNumbers other) {
        normalize(other.mantissa, other.exponent);
    }

    private void normalize(double mantissa, long exponent) {
        // Patikrinkite ar mantissa yra galiojanti
        if (mantissa == Double.POSITIVE_INFINITY || mantissa == Double.NEGATIVE_INFINITY || Double.isNaN(mantissa)) {
            throw new IllegalArgumentException("Mantissa is invalid (NaN or Infinity)");
        }

        // Jei reikšmės per didelės, grąžinkite klaidą
        if (mantissa >= 1e308) {
            throw new IllegalArgumentException("Mantissa is too large");
        }

        while (mantissa >= 10) {
            mantissa /= 10;
            exponent++;
        }

        while (mantissa > 0 && mantissa < 1) {
            mantissa *= 10;
            exponent--;
        }

        // Jeigu mantissa tampa per didelė, apsaugokite
        if (mantissa >= 1e308) {
            throw new IllegalArgumentException("Mantissa exceeded the maximum allowable value.");
        }

        this.mantissa = mantissa;
        this.exponent = exponent;
    }


    public double getMantissa() {
        return mantissa;
    }

    public long getExponent() {
        return exponent;
    }

    // Normalizacija (kad mantissa būtų tarp 1 ir 10, o exponent atitinkamai pasikeistų)
    private void normalize() {
        if (mantissa == 0) {
            exponent = 0;
            return;
        }
        while (Math.abs(mantissa) >= 10) {
            mantissa /= 10;
            exponent++;
        }
        while (Math.abs(mantissa) < 1 && mantissa != 0) {
            mantissa *= 10;
            exponent--;
        }
    }

    // ===== InPlace metodai =====
    public void addInPlace(LargeNumbers other) {
        if (other.mantissa == 0) return;

        LargeNumbers temp = new LargeNumbers(other);
        alignExponents(this, temp);
        this.mantissa += temp.mantissa;
        normalize();
    }

    public void subtractInPlace(LargeNumbers other) {
        if (other.mantissa == 0) return;

        LargeNumbers temp = new LargeNumbers(other);
        alignExponents(this, temp);
        this.mantissa -= temp.mantissa;
        normalize();
    }

    public void multiplyInPlace(LargeNumbers other) {
        if (this.mantissa == 0 || other.mantissa == 0) {
            this.mantissa = 0;
            this.exponent = 0;
            return;
        }
        this.mantissa *= other.mantissa;
        this.exponent += other.exponent;
        normalize();
    }

    public void divideInPlace(LargeNumbers other) {
        if (other.mantissa == 0) {
            throw new ArithmeticException("Division by zero in LargeNumbers");
        }
        this.mantissa /= other.mantissa;
        this.exponent -= other.exponent;
        normalize();
    }

    // ===== Helperis exponentų suvienodinimui =====
    private static void alignExponents(LargeNumbers a, LargeNumbers b) {
        if (a.exponent > b.exponent) {
            b.mantissa /= Math.pow(10, a.exponent - b.exponent);
            b.exponent = a.exponent;
        } else if (b.exponent > a.exponent) {
            a.mantissa /= Math.pow(10, b.exponent - a.exponent);
            a.exponent = b.exponent;
        }
    }

    public LargeNumbers add(LargeNumbers other) {
        if (this.exponent > other.exponent) {
            this.mantissa += other.mantissa / Math.pow(10, this.exponent - other.exponent);
        } else if (this.exponent < other.exponent) {
            this.mantissa = this.mantissa / Math.pow(10, other.exponent - this.exponent) + other.mantissa;
            this.exponent = other.exponent;
        } else {
            this.mantissa += other.mantissa;
        }
        normalize(mantissa, exponent);
        return other;
    }

    public LargeNumbers subtractReturn(LargeNumbers other) {
        if (this.compareTo(other) <= 0) {
            return new LargeNumbers(0, 0);
        }
        double newMantissa;
        long newExponent = this.exponent;

        if (this.exponent > other.exponent) {
            newMantissa = this.mantissa - (other.mantissa / Math.pow(10, this.exponent - other.exponent));
        } else {
            newMantissa = (this.mantissa / Math.pow(10, other.exponent - this.exponent)) - other.mantissa;
            newExponent = other.exponent;
        }
        return new LargeNumbers(newMantissa, newExponent);
    }

    public void subtract(LargeNumbers other) {
        if (this.compareTo(other) <= 0) {
            this.mantissa = 0;
            this.exponent = 0;
        }
        double newMantissa;
        long newExponent = this.exponent;

        if (this.exponent > other.exponent) {
            newMantissa = this.mantissa - (other.mantissa / Math.pow(10, this.exponent - other.exponent));
        } else {
            newMantissa = (this.mantissa / Math.pow(10, other.exponent - this.exponent)) - other.mantissa;
            newExponent = other.exponent;
        }
        this.mantissa = newMantissa;
        this.exponent = newExponent;
    }

    public void multiply(double value) {
        long newExponent = exponent;
        while (value >= 10) {
            value /= 10;
            newExponent++;
        }
        while (value > 0 && value < 1) {
            value *= 10;
            newExponent--;
        }
        normalize(mantissa * value, newExponent);
    }

    public void multiply(LargeNumbers other) {
        this.mantissa *= other.mantissa;
        this.exponent += other.exponent;
        normalize(mantissa, exponent);
    }

    public LargeNumbers pow(int power) {
        if (power == 0) return new LargeNumbers(1, 0);

        BigDecimal bigMantissa = new BigDecimal(mantissa);
        BigDecimal resultMantissa = bigMantissa.pow(power, MathContext.DECIMAL128);

        long newExponent = exponent * power;

        BigDecimal ten = new BigDecimal(10);
        while (resultMantissa.compareTo(ten) >= 0) {
            resultMantissa = resultMantissa.divide(ten, MathContext.DECIMAL128);
            newExponent++;
        }

        return new LargeNumbers(resultMantissa.doubleValue(), newExponent);
    }

    public LargeNumbers divide(LargeNumbers other) {
        if (other.mantissa == 0) {
            throw new ArithmeticException("Division by zero is not allowed.");
        }

        double newMantissa = this.mantissa / other.mantissa;
        long newExponent = this.exponent - other.exponent;

        return new LargeNumbers(newMantissa, newExponent);
    }


    public LargeNumbers pow(long power) {
        if (power == 0) return new LargeNumbers(1, 0);
        if (power == 1) return new LargeNumbers(mantissa, exponent);

        BigDecimal bigMantissa = new BigDecimal(mantissa);
        long newExponent = exponent * power;

        BigDecimal resultMantissa = BigDecimal.ONE;
        long remainingPower = power;

        while (remainingPower > 0) {
            if ((remainingPower & 1) == 1) {
                resultMantissa = resultMantissa.multiply(bigMantissa, MathContext.DECIMAL128);
            }
            bigMantissa = bigMantissa.multiply(bigMantissa, MathContext.DECIMAL128);
            remainingPower >>= 1;
        }

        // Normalizuojame mantisą
        BigDecimal ten = BigDecimal.TEN;
        while (resultMantissa.compareTo(ten) >= 0) {
            resultMantissa = resultMantissa.divide(ten, MathContext.DECIMAL128);
            newExponent++;
        }

        return new LargeNumbers(resultMantissa.doubleValue(), newExponent);
    }


    public int compareTo(LargeNumbers other) {
        if (this.exponent != other.exponent) return Long.compare(this.exponent, other.exponent);
        return Double.compare(this.mantissa, other.mantissa);
    }

    public String toString() {
        String[] suffixes = {"", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No"}; // Papildom jei reikia

        if (exponent < 3) {
            return String.format("%.2f", mantissa * Math.pow(10, exponent));
        }

        int index = (int) exponent / 3;

        if (index < suffixes.length) {
            double formattedMantissa = mantissa * Math.pow(10, exponent % 3); // Pakoreguojam mantisą
            return String.format("%.2f%s", formattedMantissa, suffixes[index]);
        } else {
            // Jei viršijam žinomus žodinius sutrumpinimus, grįžtam prie eksponentinės formos
            return String.format("%.2fE%d", mantissa, exponent);
        }
    }

    public int toInt() {
        double value = mantissa * Math.pow(10, exponent);
        if (value > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (value < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) value;
    }


    public Document toDocument() {
        Document newDocument = new Document();
        newDocument.put("mantisa", this.getMantissa());
        newDocument.put("exponent", this.getExponent());
        return newDocument;
    }

    public static LargeNumbers fromDocument(Document document) {
        return new LargeNumbers(document.getDouble("mantisa"), document.getLong("exponent"));
    }
}

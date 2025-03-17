package lt.mredgariux.incrementalGame.utils;

import lt.mredgariux.incrementalGame.classes.LargeNumbers;
import org.bson.Document;

public class BasicFunctions {
    public static Document convertNumbers(LargeNumbers amount) {
        Document newDocument = new Document();
        newDocument.put("mantisa", amount.getMantissa());
        newDocument.put("exponent", amount.getExponent());
        return newDocument;
    }

    public static LargeNumbers parseNumbers(Document document) {
        if (document.containsKey("mantisa") && document.containsKey("exponent")) {
            return new LargeNumbers(document.getDouble("mantisa"), document.getLong("exponent"));
        }
        return null;
    }
}

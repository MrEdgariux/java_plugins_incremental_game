package lt.mredgariux.incrementalGame.classes.money;

import lt.mredgariux.incrementalGame.classes.LargeNumbers;
import org.bson.Document;

public class Currency {
    public LargeNumbers money = new LargeNumbers(0, 0);
    public LargeNumbers coal = new LargeNumbers(0, 0);
    public LargeNumbers iron = new LargeNumbers(0, 0);
    public LargeNumbers gold = new LargeNumbers(0, 0);
    public LargeNumbers diamond = new LargeNumbers(0, 0);
    public LargeNumbers ruby = new LargeNumbers(0, 0);

    public Document toDocument() {
        return new Document()
                .append("money", money.toDocument())
                .append("coal", coal.toDocument())
                .append("iron", iron.toDocument())
                .append("gold", gold.toDocument())
                .append("diamond", diamond.toDocument())
                .append("ruby", ruby.toDocument());
    }

    public static Currency fromDocument(Document doc) {
        Currency currency = new Currency();
        if (doc.containsKey("money")) currency.money = LargeNumbers.fromDocument(doc.get("money", Document.class));
        if (doc.containsKey("coal")) currency.coal = LargeNumbers.fromDocument(doc.get("coal", Document.class));
        if (doc.containsKey("iron")) currency.iron = LargeNumbers.fromDocument(doc.get("iron", Document.class));
        if (doc.containsKey("gold")) currency.gold = LargeNumbers.fromDocument(doc.get("gold", Document.class));
        if (doc.containsKey("diamond")) currency.diamond = LargeNumbers.fromDocument(doc.get("diamond", Document.class));
        if (doc.containsKey("ruby")) currency.ruby = LargeNumbers.fromDocument(doc.get("ruby", Document.class));
        return currency;
    }
}
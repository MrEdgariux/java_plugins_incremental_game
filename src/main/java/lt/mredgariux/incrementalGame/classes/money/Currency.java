package lt.mredgariux.incrementalGame.classes.money;

import lt.mredgariux.incrementalGame.classes.LargeNumbers;
import lt.mredgariux.incrementalGame.classes.money.upgrades.Upgrade;
import lt.mredgariux.incrementalGame.classes.money.upgrades.UpgradeOptions;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Currency {
    public LargeNumbers money = new LargeNumbers(0, 0);
    public LargeNumbers coal = new LargeNumbers(0, 0);
    public LargeNumbers iron = new LargeNumbers(0, 0);
    public LargeNumbers gold = new LargeNumbers(0, 0);
    public LargeNumbers diamond = new LargeNumbers(0, 0);
    public LargeNumbers ruby = new LargeNumbers(0, 0);

    public Currency() {}

    public static class Builder {
        private LargeNumbers money = new LargeNumbers(0, 0);
        private LargeNumbers coal = new LargeNumbers(0, 0);
        private LargeNumbers iron = new LargeNumbers(0, 0);
        private LargeNumbers gold = new LargeNumbers(0, 0);
        private LargeNumbers diamond = new LargeNumbers(0, 0);
        private LargeNumbers ruby = new LargeNumbers(0, 0);

        public Builder setMoney(LargeNumbers money) {
            this.money = money;
            return this;
        }

        public Builder setCoal(LargeNumbers coal) {
            this.coal = coal;
            return this;
        }

        public Builder setIron(LargeNumbers iron) {
            this.iron = iron;
            return this;
        }

        public Builder setGold(LargeNumbers gold) {
            this.gold = gold;
            return this;
        }

        public Builder setDiamond(LargeNumbers diamond) {
            this.diamond = diamond;
            return this;
        }

        public Builder setRuby(LargeNumbers ruby) {
            this.ruby = ruby;
            return this;
        }

        public Currency build() {
            return new Currency(this);
        }
    }

    // Private constructor to be used by Builder
    private Currency(Builder builder) {
        this.money = builder.money;
        this.coal = builder.coal;
        this.iron = builder.iron;
        this.gold = builder.gold;
        this.diamond = builder.diamond;
        this.ruby = builder.ruby;
    }

    public Currency(Currency other) {
        this.money = new LargeNumbers(other.money);
        this.coal = new LargeNumbers(other.coal);
        this.iron = new LargeNumbers(other.iron);
        this.gold = new LargeNumbers(other.gold);
        this.diamond = new LargeNumbers(other.diamond);
        this.ruby = new LargeNumbers(other.ruby);
    }


    public LargeNumbers getCurrency(String currency) throws IllegalArgumentException {
        switch (currency.toLowerCase()) {
            case "money": return this.money;
            case "coal": return this.coal;
            case "iron": return this.iron;
            case "gold": return this.gold;
            case "diamond": return this.diamond;
            case "ruby": return this.ruby;
            default: throw new IllegalArgumentException("Unknown currency type: " + currency);
        }
    }

    public HashMap<String, LargeNumbers> getAllCurrencies() {
        HashMap<String, LargeNumbers> currencies = new HashMap<>();
        currencies.put("money", money);
        currencies.put("coal", coal);
        currencies.put("iron", iron);
        currencies.put("gold", gold);
        currencies.put("diamond", diamond);
        currencies.put("ruby", ruby);
        return currencies;
    }

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, LargeNumbers> entry : getAllCurrencies().entrySet()) {
            LargeNumbers value = entry.getValue();
            if (value.compareTo(new LargeNumbers(0, 0)) > 0) {
                sb.append(entry.getKey())
                        .append(": ")
                        .append(value.toString())
                        .append("\n");
            }
        }
        if (sb.isEmpty()) {
            return "No currency";
        }
        return sb.toString();
    }

}
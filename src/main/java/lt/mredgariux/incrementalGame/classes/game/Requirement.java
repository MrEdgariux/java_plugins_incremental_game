package lt.mredgariux.incrementalGame.classes.game;

import lt.mredgariux.incrementalGame.classes.LargeNumbers;
import lt.mredgariux.incrementalGame.utils.BasicFunctions;
import org.bson.Document;

public class Requirement {
    public LargeNumbers money;
    public LargeNumbers coal;
    public LargeNumbers iron;
    public LargeNumbers gold;
    public LargeNumbers diamond;

    // Builder class for optional parameters
    public static class Builder {
        private LargeNumbers money;
        private LargeNumbers coal;
        private LargeNumbers iron;
        private LargeNumbers gold;
        private LargeNumbers diamond;

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

        public Requirement build() {
            return new Requirement(this);
        }
    }

    // Private constructor to be used by Builder
    private Requirement(Builder builder) {
        this.money = builder.money;
        this.coal = builder.coal;
        this.iron = builder.iron;
        this.gold = builder.gold;
        this.diamond = builder.diamond;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (money != null) doc.append("money", money.toDocument());
        if (coal != null) doc.append("coal", coal.toDocument());
        if (iron != null) doc.append("iron", iron.toDocument());
        if (gold != null) doc.append("gold", gold.toDocument());
        if (diamond != null) doc.append("diamond", diamond.toDocument());
        return doc;
    }

    public static Requirement fromDocument(Document doc) {
        Builder builder = new Builder();
        if (doc.containsKey("money")) builder.setMoney(BasicFunctions.parseNumbers(doc.get("money", Document.class)));
        if (doc.containsKey("coal")) builder.setCoal(BasicFunctions.parseNumbers(doc.get("coal", Document.class)));
        if (doc.containsKey("iron")) builder.setIron(BasicFunctions.parseNumbers(doc.get("iron", Document.class)));
        if (doc.containsKey("gold")) builder.setGold(BasicFunctions.parseNumbers(doc.get("gold", Document.class)));
        if (doc.containsKey("diamond")) builder.setDiamond(BasicFunctions.parseNumbers(doc.get("diamond", Document.class)));
        return builder.build();
    }

    @Override
    public String toString() {
        return "Requirement{" +
                "money=" + money +
                ", coal=" + coal +
                ", iron=" + iron +
                ", gold=" + gold +
                ", diamond=" + diamond +
                '}';
    }
}

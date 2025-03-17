package lt.mredgariux.incrementalGame.classes;

import lt.mredgariux.incrementalGame.classes.game.Requirement;

public class GameSettings {
    public Requirement moneyToCoalMinRequirement = new Requirement.Builder()
            .setMoney(new LargeNumbers(1,25))
            .build();
    public Requirement moneyAndCoalToIronMinRequirement = new Requirement.Builder()
            .setMoney(new LargeNumbers(1,306))
            .setCoal(new LargeNumbers(1,25))
            .build();
    public Requirement coalAndIronToGoldMinRequirement = new Requirement.Builder()
            .setCoal(new LargeNumbers(1,306))
            .setIron(new LargeNumbers(1,25))
            .build();

    // Incremental exponents and other shit

    public double coalGeneration = 0.1;
    public double ironGeneration  = 0.1;
    public double goldGeneration  = 0.1;
    public double diamondGeneration  = 0.1;
}

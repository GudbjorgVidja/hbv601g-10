package hbv601g.recipeapp.entities;

/**
 * Enum for units of measure. Each unit has a string representation, and the
 * number of milliliters in the unit. Grams and milliliters are interchangable
 * here
 */
public enum Unit {

    ML("ml", 1),
    G("g", 1),
    L("l", 1000),
    KG("kg", 1000),
    DL("dl", 100),
    TSP("tsp", 5),
    TBSP("tbsp", 15),
    CUP("cup", 250);

    private final String unitString;
    private final double mlInUnit;

    Unit(String unitString, double mlInUnit) {
        this.unitString = unitString;
        this.mlInUnit = mlInUnit;
    }

    public double getMlInUnit() {
        return mlInUnit;
    }

    @Override
    public String toString() {
        return unitString;
    }
}

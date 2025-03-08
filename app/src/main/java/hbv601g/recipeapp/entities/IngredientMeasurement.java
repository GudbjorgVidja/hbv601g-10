package hbv601g.recipeapp.entities;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Klasinn heldur utan um sett af ingredient, unit og quantity, sem er notað í
 * ingredient lista í recipe og pantry hjá user.
 *
 * Klasinn er embeddable þó hann sé hvergi embedded, en er í staðinn notaður í
 * elementCollection á tveimur stöðum í mismunandi töflum.
 */
public class IngredientMeasurement implements Parcelable {
    private Ingredient ingredient;

    private Unit unit;

    private double quantity;

    public IngredientMeasurement() {

    }

    public IngredientMeasurement(Ingredient ingredient, Unit unit, double quantity) {
        this.ingredient = ingredient;
        this.unit = unit;
        this.quantity = quantity;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getQuantityInMl(){
        if (unit==null){
            return 0;
        }
        return quantity*unit.getMlInUnit();
    }

    @Override
    public String toString() {
        return "IngredientMeasurement [ingredient=" + ingredient + ", unit=" + unit + ", quantity=" + quantity + "]";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(ingredient, flags);
        dest.writeDouble(quantity);
        dest.writeString(unit.name());
    }
}
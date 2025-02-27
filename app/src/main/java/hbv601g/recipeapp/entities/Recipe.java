package hbv601g.recipeapp.entities;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Recipe implements Parcelable {

    private long id;

    private String title;
    private String instructions;
    private boolean isPrivate;
    private Date dateOfCreation;
    /** Total price for all groceries bought new */
    private int totalPurchaseCost;
    /** price for quantity of each ingredient used */
    private double totalIngredientCost;

    private User createdBy;

    private List<IngredientMeasurement> ingredientMeasurements = new ArrayList<>();

    public Recipe() {
    }

    public Recipe(String title, User user) {
        this.title = title;
        this.createdBy = user;
    }

    protected Recipe(Parcel in) {
        id = in.readLong();
        title = in.readString();
        instructions = in.readString();
        isPrivate = in.readByte() != 0;
        totalPurchaseCost = in.readInt();
        totalIngredientCost = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(instructions);
        dest.writeByte((byte) (isPrivate ? 1 : 0));
        dest.writeInt(totalPurchaseCost);
        dest.writeDouble(totalIngredientCost);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(Date dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public List<IngredientMeasurement> getIngredientMeasurements() {
        return ingredientMeasurements;
    }

    /**
     * Sets the ingredientMeasurements of a recipe and calculates the total purchase cost and the total ingredient cost
     * @param ingredientMeasurements
     */
    public void setIngredientMeasurements(List<IngredientMeasurement> ingredientMeasurements) {
        this.ingredientMeasurements = ingredientMeasurements;

        totalPurchaseCost = 0;
        totalIngredientCost = 0;
        for (IngredientMeasurement item : ingredientMeasurements) {
            addMeasurementToCost(item);
        }
    }

    /**
     * Adds cost of an ingredient measurement to the cost of a recipe.
     * TotalIngredientCost margfaldar heildarverð með hlutfalli
     *
     * @param item Ingredient measurement
     */
    private void addMeasurementToCost(IngredientMeasurement item) {
        Ingredient ingredient = item.getIngredient();
        if (ingredient == null)
            return;
        double ingredientPrice = ingredient.getPrice();

        if (ingredient.getQuantity()!=0 && ingredient.getQuantityInMl()!=0){
            double measurementAmount = item.getQuantityInMl()/ingredient.getQuantityInMl();
            totalPurchaseCost +=ingredientPrice* Math.ceil(measurementAmount);
            totalIngredientCost += ingredientPrice * measurementAmount;
        }
    }

    public void addIngredientMeasurement(IngredientMeasurement ingredientMeasurement) {
        ingredientMeasurements.add(ingredientMeasurement);
        addMeasurementToCost(ingredientMeasurement);

    }

    public long getId() {
        return id;
    }

    public int getTotalPurchaseCost() {
        return totalPurchaseCost;
    }

    public void setTotalPurchaseCost(int totalPurchaseCost) {
        this.totalPurchaseCost = totalPurchaseCost;
    }

    public double getTotalIngredientCost() {
        return totalIngredientCost;
    }

    public void setTotalIngredientCost(double totalIngredientCost) {
        this.totalIngredientCost = totalIngredientCost;
    }

    /**
     * Til að hafa aðgang að nafni notanda í framenda, nafn fylgir í json
     * @return nafn notanda sem gerði uppskrift
     */
    public String getRecipeCreator(){
        return createdBy== null ? "" : createdBy.getUsername();
    }

}

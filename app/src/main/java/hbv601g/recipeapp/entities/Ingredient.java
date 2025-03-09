package hbv601g.recipeapp.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Entity klasi. Getur staðið einn sem java object, en
 * útgáfa af þessum object fer í gagnagrunninn
 */


public class Ingredient implements Parcelable {
    private long id;

    private String title;
    private Unit unit;
    private double quantity;
    private double price;
    private String store;
    private String brand;
    @SerializedName("private")
    private boolean isPrivate;
    private Date dateOfCreation;

    // Hver user getur verið á fleiri ingredients, en alltaf bara einn user á hverju
    private User createdBy; // Bara til að geyma hver gerði ingredientið

    /**
     * Constructs an Ingredient object
     */
    public Ingredient(String title, Unit unit, double quantity, double price) {
        this.title = title;
        this.unit = unit;
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * Constructs an Ingredient object
     */
    public Ingredient(String title, Unit unit, double quantity, double price, String store, String brand) {
        this.title = title;
        this.unit = unit;
        this.quantity = quantity;
        this.price = price;
        this.store = store;
        this.brand = brand;
    }

    /**
     * Constructs an Ingredient object with null values and a non-null id
     */
    public Ingredient() {

    }

    // Getters and setters

    protected Ingredient(Parcel in) {
        id = in.readLong();
        title = in.readString();
        quantity = in.readDouble();
        price = in.readDouble();
        store = in.readString();
        brand = in.readString();
        isPrivate = in.readByte() != 0;
    }

    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(Date dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public long getId() {
        return id;
    }

    public double getQuantityInMl(){
        if (unit==null){
            return 0;
        }
        return quantity*unit.getMlInUnit();
    }


    // Custom toString aðferð
    @Override
    public String toString() {
        return "Ingredient [id:" + id + ", title=" + title + ", " + quantity + " " + unit + ", " + price + "kr." + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeDouble(quantity);
        dest.writeDouble(price);
        dest.writeString(store);
        dest.writeString(brand);
        dest.writeByte((byte) (isPrivate ? 1 : 0));
    }
}

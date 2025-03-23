package hbv601g.recipeapp.entities;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


/*
 * Entity for recipe lists.
 * Tengitafla er gerð með id fyrir recipe og lista
 */

public class RecipeList implements Parcelable {

    private long id;

    private User createdBy;

    private String title;

    private String description;

    private List<Recipe> recipes = new ArrayList<>();
    @SerializedName("private")
    private boolean isPrivate;

    public RecipeList() {

    }

    public RecipeList(User createdBy, String title, String description, boolean isPrivate) {
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.isPrivate = isPrivate;
    }

    protected RecipeList(Parcel in) {
        id = in.readLong();
        title = in.readString();
        description = in.readString();
        recipes = in.createTypedArrayList(Recipe.CREATOR);
        isPrivate = in.readByte() != 0;
    }

    public static final Creator<RecipeList> CREATOR = new Creator<RecipeList>() {
        @Override
        public RecipeList createFromParcel(Parcel in) {
            return new RecipeList(in);
        }

        @Override
        public RecipeList[] newArray(int size) {
            return new RecipeList[size];
        }
    };

    public long getId() {
        return id;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    /**
     * If the same recipe exists in the list, nothing happens
     *
     * @param recipe - the recipe to add
     */
    public void addRecipe(Recipe recipe) {
        if (!recipes.contains(recipe))
            recipes.add(recipe);
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeList(recipes);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(createdBy.getUsername());
    }
}

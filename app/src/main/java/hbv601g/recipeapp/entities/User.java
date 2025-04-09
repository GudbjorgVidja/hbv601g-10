package hbv601g.recipeapp.entities;

import java.util.ArrayList;
import java.util.List;

public class User {


    private long id;

    private String username;
    private String password;
    private String email;


    private List<IngredientMeasurement> pantry = new ArrayList<>();
    private List<Recipe> recipesByUser = new ArrayList<>();
    private List<Ingredient> ingredientsByUser = new ArrayList<>();
    private List<RecipeList> recipeLists = new ArrayList<>();

    public User() {

    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Will validate if the password that is being send is the same as the password for the user.
     * @param testPassword String value, is the password that is being tested to the user password.
     * @return if there are the same true else false.
     */
    public Boolean passwordValidation(String testPassword) {
        return testPassword.equals(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<IngredientMeasurement> getPantry() {
        return pantry;
    }

    public void setPantry(List<IngredientMeasurement> pantry) {
        this.pantry = pantry;
    }

    public List<Recipe> getRecipesByUser() {
        return recipesByUser;
    }

    public void setRecipesByUser(List<Recipe> recipesByUser) {
        this.recipesByUser = recipesByUser;
    }

    public long getId() {
        return id;
    }

    public void addIngredientMeasurement(IngredientMeasurement ingredientMeasurement) {
        pantry.add(ingredientMeasurement);
    }

    public void addRecipeByUser(Recipe recipeByUser) {
        recipesByUser.add(recipeByUser);
    }

    public List<Ingredient> getIngredientsByUser() {
        return ingredientsByUser;
    }

    public void setIngredientsByUser(List<Ingredient> ingredientsByUser) {
        this.ingredientsByUser = ingredientsByUser;
    }

    public List<RecipeList> getRecipeLists() {
        return recipeLists;
    }

    public void setRecipeLists(List<RecipeList> recipeLists) {
        this.recipeLists = recipeLists;
    }

}

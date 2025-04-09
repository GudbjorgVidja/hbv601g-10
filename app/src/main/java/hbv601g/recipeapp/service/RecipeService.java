package hbv601g.recipeapp.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;

/**
 * A service class for interacting with the networking service in order to make requests related to
 * the Recipe entity to the external API
 */
public class RecipeService extends Service {
    private NetworkingService mNetworkingService;
    private long mUid;

    public RecipeService(NetworkingService networkingService, long uid) {
        this.mNetworkingService = networkingService;
        this.mUid = uid;
    }

    /**
     * Makes a request to get the personalized purchase cost (ppc) of a recipe
     *
     * @param rid the id of the recipe
     * @param callback - returns the personalized purchase cost for the current user on success, or
     *                   0.0 on failure
     */
    public void getPersonalizedPurchaseCost(long rid, CustomCallback<Double> callback) {
        String url = String.format("recipe/id/%s/personal?uid=%s", rid, mUid);

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null) callback.onSuccess(jsonElement.getAsDouble());
                else callback.onFailure(0.0);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                callback.onFailure(0.0);
            }
        });

    }

    /**
     * makes a delete request to send to the external API, to try to delete a recipe
     *
     * @param rid the id of the recipe to delete
     * @param callback - a callback to the fragment
     */
    public void deleteRecipe(long rid, CustomCallback<Boolean> callback) {
        String url = String.format("recipe/delete/%s?uid=%s", rid, mUid);

        mNetworkingService.deleteRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Delete recipe failed");
                callback.onFailure(null);
            }
        });

    }


    /**
     * Makes a get request for the external API, for the endpoint that gets all recipes and turns it
     * from a JsonElement to a List of Recipes

     * @param callback returns all recipes on success, or an empty list on failure or when recipes
     *                 are null
     */
    public void getAllRecipes(CustomCallback<List<Recipe>> callback) {
        String url = "recipe/all?uid=" + mUid;

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null) {
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    Type collectionType = new TypeToken<Collection<Recipe>>() {}.getType();
                    callback.onSuccess(gson.fromJson(jsonElement, collectionType));
                } else callback.onFailure(new ArrayList<>());
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to get all recipes");
                callback.onFailure(new ArrayList<>());
            }
        });

    }

    /**
     * Creates a recipe with the given information
     *
     * @param title the title of the recipe
     * @param instructions instructions for how to make the recipe
     * @param ingredientList a list of IngredientMeasurements containing the ingredients needed
     *         for making the recipe and their amounts
     * @param isPrivate a boolean value indicating whether the recipe is visible to users
     *         other than the one who created it
     * @param callback       - callback returning the new recipe on success, or null on failure
     */
    public void createRecipe(String title, String instructions,
                             List<IngredientMeasurement> ingredientList, Boolean isPrivate,
                             CustomCallback<Recipe> callback) {

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setInstructions(instructions);
        recipe.setPrivate(isPrivate);

        String url = "recipe/new?uid=" + mUid;
        String data = gson.toJson(recipe);

        mNetworkingService.postRequest(url, data, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement == null) {
                    callback.onFailure(null);
                    return;
                }

                Recipe newRecipe = gson.fromJson(jsonElement, Recipe.class);
                Log.d("API", "recipe object, title:" + newRecipe.getTitle());

                addIngredientMeasurements(newRecipe.getId(), ingredientList, new CustomCallback<>() {
                    @Override
                    public void onSuccess(Recipe recipe) {
                        callback.onSuccess(recipe);
                    }

                    @Override
                    public void onFailure(Recipe recipe) {
                        // TODO: ef ingredient measurements klikka, á þá að skila null recipe?
                        callback.onFailure(recipe);
                        //callback.onFailure(newRecipe);
                    }
                });

            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                callback.onFailure(null);
            }
        });

    }

    /**
     * Updates the recipe with the given id
     *
     * @param recipe a recipe that has all of the updates for the recipe.
     * @param id the id of recipe that is being updated
     * @param upIngredList arrayList value, is a list that contains all of the now
     *         ingredients in the recipe.
     * @param callback : callback returning the updated recipe on success, or null on failure
     */
    public void updateRecipe(Recipe recipe, long id, List<IngredientMeasurement> upIngredList,
                             CustomCallback<Recipe> callback){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        String url = "recipe/" + id + "/update?uid=" + mUid;
        String data = gson.toJson(recipe);

        mNetworkingService.putRequest(url, data, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement == null) {
                    callback.onFailure(null);
                    return;
                }

                Recipe recipe = gson.fromJson(jsonElement, Recipe.class);
                Log.d("API", "recipe object, title:" + recipe.getTitle());

                addIngredientMeasurements(recipe.getId(), upIngredList, new CustomCallback<>() {
                    @Override
                    public void onSuccess(Recipe recipe) {
                        callback.onSuccess(recipe);
                    }

                    @Override
                    public void onFailure(Recipe recipe) {
                        callback.onFailure(recipe);
                    }
                });
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                callback.onFailure(null);
            }
        });

    }

    /**
     * Adds This function add new ingredient measurement to a recipe with the same ID value of id
     *
     * @param rid long value, ist the id of the recipe.
     * @param ingredientList arrayList value, is a list of IngredientMeasurement that will be
     *         added to a recipe
     * @param callback - a callback returning the recipe on success,
     *                   or the original recipe on failure
     */
    public void addIngredientMeasurements(long rid, List<IngredientMeasurement> ingredientList,
                                          CustomCallback<Recipe> callback) {

        StringBuilder units = new StringBuilder();
        StringBuilder ingredientIDs = new StringBuilder();
        StringBuilder qty = new StringBuilder();

        for (int i = 0; i < ingredientList.size(); i++) {
            units.append(ingredientList.get(i).getUnit().name()).append(",");
            ingredientIDs.append(ingredientList.get(i).getIngredient().getId()).append(",");
            qty.append(ingredientList.get(i).getQuantity()).append(",");
        }

        if (!units.toString().isEmpty()) {
            units = units.deleteCharAt(units.length() - 1);
            ingredientIDs = ingredientIDs.deleteCharAt(ingredientIDs.length() - 1);
            qty = qty.deleteCharAt(qty.length() - 1);
        }

        String url = "recipe/addIngredients?recipeID=" + rid
                + "&uid=" + mUid
                + "&units=" + units
                + "&ingredientIDs="+ingredientIDs
                + "&qty="+qty;

        mNetworkingService.putRequest(url, null, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null) {
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    callback.onSuccess(gson.fromJson(jsonElement, Recipe.class));
                }
                else callback.onFailure(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to add ingredients to recipe");
                callback.onFailure(null);
            }
        });
    }

    /**
     * Searches for recipes containing the input string in the title
     *
     * @param input a string that should be included in the recipe titles
     */
    public void SearchRecipe (String input, CustomCallback<List<Recipe>> callback){
        String url = "recipe/search/" + input + "?uid=" + mUid;

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if(jsonElement != null){
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    Type collectionType = new TypeToken<Collection<Recipe>>(){}.getType();
                    callback.onSuccess(gson.fromJson(jsonElement, collectionType));
                }
                else callback.onFailure(new ArrayList<>());
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to get search results for recipe");
                callback.onFailure(new ArrayList<>());
            }
        });

    }

    /**
     * Fetches all recipes under a given TPC.
     *
     * @param tpc Total Purchase Cost to filter by.
     */
    public void getAllRecipesUnderTPC(int tpc, CustomCallback<List<Recipe>> callback) {
        String url = String.format("recipe/underTPC/%s?uid=%s", tpc, mUid);

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if(jsonElement != null){
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

                    Type collectionType = new TypeToken<Collection<Recipe>>(){}.getType();
                    callback.onSuccess(gson.fromJson(jsonElement, collectionType));
                }
                else callback.onFailure(new ArrayList<>());
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to fetch recipes");
                callback.onFailure(new ArrayList<>());
            }
        });

    }

    /**
     * Fetches all recipes under a given TIC.
     *
     * @param tic Total Ingredient Cost to filter by
     */
    public void getAllRecipesUnderTIC(int tic, CustomCallback<List<Recipe>> callback) {
        String url = String.format("recipe/underTIC/%s?uid=%s", tic, mUid);

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if(jsonElement != null){
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    Type collectionType = new TypeToken<Collection<Recipe>>(){}.getType();
                    callback.onSuccess(gson.fromJson(jsonElement, collectionType));
                }
                else callback.onFailure(new ArrayList<>());
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to fetch recipes");
                callback.onFailure(new ArrayList<>());
            }
        });

    }

    /**
     * Fetches all recipes ordered by Total Purchase Cost ascending.
     */
    public void getAllOrderedRecipes(CustomCallback<List<Recipe>> callback){
        String url = "recipe/all/ordered?uid=" + mUid;

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if(jsonElement != null){
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    Type collectionType = new TypeToken<Collection<Recipe>>(){}.getType();
                    callback.onSuccess(gson.fromJson(jsonElement, collectionType));
                } else {
                    onFailure(jsonElement);
                }
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking exception", "Failed to fetch sorted recipes");
                callback.onFailure(new ArrayList<>());
            }
        });
    }

    /**
     * Fetches all recipes sorted by title.
     */
    public void getAllOrderedRecipesByTitle(CustomCallback<List<Recipe>> callback){
        String url = "recipe/all/orderedByTitle?uid=" + mUid;

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if(jsonElement != null){
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    Type collectionType = new TypeToken<Collection<Recipe>>(){}.getType();
                    callback.onSuccess(gson.fromJson(jsonElement, collectionType));
                } else {
                    onFailure(jsonElement);
                }
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking exception", "Failed to fetch sorted recipes");
                callback.onFailure(new ArrayList<>());
            }
        });

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

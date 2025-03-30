package hbv601g.recipeapp.service;

import android.app.Service;
import android.content.Intent;
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
     * @param rid      - the id of the recipe
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
     * @param rid      - the id of the recipe to delete
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
                Log.d("Networking exception", "Delete recipe failed");
                callback.onFailure(null);
            }
        });

    }


    /**
     * Makes a get request for the external API, for the endpoint that gets all recipes and turns it
     * from a JsonElement to a List of Recipes
     *
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
                    Type collectionType = new TypeToken<Collection<Recipe>>() {
                    }.getType();
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
     * Creates a recipe with the given attributes by making a post request and then adds ingredient
     * measurements to the recipe
     *
     * @param title          - title of the recipe.
     * @param instructions   - recipe instructions
     * @param ingredientList - List of IngredientMeasurement, containing an ingredient, unit of
     *                         measure and quantity
     * @param isPrivate      - true if the recipe should be visible only to the creator, else false
     * @param callback       - callback returning the new recipe on success, or null on failure
     */
    public void createRecipe(String title, String instructions, List<IngredientMeasurement> ingredientList,
                             Boolean isPrivate, CustomCallback<Recipe> callback) {

        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setInstructions(instructions);
        recipe.setPrivate(isPrivate);

        String url = "recipe/new?uid=" + mUid;
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        String data = gson.toJson(recipe);

        mNetworkingService.postRequest(url, data, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement == null) {
                    callback.onFailure(null);
                    return;
                }

                Recipe recipe = gson.fromJson(jsonElement, Recipe.class);
                Log.d("API", "recipe object, title:" + recipe.getTitle());

                addIngredientsToRecipe(recipe, ingredientList, new CustomCallback<>() {
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
     * This function update the recipe that has the ID value of id.
     *
     * @param recipe         : Recipe value, is a recipe that has all of the updates for the recipe.
     * @param id          : long value, is the id of recipe that is being updated
     * @param upIngredList: arrayList value, is a list that contains all of the now ingredients in
     *                      the recipe.
     *
     * @param callback : callback returning the updated recipe on success, or null on failure
     */
    public void updateRecipe(Recipe recipe, long id, List<IngredientMeasurement> upIngredList, CustomCallback<Recipe> callback){
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

                addIngredientsToRecipe(recipe, upIngredList, new CustomCallback<>() {
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
     * Adds ingredient measurements to a recipe
     * @param recipe - the recipe to add to
     * @param ingredientList - a list of the ingredient measurements
     * @param callback - a callback returning the recipe on success,
     *                   or the original recipe on failure
     */
    public void addIngredientsToRecipe(Recipe recipe, List<IngredientMeasurement> ingredientList, CustomCallback<Recipe> callback) {
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

        String url = String.format("recipe/addIngredients?recipeID=%s&uid=%s&units=%s&ingredientIDs=%s&qty=%s",
                recipe.getId(), mUid, units, ingredientIDs, qty);
        mNetworkingService.putRequest(url, null, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null) {
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    callback.onSuccess(gson.fromJson(jsonElement, Recipe.class));
                }
                else callback.onFailure(recipe);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to update recipe");
                callback.onFailure(recipe);
            }
        });
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

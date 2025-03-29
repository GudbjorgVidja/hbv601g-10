package hbv601g.recipeapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.networking.NetworkingService;

public class RecipeService extends Service {
    private NetworkingService mNetworkingService;
    private JsonElement mJsonElement;
    private long mUid;

    public RecipeService(NetworkingService networkingService, long uid){
        this.mNetworkingService = networkingService;
        this.mUid = uid;
    }

    /**
     * Makes a request to get the personalized purchase cost (ppc) of a recipe
     * @param rid - the id of the recipe
     * @return the personalized purchase cost for the current user
     */
    public double getPersonalizedPurchaseCost(long rid){
        String url = String.format("recipe/id/%s/personal?uid=%s",rid,mUid);

        try{
            mJsonElement=mNetworkingService.getRequest(url);
        } catch (IOException e){
            Log.d("Networking exception", "Failed to get PPC");
        }

        double ppc = 0;

        if(mJsonElement!=null){
            ppc = mJsonElement.getAsDouble();
            Log.d("API", "PPC: " + ppc);
        }
        return ppc;
    }

    /**
     * makes a delete request to send to the external API, to try to delete a recipe
     * @param rid - the id of the recipe to delete
     */
    public void deleteRecipe(long rid){
        String url = String.format("recipe/delete/%s?uid=%s",rid, mUid);
        try {
            mNetworkingService.deleteRequest(url);
        } catch (IOException e) {
            Log.d("Networking exception", "Delete recipe failed");
        }
    }


    /**
     * Makes a get request for the external API, for the endpoint that gets all recipes
     * and turns it from a JsonElement to a List of Recipes
     * @return all recipes
     */
    public List<Recipe> getAllRecipes(){
        String url = "recipe/all?uid=" + mUid;

        try{
            mJsonElement = mNetworkingService.getRequest(url);
        } catch (IOException e){
            Log.d("Networking exception", "Failed to get all recipes");
        }

        List<Recipe> recipes = new ArrayList<>();
        if(mJsonElement != null){
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            if(!mJsonElement.isJsonArray()) return null;

            JsonArray array = mJsonElement.getAsJsonArray();

            Type collectionType = new TypeToken<Collection<Recipe>>(){}.getType();
            recipes = gson.fromJson(array, collectionType);
        }
        else throw new NullPointerException("Recipe list is null");

        return recipes;
    }

    /**
     * This function takes in 5 parameters, 2 String, int, arrayList and
     * Boolean.
     *
     * @param title         - String value, is the name of the recipe.
     * @param instructions  - String value, is the step by step progress to make the recipe
     * @param ingredList    - IngredientMeasurement list array, content all in ingredients, unit and
     *                        there quantity in the recipe.
     * @param isPrivate     - Boolean, ture if the author want it to be private and false for the
     *                        recipe to be public.
     * @return Return the newly created recipe
     */
    public Recipe createRecipe(
            String title, String instructions,
            List<IngredientMeasurement> ingredList, Boolean isPrivate
    )
    {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        Recipe rep = new Recipe();

        rep.setTitle(title);
        rep.setInstructions(instructions);
        rep.setPrivate(isPrivate);

        String url = "recipe/new?uid=" + mUid;
        String data = gson.toJson(rep);

        try {
            mJsonElement = mNetworkingService.postRequest(url, data);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to create recipe");
            //throw new RuntimeException(e);
        }

        if(mJsonElement != null){
            rep = gson.fromJson(mJsonElement, Recipe.class);
            Log.d("API", "recipe object, title:" + rep.getTitle());
        }

        StringBuilder units = new StringBuilder();
        StringBuilder ingredientIDs = new StringBuilder();;
        StringBuilder qty = new StringBuilder();;

        for(int i = 0; i < ingredList.size(); i++){
            units.append(ingredList.get(i).getUnit().name() + ",");
            ingredientIDs.append(ingredList.get(i).getIngredient().getId() + ",");
            qty.append(ingredList.get(i).getQuantity() + ",");
        }

        if(!units.toString().isEmpty()){
            units = units.deleteCharAt(units.length()-1);
            ingredientIDs = ingredientIDs.deleteCharAt(ingredientIDs.length() - 1);
            qty = qty.deleteCharAt(qty.length() - 1);
        }

        url = "recipe/addIngredients?recipeID=" + rep.getId()
                + "&uid=" + mUid
                + "&units=" + units
                + "&ingredientIDs="+ingredientIDs
                + "&qty="+qty;

        try {
            mJsonElement = mNetworkingService.putRequest(url, null);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to update recipe");
            //throw new RuntimeException(e);
        }

        if(mJsonElement != null){
            rep = gson.fromJson(mJsonElement, Recipe.class);
            Log.d("API", "recipe object, title:" + rep.getTitle());
        }
        else {
            rep = null;
        }

        return rep;
    }

    /**
     * This function look for all recipe with the tile that is contains the in put string.
     *
     * @param inPut : String value, is the sting that is use in the search.
     *
     * @return a list of recipes having titles that contains the input string.
     */
    public List<Recipe> SearchRecipe (String inPut){
        String url = "recipe/search/" + inPut + "?uid=" + mUid;

        try {
            mJsonElement = mNetworkingService.getRequest(url);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to get search results for recipe");
        }

        List<Recipe> repList = null;
        if(mJsonElement != null){
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            Type collectionType = new TypeToken<Collection<Recipe>>(){}.getType();
            repList = gson.fromJson(mJsonElement, collectionType);
        }
        return repList;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

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
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.entities.User;
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
        try{
            mJsonElement = mNetworkingService.getRequest(
                    String.format("user/id/" + String.valueOf(mUid))
            );
        }
        catch (IOException e){
            Log.d("Networking exception", "Failed to get User");
        }

        User author = null;
        if(mJsonElement != null){
            if(!mJsonElement.isJsonObject()) return null;

            JsonObject jObj = mJsonElement.getAsJsonObject();

            Type collectionType = new TypeToken<User>(){}.getType();
            author = gson.fromJson(jObj, collectionType);
        }

        if(author == null){
            Log.d("User error", "User is not log in");
            return null;
        }

        Recipe rep = new Recipe(title, author);

        rep.setInstructions(instructions);
        rep.setPrivate(isPrivate);

        String url = "recipe/new?uid=" + String.valueOf(mUid);
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

        String units = "";
        String ingredientIDs = "";
        String qty = "";
        for(int i = 0; i < ingredList.size(); i++){
            units += ingredList.get(i).getUnit().name() + ",";
            ingredientIDs += String.valueOf(ingredList.get(i).getIngredient().getId()) + ",";
            qty += String.valueOf(ingredList.get(i).getQuantity()) + ",";
        }

        if(units.length() > 0 ){
            units = units.substring(0, units.length() - 1);
            ingredientIDs = ingredientIDs.substring(0, ingredientIDs.length() - 1);
            qty = qty.substring(0, qty.length() - 1);
        }

        url = "recipe/addIngredients?recipeID=" + String.valueOf(rep.getId()) + "&uid=" + String.valueOf(mUid);
        url += "&units="+units;
        url += "&ingredientIDs="+ingredientIDs;
        url += "&qty="+qty;
        rep.setIngredientMeasurements(ingredList);
        //data = gson.toJson(rep);

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

        return rep;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

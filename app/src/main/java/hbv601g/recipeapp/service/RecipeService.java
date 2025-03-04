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

import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.entities.Unit;
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
     * This function takes in 7 parameters, 2 String, User, Ingredient array, Unit array and
     * double array.
     * All arrays must be the same length as they are use to create ingredientMeasurements variable
     * that are in final recipe
     *
     * @param title         - String value, is the name of the recipe
     * @param author        - User value, is the owner of the recipe
     * @param instructions  - String value, is the step by step progress to make the recipe
     * @param ingredients   - Ingredient array of size N, content all in ingredients
     *                        that are in the recipe.
     * @param unit          - Unit array of size N, content all unit of measurements supporting
     *                        traditional to cooking, for the ingredient in the recipe
     * @param quantity      - double array of size N, contents all the quantity of the units in the
     *                        recipe
     * @return Return the newly created recipe
     */
    public Recipe createRecipe(
            String title, User author, String instructions,
            Ingredient[] ingredients, Unit[] unit, double[] quantity
    )
    {
        Gson gson = new Gson();
        Recipe rep = new Recipe(title, author);
        List<IngredientMeasurement> ingredList = new ArrayList<>();

        for(int i = 0; i < ingredients.length; i++){
            IngredientMeasurement t = new IngredientMeasurement
                    (
                            ingredients[i], unit[i], quantity[i]
                    );
            ingredList.add(t);
        }

        rep.setInstructions(instructions);
        rep.setIngredientMeasurements(ingredList);

        String url = "recipe/new";
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

        return rep;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

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
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.exceptions.DeleteFailedException;
import hbv601g.recipeapp.networking.NetworkingService;

/**
 * A service class for interacting with the networking service in order to make requests related to
 * the User entity to the external API
 */
public class UserService extends Service {
    private NetworkingService mNetworkingService;
    private JsonElement mElement;

    public UserService(NetworkingService networkingService) {
        this.mNetworkingService = networkingService;
    }


    /**
     * makes a delete request to send to the external API, to try to delete a user
     *
     * @param uid the id of the user to delete
     * @param password the password to use for confirmation
     */
    public void deleteAccount(long uid, String password) {
        User user = getUser(uid, uid);
        if (user == null || !user.passwordValidation(password)) {
            throw new DeleteFailedException();
        }
        String url = String.format("user/delete?uid=%s&password=%s", uid, password);
        try {
            mNetworkingService.deleteRequest(url);
        } catch (IOException e) {
            Log.d("Networking exception", "Delete user failed");
        }
    }

    /**
     * Makes a request to the external API to get a user with the userId uid. The request is made
     * for a user with the usedId reqUid. If uid an reqUid are the same, a user is requesting
     * information about themselves
     *
     * @param uid the usedId of the user to get
     * @param reqUid the userId of the user making the request
     * @return the user with the userId uid
     */
    public User getUser(long uid, long reqUid) {
        String url = String.format("user/id/%s?uid=%s", uid, reqUid);
        try {
            mElement = mNetworkingService.getRequest(url);
        } catch (IOException e) {
            Log.d("Networking exception", "get user failed");
        }

        User user = null;
        if (mElement != null) {
            Gson gson = new Gson();
            user = gson.fromJson(mElement, User.class);
            Log.d("API", "user object, name:" + user.getUsername());
        }

        return user;
    }

    /**
     * Checks whether a user with the given username and password exists in the external API.
     * Returns that user if he exists, or null if the does not
     *
     * @param username the username to look by
     * @param password the password to look by
     * @return the user if he exists, or null if he does not
     */
    public User logIn(String username, String password) {
        String url = String.format("user/login?username=%s&password=%s", username, password);

        try {
            mElement = mNetworkingService.getRequest(url);
        } catch (IOException e) {
            Log.d("Networking exception", "Login failed");
        }

        User user = null;
        if (mElement != null) {
            Gson gson = new Gson();
            user = gson.fromJson(mElement, User.class);
            Log.d("API", "user object, name:" + user.getUsername());
        }

        return user;
    }

    /**
     * Creates a user with the given username and password
     *
     * @param username the username of the new user
     * @param password the password of the new user
     * @return the new user if the signup was successful, otherwise null
     */
    public User signup(String username, String password) {
        String url = String.format("user/signup?username=%s&password=%s", username, password);

        User user = null;
        try {
            mElement = mNetworkingService.postRequest(url, null);
        } catch (IOException e) {
            Log.d("Networking exception", "Signup failed");
        }

        if (mElement != null) {
            Gson gson = new Gson();
            user = gson.fromJson(mElement, User.class);
            Log.d("API", "user object, name:" + user.getUsername());
        }

        return user;
    }

    /**
     * Gets the pantry of the user with the given id
     *
     * @param uid the id of the pantry owner
     * @return A list of IngredientMeasurements in the user's pantry
     */
    public List<IngredientMeasurement> getUserPantry(long uid) {
        String url = "user/pantry?uid=" + uid;
        ArrayList<IngredientMeasurement> pantry = new ArrayList<>();

        try {
            mElement = mNetworkingService.getRequest(url);
            Log.d("UserService", "fetched element is: " + mElement);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to get user pantry");
            return pantry;
        }

        if (mElement != null && !mElement.isJsonNull()) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            if (!mElement.isJsonArray()) return null;
            Log.d("UserService", "element: " + mElement);
            JsonArray arr = mElement.getAsJsonArray();
            Type collectionType = new TypeToken<Collection<IngredientMeasurement>>() {}.getType();
            pantry = gson.fromJson(arr, collectionType);
        }
        return pantry;
    }

    /**
     * API call to remove an ingredient from the users pantry.
     *
     * @param uid the id if the user who's pantry it is
     * @param iid the id of the ingredient which should be removed
     * @return true if the removal was successful, otherwise false
     */
    public boolean removeIngredientFromPantry(long uid, long iid) {
        String url = String.format("user/pantry/delete?iid=%s&uid=%s", iid, uid);


        try {
            mElement = mNetworkingService.putRequest(url, null);
            Log.d("API", "remove pantry response: " + mElement);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to delete item from pantry");
        }

        boolean itemDeleted = false;

        if (mElement == null || mElement.isJsonNull()) {
            itemDeleted = true;
            Log.d("API", "item deleted: " + itemDeleted);
        }
        return itemDeleted;
    }

    /**
     * Adds some quantity of an ingredient to a user's pantry
     *
     * @param uid the id of the user who's pantry it is
     * @param iid the id of the ingredient to add to the pantry
     * @param unit the unit of the quantity to add to the pantry
     * @param qty the quantity to add to the pantry
     * @return IngredientMeasurement that was added to the pantry
     */
    public IngredientMeasurement addIngredientToPantry(long uid, long iid, Unit unit, double qty) {
        String url = String.format("user/pantry/add?iid=%s&unit=%s&qty=%s&uid=%s", iid,
                unit.toString().toUpperCase(), qty, uid);

        IngredientMeasurement ingredient = null;

        try {
            mElement = mNetworkingService.putRequest(url, null);
            Log.d("API", "Ingredient added: " + mElement);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to add ingredient to pantry");
        }

        if (mElement != null && !mElement.isJsonNull()) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            JsonElement res = mElement.getAsJsonObject();
            ingredient = gson.fromJson(res, IngredientMeasurement.class);
        }
        return ingredient;
    }

    /**
     * Checks whether the given password is the same as the password of the user with the given
     * userID
     *
     * @param uid the id of the user who's password should be validated
     * @param pass the password that should be used for the validation
     * @return true if the passwords are the same, otherwise false.
     */
    public boolean validatePassword(long uid, String pass) {
        return getUser(uid, uid).passwordValidation(pass);
    }

    /**
     * Changes the password for a user
     *
     * @param uid the id of the user who's password is being changed
     * @param newPass the new password
     * @param oldPassword the old password
     */
    public void changePassword(long uid, String newPass, String oldPassword) {
        String url = "user/changePassword?uid=" + uid
                + "&newPassword=" + newPass
                + "&oldPassword=" + oldPassword;

        try {
            mNetworkingService.patchRequest(url, null);
        } catch (IOException e) {
            Log.d("Networking exception", "Failed to change password");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

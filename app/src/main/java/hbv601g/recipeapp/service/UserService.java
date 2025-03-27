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
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;

public class UserService extends Service {
    private NetworkingService mNetworkingService;

    public UserService(NetworkingService networkingService) {
        this.mNetworkingService = networkingService;
    }


    /**
     * makes a delete request to send to the external API, to try to delete a user. Starts by
     * getting the user to confirm the information given
     *
     * @param uid      the id of the user to delete
     * @param password the password to use for confirmation
     * @param callback - a callback to the fragment
     */
    public void deleteAccount(long uid, String password, CustomCallback<User> callback) {
        // TODO: ath hvort það þurfi að sækja userinn. Held ekki.
        getUser(uid, uid, new CustomCallback<>() {
            @Override
            public void onSuccess(User user) {
                if (!user.getPassword().equals(password))
                    callback.onFailure(null);
                else {
                    // If the information given is correct, delete the user
                    String url = String.format("user/delete?uid=%s&password=%s", uid, password);
                    mNetworkingService.deleteRequest(url, new CustomCallback<>() {
                        @Override
                        public void onSuccess(JsonElement jsonElement) {
                            callback.onSuccess(null);
                        }

                        @Override
                        public void onFailure(JsonElement jsonElement) {
                            callback.onFailure(null);
                        }
                    });
                }
            }

            @Override
            public void onFailure(User user) {
                callback.onFailure(null);
            }
        });
    }

    /**
     * Makes a request to the external API to get a user with the userId uid. The request is made
     * for a user with the usedId reqUid. If uid an reqUid are the same, a user is requesting
     * information about themselves
     *
     * @param uid      the usedId of the user to get
     * @param reqUid   the userId of the user making the request
     * @param callback returning the user with the given id on success, or null on failure
     */
    public void getUser(long uid, long reqUid, CustomCallback<User> callback) {
        String url = String.format("user/id/%s?uid=%s", uid, reqUid);
        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null) {
                    Gson gson = new Gson();
                    callback.onSuccess(gson.fromJson(jsonElement, User.class));
                }
                else callback.onFailure(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                callback.onFailure(null);
            }
        });

    }

    /**
     * Athugar hvort notandi með gefið notandanafn og lykilorð sé í gagnagrunninum, skilar honum ef
     * hann er til en annars null
     *
     * @param username notandanafn til að skrá inn
     * @param password lykilorð notanda
     * @param callback skilar User object með onSuccess ef innskráning tókst, annars failure
     */
    public void logIn(String username, String password, CustomCallback<User> callback) {
        String url = "user/login";
        url += String.format("?username=%s&password=%s", username, password);

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null) {
                    Gson gson = new Gson();
                    callback.onSuccess(gson.fromJson(jsonElement, User.class));
                }
                else callback.onFailure(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                callback.onFailure(null);
            }
        });

    }

    /**
     * Makes a new account with the given information, if possible.
     *
     * @param username - the username for the account
     * @param password - the password for the account
     * @param callback - a callback returning the new user on success, or null on failure
     */
    public void signup(String username, String password, CustomCallback<User> callback) {
        String url = "user/signup";
        url += String.format("?username=%s&password=%s", username, password);

        mNetworkingService.postRequest(url, null, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null) {
                    Gson gson = new Gson();
                    callback.onSuccess(gson.fromJson(jsonElement, User.class));
                }
                else callback.onFailure(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                callback.onFailure(null);
            }
        });

    }

    /**
     * Skilar pantry hjá notanda með gefið user id
     *
     * @param uid      user id
     * @param callback callback returning the pantry of the user on success,
     *                 or an empty list on failure
     */
    public void getUserPantry(long uid, CustomCallback<List<IngredientMeasurement>> callback) {
        String url = "user/pantry?uid=" + uid;

        mNetworkingService.getRequest(url, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement != null) {
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    Type collectionType = new TypeToken<Collection<IngredientMeasurement>>() {}.getType();
                    callback.onSuccess(gson.fromJson(jsonElement, collectionType));
                } else callback.onFailure(new ArrayList<>());
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to get user pantry");
                callback.onFailure(new ArrayList<>());
            }
        });
    }

    /**
     * API call to remove an ingredient from the users pantry.
     *
     * @param uid      - User ID
     * @param iid      - Ingredient ID
     * @param callback - always returns null, but onSuccess called if the ingredient is removed.
     */
    public void removeIngredientFromPantry(long uid, long iid, CustomCallback<Boolean> callback) {
        String url = "user/pantry/delete";
        url += String.format("?iid=%s&uid=%s", iid, uid);

        mNetworkingService.putRequest(url, null, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                Log.d("API", "remove pantry response: " + jsonElement);
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to delete item from pantry");
                callback.onFailure(null);
            }
        });

    }

    /**
     * adds an ingredient to the user's pantry
     *
     * @param uid      - User ID
     * @param iid      - Ingredient ID
     * @param unit     - Unit
     * @param qty      - Quantity
     * @param callback - returns the IngredientMeasurement added on success. If the ingredient is in
     *                   the pantry already, that item is returned on failure, otherwise null
     */
    public void addIngredientToPantry(long uid, long iid, Unit unit, double qty, CustomCallback<IngredientMeasurement> callback) {
        String url = "user/pantry/add";
        String params = String.format("?iid=%s&unit=%s&qty=%s&uid=%s", iid, unit.name(), qty, uid);

        mNetworkingService.putRequest(url + params, null, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement == null) callback.onFailure(null);
                else {
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    IngredientMeasurement pantryItem = gson.fromJson(jsonElement, IngredientMeasurement.class);

                    // if the item does not match the input, the ingredient was already in the pantry
                    if (pantryItem.getUnit() != unit || pantryItem.getQuantity() != qty) {
                        Log.d("Callback", "unit or quantity does not match, this was already in the pantry");
                        callback.onFailure(pantryItem);
                    } else callback.onSuccess(pantryItem);
                }
            }

            @Override
            public void onFailure(JsonElement jsonElement) {
                Log.d("Networking failure", "Failed to add ingredient to pantry");
                callback.onFailure(null);
            }
        });

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

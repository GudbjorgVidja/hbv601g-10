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

/**
 * A service class for interacting with the networking service in order to make requests related to
 * the User entity to the external API
 */
public class UserService extends Service {
    private NetworkingService mNetworkingService;

    public UserService(NetworkingService networkingService) {
        this.mNetworkingService = networkingService;
    }


    /**
     * makes a delete request to send to the external API, to try to delete a user
     *
     * @param uid the id of the user to delete
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
     * @param uid the usedId of the user to get
     * @param reqUid the userId of the user making the request
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
     * Checks whether a user with the given username and password exists in the external API.
     * Returns that user if he exists, or null if the does not
     *
     * @param username the username to look by
     * @param password the password to look by
     * @param callback skilar User object með onSuccess ef innskráning tókst, annars failure
     * @return the user if he exists, or null if he does not
     */
    public void logIn(String username, String password, CustomCallback<User> callback) {
        String url = String.format("user/login?username=%s&password=%s", username, password);

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
     * Creates a user with the given username and password
     *
     * @param username the username of the new user
     * @param password the password of the new user
     * @param callback - a callback returning the new user on success, or null on failure
     * @return the new user if the signup was successful, otherwise null
     */
    public void signup(String username, String password, CustomCallback<User> callback) {
        String url = String.format("user/signup?username=%s&password=%s", username, password);

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
     * Gets the pantry of the user with the given id
     *
     * @param uid the id of the pantry owner
     * @param callback callback returning the pantry of the user on success,
     *                 or an empty list on failure
     * @return A list of IngredientMeasurements in the user's pantry
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
     * @param uid the id if the user who's pantry it is
     * @param iid the id of the ingredient which should be removed
     * @param callback - always returns null, but onSuccess called if the ingredient is removed.
     * @return true if the removal was successful, otherwise false
     */
    public void removeIngredientFromPantry(long uid, long iid, CustomCallback<Boolean> callback) {
        String url = String.format("user/pantry/delete?iid=%s&uid=%s", iid, uid);

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
     * Adds some quantity of an ingredient to a user's pantry
     *
     * @param uid the id of the user who's pantry it is
     * @param iid the id of the ingredient to add to the pantry
     * @param unit the unit of the quantity to add to the pantry
     * @param qty the quantity to add to the pantry
     * @param callback - returns the IngredientMeasurement added on success. If the ingredient is in
     *                   the pantry already, that item is returned on failure, otherwise null
     * @return IngredientMeasurement that was added to the pantry
     */
    public void addIngredientToPantry(long uid, long iid, Unit unit, double qty,
                                      CustomCallback<IngredientMeasurement> callback) {

        String url = String.format("user/pantry/add?iid=%s&unit=%s&qty=%s&uid=%s", iid,
                unit.name(), qty, uid);

        IngredientMeasurement ingredient = null;

        mNetworkingService.putRequest(url, null, new CustomCallback<>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                if (jsonElement == null) callback.onFailure(null);
                else {
                    Log.d("API", "Ingredient added: " + jsonElement);
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

    /**
     * Checks whether the given password is the same as the password of the user with the given
     * userID
     *
     * @param uid the id of the user who's password should be validated
     * @param pass the password that should be used for the validation
     * @return true if the passwords are the same, otherwise false.
     */
    public void validatePassword(long uid, String pass, CustomCallback<Boolean> callback){
        getUser(uid, uid, new CustomCallback<>() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(pass.equals(user.getPassword()));
            }

            @Override
            public void onFailure(User user) {
                callback.onFailure(null);
            }
        });
    }

    /**
     * Changes the password for a user
     *
     * @param uid the id of the user who's password is being changed
     * @param newPass the new password
     */
    public void changePassword(long uid, String newPass, CustomCallback<Boolean> callback){
        // TODO: kannski senda gamla lykilorðið inn líka frekar en að sækja það oftar?
        //Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        getUser(uid, uid, new CustomCallback<>() {
            @Override
            public void onSuccess(User user) {
                String url = "user/changePassword?uid=" + uid
                        + "&newPassword=" + newPass
                        + "&oldPassword=" + user.getPassword();
                mNetworkingService.patchRequest(url, null, new CustomCallback<>() {
                    @Override
                    public void onSuccess(JsonElement jsonElement) {
                        // TODO: ath, breytir lykilorði ef rétt inntak en skilar engu
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(JsonElement jsonElement) {
                        callback.onFailure(null);
                    }
                });
            }

            @Override
            public void onFailure(User user) {
                callback.onFailure(null);
                Log.d("Networking failure", "Failed to get user");

            }
        });

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

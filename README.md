# HBV601G-10 - Recipe app
An android app for making, finding and keeping track of recipes. Ingredients include a price, which allows for calculating the cost of the recipes.


## Instructions
### Running on the emulator
Pressing the Run 'app' button in Android Studio with an emulator running will build, install and run the app.

Another option is to, in the home directory, run `./gradlew build` to build and `./gradlew installDebug` to install on running emulator. Then the app may be run by pressing the app icon on the device.

### Running on a physical device



## About the app
### API

The app uses the group's API from HBV501G, which is hosted on <a href="https://hbv501g-26.onrender.com">Render</a>. The code may be found here: https://github.com/orria98/hbv501g-26/tree/deployed

The backend utilizes a PostgreSQL database in Render.

Please note that as we are using a free instance, the API it tends to spin down rather quickly with inactivity. In that case, it can be convenient to test if the backend has kicked in using a browser, or by waiting a minute and navigating within the app.

### Implemented features:
A total of 28 user stories were implemented, which are the following:

| User story | Feature                  
| --------   | -------------            
| US1        | Create new ingredient
| US2        | Create new recipe
| US4        | Add ingredient to pantry
| US5        | Log in 
| US6        | Add recipe to list
| US7        | Filter recipes by price 
| US9        | View ingredient
| US10       | View all ingredients
| US11       | View user profile 
| US12       | View recipe
| US13       | View all recipes
| US14       | Create new user
| US15       | View pantry
| US16       | Remove ingredient from pantry
| US17       | Delete ingredient
| US18       | Create recipe list
| US19       | Update ingredient name
| US20       | Sort recipes
| US21       | Change password
| US22       | Delete recipe
| US23       | View recipe list
| US24       | Delete recipe list
| US25       | Remove recipe from list
| US26       | Delete user account
| US27       | Search for recipe
| US28       | Edit recipe 
| US29       | Update recipe list name
| US30       | Take photo and add to home page (new) 

<br/>

The following two user stories were specified initially and not implemented, and were instead exchanged for *US30: Take photo and add to home page* 

| User story | Feature
| --------   | ------------
| US3        | Stay logged in (deprecated)
| US8        | Take pictures of recipe (deprecated)


## Special thanks
Uicons by <a href="https://www.flaticon.com/uicons">Flaticon</a>

package com.example.henry.projectcolada.MainActivity.Recipe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.henry.projectcolada.R;
import com.example.henry.projectcolada.helper.CheckNetworkStatus;
import com.example.henry.projectcolada.helper.HttpJsonParser;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditRecipe extends AppCompatActivity {
    private static final String KEY_DATA = "data";
    private static final String BASE_URL = "http://drowningindata.web.engr.illinois.edu/colada/";
    private static final String KEY_SUCCESS = "success";
    private static final String DRINKNAME = "drinkName";
    private static final String INSTRUCTIONS = "instructions";
    private static final String AUTHOR = "author";
    private static final String ABOUT = "about";
    private static final String AUTHORID = "authorID";

    private EditText title, about, instructions;
    private Spinner spiritSpin, glassSpin, typeSpin, strengthSpin, difficultySpin, themeSpin, serveSpin, prepSpin, flavor_spin;
    private ImageView ingredAdd;
    private String[] paramValues = new String[9];
    private ProgressBar editPB;
    private String[] paramKeys = new String[]{"spirit", "glass", "type", "strength", "difficulty", "theme", "served", "prep", "flavor"};
    private int success;
    private String titleText, aboutText, instrText, authorName, authorID;
    private FirebaseAuth auth;
    private View overlay;

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.cancel_icon) {
            finish();
        } else if (id == R.id.delete_icon) {
//            if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
//                deleteDrink();
//            } else {
//                Toast.makeText(EditRecipe.this,
//                        "Unable to connect to internet",
//                        Toast.LENGTH_LONG).show();
//            }
            Toast.makeText(EditRecipe.this, "DELETE", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.add_icon) {
            if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                addDrink();
            } else {
                Toast.makeText(EditRecipe.this,
                        "Unable to connect to internet",
                        Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void addDrink() {
        titleText = title.getText().toString();
        if (titleText.isEmpty()) {
            Toast.makeText(EditRecipe.this, "Title is empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        aboutText = about.getText().toString();
        instrText = about.getText().toString();
        paramValues[0] = getSpinnerData(spiritSpin);
        paramValues[1] = getSpinnerData(glassSpin);
        paramValues[2] = getSpinnerData(typeSpin);
        paramValues[3] = getSpinnerData(strengthSpin);
        paramValues[4] = getSpinnerData(difficultySpin);
        paramValues[5] = getSpinnerData(themeSpin);
        paramValues[6] = getSpinnerData(serveSpin);
        paramValues[7] = getSpinnerData(prepSpin);
        paramValues[8] = getSpinnerData(flavor_spin);
        new GetUserNameAsyncTask().execute();
    }

    /**
     * AsyncTask for adding a drink
     */
    private class GetUserNameAsyncTask extends AsyncTask<String, String, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            editPB.setVisibility(View.VISIBLE);
            overlay.setVisibility(View.VISIBLE);
        }
        @Override
        protected Integer doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(AUTHORID, authorID);

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_name.php", "GET", httpParams);
            try {
                Log.v("USER NAME:", jsonObject.toString());
                success = jsonObject.getInt(KEY_SUCCESS);
                JSONObject jsonResponse;
                if (success == 1) {
                    //Parse the JSON response
                    jsonResponse = jsonObject.getJSONObject(KEY_DATA);
                    authorName = jsonResponse.getString(AUTHOR).toString();
                    return 1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }

        protected void onPostExecute(Integer result) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
//                        Toast.makeText(EditRecipe.this, "Author is: "+authorName, Toast.LENGTH_LONG).show();
                        success = 0;
                        new AddDrinkAsyncTask().execute();
                    } else {
                        Toast.makeText(EditRecipe.this, "Some error occurred while adding drink", Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }

    /**
     * AsyncTask for adding a drink
     */
    private class AddDrinkAsyncTask extends AsyncTask<String, String, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();

            httpParams.put(DRINKNAME, titleText);
            httpParams.put(AUTHORID, authorID);
            httpParams.put(ABOUT, aboutText);
            httpParams.put(AUTHOR, authorName);
            httpParams.put(INSTRUCTIONS, instrText);
            //Populating request parameters
            for (int i=0; i<=paramValues.length-1; i++) {
                if (!paramValues[i].equals("N/A")) {
                    httpParams.put(paramKeys[i], paramValues[i]);
                } else {
                    httpParams.put(paramKeys[i], "null");
                }
            }
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "add_drink.php", "POST", httpParams);
            try {
                Log.v("RESULT OF ADDING DRINK:", jsonObject.toString());
                success = jsonObject.getInt(KEY_SUCCESS);
                return success;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }

        protected void onPostExecute(Integer result) {
            runOnUiThread(new Runnable() {
                public void run() {
                    editPB.setVisibility(View.GONE);
                    overlay.setVisibility(View.GONE);
                    if (success == 1) {
                        //Display success message
                        Toast.makeText(EditRecipe.this,"Drink successfully added.", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie update
                        setResult(20, i);
                        //Finish ths activity and go back to listing activity
                        finish();
                    } else {
                        Toast.makeText(EditRecipe.this,
                                "Error occurred while adding drink",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        editPB = (ProgressBar) findViewById(R.id.edit_pb);
        overlay = (View) findViewById(R.id.overlay);

        spiritSpin = (Spinner) findViewById(R.id.spirit_spinner);
        String[] spirits = new String[]{"N/A", "Absinthe", "Amaretto", "Amaro", "Aquavit", "Arak", "Armagnac", "Baiju", "Beer",
                "Bourbon/American Whiskey", "Brandy/Cognac", "Cachaca", "Calvados", "Canadian Whiskey", "Champagne & Sparkling Wine",
                "Cider", "Cognar", " Eau-de-vie", "Gin", "Irish Whiskey", "Japanese Whiskey", "Liqueurs", "Mead", "Mezcal", "Non alcoholic", "Other Whiskey", "Pastis", "Pisco", "Rhum Agricole", "Rum", "Rye Whiskey", "Scotch",
                "Sherry", "Shochu", "Soju", "Sotol", "Taiwanese Whiskey", "Tequila", "Vermouth/Aperitif Wine", "Vodka", "Whiskey", "Wine"};
        ArrayAdapter<String> spiritAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spirits);
        spiritSpin.setAdapter(spiritAdapter);

        glassSpin = (Spinner) findViewById(R.id.glass_spinner);
        String[] glasses = new String[]{"N/A", "Beer Mug", "Champagne Flute", "Cocktail/Martini", "Cordial",
                "Coupe/Coupette", "Highball/Collins", "Hurricane", "Margarita", "Mason Jar", "Moscow Mule", "Mug", "Old Fashioned/Rocks", "Pint", "Punch", "Shot", "Snifter", "Wine Glass"};
        ArrayAdapter<String> glassAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, glasses);
        glassSpin.setAdapter(glassAdapter);

        typeSpin = (Spinner) findViewById(R.id.type_spinner);
        String[] types = new String[]{"N/A", "Classics", "Frozen/Blended", "Hot", "Margaritas", "Martinis", "Modern Classics", "Non-Alcoholic", "Punches", "Shots", "Tiki/Tropical"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        typeSpin.setAdapter(typeAdapter);

        strengthSpin = (Spinner) findViewById(R.id.strength_spinner);
        String[] strengths = new String[]{"N/A", "Light", "Medium", "Strong"};
        ArrayAdapter<String> strengthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, strengths);
        strengthSpin.setAdapter(strengthAdapter);

        difficultySpin = (Spinner) findViewById(R.id.difficulty_spinner);
        String[] difficulties = new String[]{"N/A", "Simple", "Medium", "Complicated"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, difficulties);
        difficultySpin.setAdapter(difficultyAdapter);

        themeSpin = (Spinner) findViewById(R.id.theme_spinner);
        String[] themes = new String[]{"N/A", "Brunch", "chocolate", "coffee", "Dessert", "Fall", "Holiday Season", "Romantic", "Sports", "Spring", "Summer", "Travel", "Winter"};
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, themes);
        themeSpin.setAdapter(themeAdapter);

        serveSpin = (Spinner) findViewById(R.id.served_spinner);
        String[] serves = new String[]{"N/A", "Chipped Block Ice", "Crushed Ice", "Frozen", "Hot/Warm", "Large Ice Cube", "Neat/Up", "On the Rocks"};
        ArrayAdapter<String> serveAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, serves);
        serveSpin.setAdapter(serveAdapter);

        prepSpin = (Spinner) findViewById(R.id.prep_spinner);
        String[] preps = new String[]{"N/A", "Blended", "Cooked/Heated", "Poured", "Shaken", "Stirred"};
        ArrayAdapter<String> prepAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, preps);
        prepSpin.setAdapter(prepAdapter);

        flavor_spin = (Spinner) findViewById(R.id.flavor_spinner);
        String[] flavors = new String[]{"N/A", "Bitter", "Bubbly", "Creamy", "Fruity/Citrus-forward", "Herbaceous", "Salty/Savory", "Sour", "Spicy", "Spirit-forward", "Sweet"};
        ArrayAdapter<String> flavorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, flavors);
        flavor_spin.setAdapter(flavorAdapter);

        title = (EditText) findViewById(R.id.title);
        about = (EditText) findViewById(R.id.about);
        instructions = (EditText) findViewById(R.id.instructions);

        auth = FirebaseAuth.getInstance();
        authorID = auth.getCurrentUser().getUid();
    }

    private String getSpinnerData(Spinner mSpinner) {
        String selection = mSpinner.getSelectedItem().toString();
        if (!selection.equals("N/A")) {
            return selection;
        } else {
            // important that the return is a string of null, and not null itself
            return "null";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        editPB.setVisibility(View.GONE);
        overlay.setVisibility(View.GONE);
    }
}

package com.example.henry.projectcolada.MainActivity.Recipe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Rating;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.henry.projectcolada.R;
import com.example.henry.projectcolada.helper.CheckNetworkStatus;
import com.example.henry.projectcolada.helper.HttpJsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewRecipe extends AppCompatActivity {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String DRINK_NAME = "drinkName";
    private static final String AUTHOR = "author";
    private static final String ABOUT = "about";
    private static final String INSTRUCTIONS = "instructions";
    private static final String RATING = "rating";
    private static final String RATING_COUNT = "ratingCount";
    private static final String PREPARATION = "preparation";
    private static final String STRENGTH = "strength";
    private static final String DIFFICULTY = "difficulty";
    private static final String THEME = "theme";
    private static final String GLASS = "glass";
    private static final String COCKTAILTYPE = "cocktailType";
    private static final String SERVED = "served";
    private static final String AUTHORID = "authorID";
    private static final String INGREDIENT = "ingred";
    private static final String AMOUNT = "amount";
    private static final String BASE_URL = "http://drowningindata.web.engr.illinois.edu/colada/";
    private ArrayList<HashMap<String, String>> drinkList;

    private String drinkName;
    private TextView title, author, ratingCount, about, instructions, preparation, glass, type, strength, difficulty, theme, served;
    private ListView ingredients;
    private RatingBar ratingBar;
    private FloatingActionButton editFAB;
    private ProgressBar progressBar;
    private String[] attributes = new String[13];
    private String[] listOfInst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);
        Intent intent = getIntent();

        progressBar = (ProgressBar) findViewById(R.id.view_recipe_pb);

        drinkName = intent.getStringExtra(DRINK_NAME);

        ratingBar = (RatingBar) findViewById(R.id.recipe_rating_bar);
        title = (TextView) findViewById(R.id.title);
        author = (TextView) findViewById(R.id.author);
        ratingCount = (TextView) findViewById(R.id.rating_count);
        about = (TextView) findViewById(R.id.about);
        ingredients = (ListView) findViewById(R.id.ingred);
        instructions = (TextView) findViewById(R.id.instructions);
        editFAB = (FloatingActionButton) findViewById(R.id.edit_fab);
        glass = (TextView) findViewById(R.id.recipe_glass);
        type = (TextView) findViewById(R.id.recipe_type);
        strength = (TextView) findViewById(R.id.recipe_strength);
        difficulty = (TextView) findViewById(R.id.recipe_difficulty);
        theme = (TextView) findViewById(R.id.recipe_theme);
        served = (TextView) findViewById(R.id.recipe_served);
        preparation = (TextView) findViewById(R.id.recipe_prep);

        new FetchDrinkDetailsAsyncTask().execute();
        new FetchDrinkIngredAsyncTask().execute();
    }

    private class FetchDrinkDetailsAsyncTask extends AsyncTask<String, String, String[][]>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected String[][] doInBackground(String... strings) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(DRINK_NAME, drinkName);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "get_drink_details.php", "GET", httpParams);
            String[] results;
            try {
                int success = jsonObject.getInt(KEY_SUCCESS);
                JSONObject jsonResponse;
                if (success == 1) {
                    //Parse the JSON response
                    jsonResponse = jsonObject.getJSONObject(KEY_DATA);
                    Log.v("JSONRESPONSE HERE", jsonResponse.toString());
                    drinkName = jsonResponse.getString(DRINK_NAME);
                    attributes[0] = jsonResponse.getString(AUTHOR);
                    attributes[1] = jsonResponse.getString(ABOUT);
                    attributes[2] = jsonResponse.getString(INSTRUCTIONS);
                    attributes[3] = jsonResponse.getString(RATING);
                    attributes[4] = jsonResponse.getString(RATING_COUNT);
                    attributes[5] = jsonResponse.getString(PREPARATION);
                    attributes[6] = jsonResponse.getString(STRENGTH);
                    attributes[7] = jsonResponse.getString(DIFFICULTY);
                    attributes[8] = jsonResponse.getString(THEME);
                    attributes[9] = jsonResponse.getString(GLASS);
                    attributes[10] = jsonResponse.getString(COCKTAILTYPE);
                    attributes[11] = jsonResponse.getString(SERVED);
                    attributes[12] = jsonResponse.getString(AUTHORID);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[][] s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            runOnUiThread(new Runnable() {
                public void run() {
                    //Populate the Edit Texts once the network activity is finished executing
                    title.setText(drinkName);
                    author.setText(attributes[0]);
                    about.setText(attributes[1]);
                    instructions.setText(attributes[2]);
                    try{
                        String stringRating = (String) attributes[3];
                        float ratingValue = Float.parseFloat(attributes[3]);
                        ratingBar.setRating(ratingValue);
                        ratingCount.setText(attributes[4] + " Ratings");
                    } catch (NullPointerException e){
                        Log.e("No rating available",e.toString());
                    }
                    setPaletteAttr(5, preparation);
                    setPaletteAttr(6, strength);
                    setPaletteAttr(7, difficulty);
                    setPaletteAttr(8, theme);
                    setPaletteAttr(9,  glass);
                    setPaletteAttr(10, type);
                    setPaletteAttr(11, served);
//                    authorID.setText(attributes[11]);
                }
            });
        }
    }

    // Sets the palette attributes
    private void setPaletteAttr(int index, TextView textView) {
        if(!attributes[index].equals("null")) {
            textView.setText(attributes[index]);
        } else {
            ((LinearLayout) textView.getParent()).setVisibility(View.GONE);
        }
    }

    /**
     * Fetches the list of drinks from the server
     */
    private class FetchDrinkIngredAsyncTask extends AsyncTask<String, String, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(DRINK_NAME, drinkName);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_drink_ingred.php", "GET", httpParams);
            Log.v("Fetch drinks", jsonObject.toString());
            try {
                int success = jsonObject.getInt(KEY_SUCCESS);
                JSONArray drinkArray;
                if (success == 1) {
                    drinkList = new ArrayList<>();
                    drinkArray = jsonObject.getJSONArray(KEY_DATA);
                    //Iterate through the response and populate movies list
                    for (int i = 0; i < drinkArray.length(); i++) {
                        JSONObject drink = drinkArray.getJSONObject(i);
//                        String drinkName = drink.getString(DRINK_NAME);
                        String ingred = drink.getString(INGREDIENT);
                        String amount = drink.getString(AMOUNT);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(INGREDIENT, ingred.toString());
                        map.put(AMOUNT, amount.toString());
                        drinkList.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return 0;
            }
            return 1;
        }

        protected void onPostExecute(Integer result) {
            if(result != 0){
                runOnUiThread(new Runnable() {
                    public void run() {
                        populateIngredList();
                    }
                });
            } else {
                Toast.makeText(ViewRecipe.this, "Failed to get ingredients.", Toast.LENGTH_LONG).show();
            }

        }

    }

    /**
     * Updating parsed JSON data into ListView
     */
    private void populateIngredList() {
        SimpleAdapter adapter = new SimpleAdapter(
                this, drinkList,
                R.layout.ingred_list_item, new String[]{INGREDIENT,
                AMOUNT},
                new int[]{R.id.ingred_name, R.id.ingred_amount});
        // updating listview
        ingredients.setAdapter(adapter);
//        // draw a dividing line
        ingredients.setDivider(ContextCompat.getDrawable(this, R.drawable.divider));
        ingredients.setDividerHeight(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}

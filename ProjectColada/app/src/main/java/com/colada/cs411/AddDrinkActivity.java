package com.colada.cs411;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.colada.cs411.helper.CheckNetworkStatus;
import com.colada.cs411.helper.HttpJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddDrinkActivity extends AppCompatActivity {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DRINK_NAME = "movie_name";
    private static final String KEY_INSTRUCTIONS = "genre";
    private static final String KEY_YEAR = "year";
    private static final String KEY_RATING = "rating";
    private static final String BASE_URL = "http://drowningindata.web.engr.illinois.edu/colada/";
    private static String STRING_EMPTY = "";
    private EditText drinkNameEditText;
    private EditText genreEditText;
    private EditText yearEditText;
    private EditText ratingEditText;
    private String drinkName;
    private String genre;
    private String year;
    private String rating;
    private Button addButton;
    private int success;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_drink);
        drinkNameEditText = (EditText) findViewById(R.id.txtMovieNameAdd);
        genreEditText = (EditText) findViewById(R.id.txtGenreAdd);
        yearEditText = (EditText) findViewById(R.id.txtYearAdd);
        ratingEditText = (EditText) findViewById(R.id.txtRatingAdd);
        addButton = (Button) findViewById(R.id.btnAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    addDrink();
                } else {
                    Toast.makeText(AddDrinkActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    /**
     * Checks whether all files are filled. If so then calls AddDrinkAsyncTask.
     * Otherwise displays Toast message informing one or more fields left empty
     */
    private void addDrink() {
        if (!STRING_EMPTY.equals(drinkNameEditText.getText().toString()) &&
                !STRING_EMPTY.equals(genreEditText.getText().toString()) &&
                !STRING_EMPTY.equals(yearEditText.getText().toString()) &&
                !STRING_EMPTY.equals(ratingEditText.getText().toString())) {

            drinkName = drinkNameEditText.getText().toString();
            genre = genreEditText.getText().toString();
            year = yearEditText.getText().toString();
            rating = ratingEditText.getText().toString();
            new AddDrinkAsyncTask().execute();
        } else {
            Toast.makeText(AddDrinkActivity.this,
                    "One or more fields left empty!",
                    Toast.LENGTH_LONG).show();

        }


    }

    /**
     * AsyncTask for adding a drink
     */
    private class AddDrinkAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display proggress bar
            pDialog = new ProgressDialog(AddDrinkActivity.this);
            pDialog.setMessage("Adding drink. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Populating request parameters
            httpParams.put(KEY_DRINK_NAME, drinkName);
            httpParams.put(KEY_INSTRUCTIONS, genre);
            httpParams.put(KEY_YEAR, year);
            httpParams.put(KEY_RATING, rating);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "add_drink.php", "POST", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        //Display success message
                        Toast.makeText(AddDrinkActivity.this,
                                "Drink Added", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie update
                        setResult(20, i);
                        //Finish ths activity and go back to listing activity
                        finish();

                    } else {
                        Toast.makeText(AddDrinkActivity.this,
                                "Some error occurred while adding drink",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }
}

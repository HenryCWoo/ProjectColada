package com.colada.cs411;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.colada.cs411.helper.CheckNetworkStatus;
import com.colada.cs411.helper.HttpJsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DrinkListingActivity extends AppCompatActivity {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_MOVIE_ID = "movie_id";
    private static final String KEY_DRINK_NAME = "movie_name";
    private static final String BASE_URL = "http://drowningindata.web.engr.illinois.edu/colada/";
    private ArrayList<HashMap<String, String>> drinkList;
    private ListView drinkListView;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_listing);
        drinkListView = (ListView) findViewById(R.id.drinkList);
        new FetchDrinkAsyncTask().execute();

    }

    /**
     * Fetches the list of drinks from the server
     */
    private class FetchDrinkAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(DrinkListingActivity.this);
            pDialog.setMessage("Loading drinks. Please wait...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_all_drinks.php", "GET", null);
            Log.v("Fetch",jsonObject.toString());
            try {
                int success = jsonObject.getInt(KEY_SUCCESS);
                JSONArray drinkArray;
                if (success == 1) {
                    drinkList = new ArrayList<>();
                    drinkArray = jsonObject.getJSONArray(KEY_DATA);
                    //Iterate through the response and populate movies list
                    for (int i = 0; i < drinkArray.length(); i++) {
                        JSONObject drink = drinkArray.getJSONObject(i);
                        Integer movieId = drink.getInt(KEY_MOVIE_ID);
                        String drinkName = drink.getString(KEY_DRINK_NAME);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_MOVIE_ID, movieId.toString());
                        map.put(KEY_DRINK_NAME, drinkName);
                        drinkList.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    populateDrinkList();
                }
            });
        }

    }

    /**
     * Updating parsed JSON data into ListView
     * */
    private void populateDrinkList() {
        ListAdapter adapter = new SimpleAdapter(
                DrinkListingActivity.this, drinkList,
                R.layout.list_item, new String[]{KEY_MOVIE_ID,
                KEY_DRINK_NAME},
                new int[]{R.id.drinkID, R.id.drinkName});
        // updating listview
        drinkListView.setAdapter(adapter);
        //Call DrinkUpdateDeleteActivity when a drink is clicked
        drinkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Check for network connectivity
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    String drinkid = ((TextView) view.findViewById(R.id.drinkID))
                            .getText().toString();
                    Intent intent = new Intent(getApplicationContext(),
                            DrinkUpdateDeleteActivity.class);
                    intent.putExtra(KEY_MOVIE_ID, drinkid);
                    startActivityForResult(intent, 20);

                } else {
                    Toast.makeText(DrinkListingActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();

                }


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 20) {
            // If the result code is 20 that means that
            // the user has deleted/updated the movie.
            // So refresh the movie listing
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

}

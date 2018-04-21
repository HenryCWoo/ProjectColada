package com.example.henry.projectcolada.MainActivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.henry.projectcolada.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecipeFragment extends Fragment {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_MOVIE_ID = "movie_id";
    private static final String KEY_DRINK_NAME = "movie_name";
    private static final String BASE_URL = "http://drowningindata.web.engr.illinois.edu/colada/";
    private ArrayList<HashMap<String, String>> drinkList;
    private ListView drinkListView;
    private ProgressDialog pDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_people_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();

        drinkListView = v.findViewById(R.id.peopleList);
        new RecipeFragment.FetchDrinkAsyncTask().execute();
    }

    /**
     * Fetches the list of drinks from the server
     */
    private class FetchDrinkAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading drinks. Please wait...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            com.example.henry.projectcolada.helper.HttpJsonParser httpJsonParser = new com.example.henry.projectcolada.helper.HttpJsonParser();
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_all_drinks.php", "GET", null);
            Log.v("Fetch", jsonObject.toString());
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
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    populateDrinkList();
                }
            });
        }

    }

    /**
     * Updating parsed JSON data into ListView
     */
    private void populateDrinkList() {
        ListAdapter adapter = new SimpleAdapter(
                getActivity(), drinkList,
                R.layout.fragment_people_list_item, new String[]{KEY_MOVIE_ID,
                KEY_DRINK_NAME},
                new int[]{R.id.drinkID, R.id.drinkName});
        // updating listview
        drinkListView.setAdapter(adapter);
//        // draw a dividing line
//        drinkListView.setDivider(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
//        drinkListView.setDividerHeight(1);
        //Call DrinkUpdateDeleteActivity when a drink is clicked
        drinkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Check for network connectivity
                if (com.example.henry.projectcolada.helper.CheckNetworkStatus.isNetworkAvailable(getActivity().getApplicationContext())) {
                    String drinkid = ((TextView) view.findViewById(R.id.drinkID))
                            .getText().toString();
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            null); //TODO: create an activity to view profiles
                    intent.putExtra(KEY_MOVIE_ID, drinkid);
                    startActivityForResult(intent, 20);

                } else {
                    Toast.makeText(getActivity(),
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();

                }


            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 20) {
            // If the result code is 20 that means that
            // the user has deleted/updated the movie.
            // So refresh the movie listing
            Intent intent = getActivity().getIntent();
            getActivity().finish();
            startActivity(intent);
        }
    }


    public RecipeFragment() {
        // Required empty public constructor
    }
}

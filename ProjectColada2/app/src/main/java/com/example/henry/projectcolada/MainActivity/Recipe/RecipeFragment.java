package com.example.henry.projectcolada.MainActivity.Recipe;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.henry.projectcolada.R;
import com.example.henry.projectcolada.helper.CheckNetworkStatus;

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
    private static final String DRINK_NAME = "drinkName";
    private static final String AUTHOR = "author";
    private static final String RATING = "rating";
    private static final String BASE_URL = "http://drowningindata.web.engr.illinois.edu/colada/";
    private ArrayList<HashMap<String, String>> drinkList;
    private ListView drinkListView;
    private ProgressBar pDialog;
    private FloatingActionButton addRecipeFAB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();

        drinkListView = v.findViewById(R.id.recipeList);
        pDialog = v.findViewById(R.id.recipe_fragment_pb);
        addRecipeFAB = v.findViewById(R.id.add_recipe_fab);

        addRecipeFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "FAB clicked.", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getActivity().getApplicationContext(), null);
//                startActivity(intent);
            }
        });
        new RecipeFragment.FetchDrinkAsyncTask().execute();
    }

    /**
     * Fetches the list of drinks from the server
     */
    private class FetchDrinkAsyncTask extends AsyncTask<String, String, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            com.example.henry.projectcolada.helper.HttpJsonParser httpJsonParser = new com.example.henry.projectcolada.helper.HttpJsonParser();
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_all_drinks.php", "GET", null);
//            Log.v("Fetch drinks", jsonObject.toString());
            try {
                int success = jsonObject.getInt(KEY_SUCCESS);
                JSONArray drinkArray;
                if (success == 1) {
                    drinkList = new ArrayList<>();
                    drinkArray = jsonObject.getJSONArray(KEY_DATA);
                    //Iterate through the response and populate movies list
                    for (int i = 0; i < drinkArray.length(); i++) {
                        JSONObject drink = drinkArray.getJSONObject(i);
                        String drinkName = drink.getString(DRINK_NAME);
                        String author = drink.getString(AUTHOR);
                        if(author.equals("null")) {
                            author = "";
                        }
                        Double rating = drink.getDouble(RATING);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(DRINK_NAME, drinkName.toString());
                        map.put(AUTHOR, author.toString());
                        map.put(RATING, rating.toString());
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
            pDialog.setVisibility(View.GONE);
            if(result != 0){
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        populateDrinkList();
                    }
                });
            } else {
                Toast.makeText(getActivity(), "Failed to get data.", Toast.LENGTH_LONG).show();
            }

        }

    }

    /**
     * Updating parsed JSON data into ListView
     */
    private void populateDrinkList() {
        SimpleAdapter adapter = new SimpleAdapter(
                getActivity(), drinkList,
                R.layout.fragment_recipe_list_item, new String[]{DRINK_NAME,
                AUTHOR, RATING},
                new int[]{R.id.drink_name, R.id.drink_author, R.id.drink_rating});
        adapter.setViewBinder(new ratingBinder());
        // updating listview
        drinkListView.setAdapter(adapter);
//        // draw a dividing line
        drinkListView.setDivider(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
        drinkListView.setDividerHeight(1);
        //Call DrinkUpdateDeleteActivity when a drink is clicked
        drinkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Check for network connectivity
                if (CheckNetworkStatus.isNetworkAvailable(getActivity().getApplicationContext())) {
                    String drinkName = ((TextView) view.findViewById(R.id.drink_name))
                            .getText().toString();
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            ViewRecipe.class);
                    intent.putExtra(DRINK_NAME, drinkName);
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

    // Custom binder to set rating and strings in a list element
    class ratingBinder implements SimpleAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if(view.getId() == R.id.drink_rating){
                String stringval = (String) data;
                float ratingValue = Float.parseFloat(stringval);
                RatingBar ratingBar = (RatingBar) view;
                ratingBar.setRating(ratingValue);
                return true;
            } else if(view.getId() == R.id.drink_author) {
                String stringval = (String) data;
                if(stringval.equals("")) {
                    view.setVisibility(View.GONE);
                }
            }
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        pDialog.setVisibility(View.GONE);
    }
}


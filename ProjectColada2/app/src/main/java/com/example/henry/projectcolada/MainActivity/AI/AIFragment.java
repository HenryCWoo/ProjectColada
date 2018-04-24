package com.example.henry.projectcolada.MainActivity.AI;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.henry.projectcolada.MainActivity.Recipe.RecipeFragment;
import com.example.henry.projectcolada.MainActivity.Recipe.ViewRecipe;
import com.example.henry.projectcolada.R;
import com.example.henry.projectcolada.helper.CheckNetworkStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class AIFragment extends Fragment {
    private static final String DRINKNAME = "drinkName";
    private ImageButton generate;
    private ArrayList<HashMap<String, String>> drinkList;
    private TensorFlowInferenceInterface inferenceInterface;
    private ListView selectionList;
    private String[] rawList = new String[]{"aila", "borassus flabellifer", "honey", "merisa", "manx spirit", "apfelwein", "gouqi jiu", "jenever", "bagaço", "palm wine", "brown ale", "kasiri", "rhum agricole", "apples", "palm", "lozovača", "fruit brandy", "moonshine", "palinka", "gin", "sake", "poitín ", "maotai", "pastis", "table wine", "tequila", "nihamanchi", "tej", "sorghum", "arak", "eau-de-vie", "irish whiskey", "perry", "molasses", "guaro", "cask ale", "canadian whisky", "rum", "apricots", "mbege", "chuoi hot", "choujiu", "poire williams", "pulque", "sherry", "yangmei jiu", "sparkling wine", "wine", "burukutu", "grappa", "coyol wine", "coconut", "törkölypálinka", "fermenting", "baijiu", "vinjak", "rakı", "feni", "shōchū", "singani", "pomegranate", "urgwagwa", "scotch whisky", "zivania", "sloe gin", "pito", "raki", "orujo", "tonto", "marsala", "tennessee whiskey", "metaxa", "witbier", "basi", "rakia", "rice baijiu", "plum jerkum", "beer", "champagne", "kirsch", "juniper berries", "kefir", "cauim", "fruit beer", "viljamovka", "pale ale", "corn beer", "korn", "ginger wine", "calvados", "cider", "plum wine", "țuică", "raicilla", "kilju", "wheat beer", "port", "brem", "umeshu", "tsipouro", "pinga", "mamajuana", "horilka", "honey-flavored liqueur", "ti root", "sweet potato", "majmunovača", "myrica rubra", "madeira", "damassine", "ale", "blaand", "sahti", "cognac", "tepache", "tembo", "wheat", "whiskey", "cocoroco", "slivovitz", "pisco", "dunjevača", "vermouth", "bitter ale", "marc", "awamori", "stout", "distillation", "ginger ale", "buckwheat", "old ale", "ouzo", "plums", "sugar", "tuak", "rye", "tescovină", "krushova rakia", "huangjiu", "tsikoudia", "desi daru", "icariine liquor", "edit", "ruou gao", "akvavit", "borovička", "neutral grain spirit", "sangria", "saliva-fermented beverages", "tiswin", "barley wine", "fortified wine", "shochu", "japanese whisky", "democratic republic of the congo", "applejack", "stock ale", "pálinka", "kvass", "damson gin", "grapes", "milk", "pilsener", "tongba", "sonti", "boza", "kajsijevača", "bourbon whiskey", "armagnac", "fruit wine", "arenga pinnata", "mild ale", "chicha", "chungju", "pears", "himbeergeist", "williamine", "rye beer", "kaoliang wine", "gouqi", "ginger beer", "sugarcane", "clairin", "pineapples", "bandundu province", "hypopta agavis", "shōchū ", "parakari", "lager", "tesguino", "poiré", "makgeolli", "tiquira", "mead", "obstwasser", "porter", "pomace", "agave", "french caribbean", "kumis", "mezcal", "rye whiskey", "thwon", "arrack", "trester", "absinthe", "poitín", "bilibili", "aguardiente", "betsa-betsa", "scotch ale", "quinces", "vinsanto", "puncheon rum", "maerzen/oktoberfest beer", "ţuică", "sambuca", "jabukovača", "cassava", "whisky", "kasikisi", "okolehao", "bock", "millet", "schwarzbier", "millet beer", "pomace wine", "absinthe spoon", "ogogoro", "kaisieva rakia", "cashew", "pale lager", "brandy", "soju", "slivova rakia", "vodka", "toddy", "barleywine", "cachaça", "schnapps", "šljivovica", "beers"};
    private float[] tensor, output;
    private ArrayList<Integer> indices = new ArrayList<>();
    private ProgressBar tensorPB;
    private TextView results;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        tensorPB = v.findViewById(R.id.tensor_pb);
        results = v.findViewById(R.id.results);
        drinkList = new ArrayList<>();
        selectionList = v.findViewById(R.id.selection_list);
        for(int i=0; i<rawList.length; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(DRINKNAME, rawList[i]);
            map.put("INDEX", String.valueOf(i));
            drinkList.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(
                getActivity(), drinkList,
                R.layout.fragment_ai_element, new String[]{DRINKNAME, "INDEX"},
                new int[]{R.id.ai_drink_element, R.id.index});
        selectionList.setAdapter(adapter);
        selectionList.setDivider(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
        selectionList.setDividerHeight(1);
        selectionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int selection = Integer.parseInt(((TextView) view.findViewById(R.id.index)).getText().toString());
                indices.add(selection);
                TextView curBox = (TextView) view.findViewById(R.id.ai_drink_element);
                String added = curBox.getText().toString();
                Toast.makeText(getActivity(), added+" added.", Toast.LENGTH_SHORT).show();
            }
        });
        generate = v.findViewById(R.id.generate);
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectionList.setVisibility(View.GONE);
                new GetComboAsyncTask().execute();
            }
        });

    }

    /**
     * Fetches the list of drinks from the server
     */
    private class GetComboAsyncTask extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            tensorPB.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            Log.v("list length", Integer.toString(rawList.length));
            inferenceInterface = new TensorFlowInferenceInterface(getActivity().getAssets(), "tensorflow_lite_coladaAIModel.pb");
            ArrayList<ArrayList<Integer>> combos = new ArrayList<>();
            tensor = new float[rawList.length];
            for(int i=0; i<rawList.length;i++){
                tensor[i] = 0;
            }
            for(int i=0; i<indices.size(); i++){
                tensor[indices.get(i)] = 1;
            }
//            for(int i=0; i<10; i++){
//                getRandom(indices);
//            }
            output = predict(tensor);
            return null;
        }

        protected void onPostExecute(int result) {
            tensorPB.setVisibility(View.GONE);
            if (result != 0) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        results.setVisibility(View.VISIBLE);
                        results.setText(String.valueOf(output[0]));
                    }
                });
            }
        }
    }

    private float[] predict(float[] input){
        // model has only 1 output neuron
        float output[] = new float[1];

        // feed network with input of shape (1,input.length) = (1,2)
        inferenceInterface.feed("dense_1_input", input, 1, input.length);
        inferenceInterface.run(new String[]{"dense_2/Relu"});
        inferenceInterface.fetch("dense_2/Relu", output);

        // return prediction
        return output;
    }

    private int getRandom(ArrayList<Integer> array) {
        int rnd = new Random().nextInt(array.size());
        return array.get(rnd);
    }
}

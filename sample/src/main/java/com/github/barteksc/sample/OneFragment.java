package com.github.barteksc.sample;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.sample.model.Dictionary;
import com.helger.jcodemodel.JArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nrtdemo-NB on 11/12/2016.
 */

public class OneFragment extends Fragment {
    private String TAG = OneFragment.class.getSimpleName();
    private static String url = "https://owlbot.info/api/v1/dictionary/cat?format=json";

    ArrayList<Dictionary> dictList;

    private ProgressDialog pDialog;
    private EditText editText;
    private TextView output;
    private ListView lv;

    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_one, container, false);
        dictList = new ArrayList<>();

        output = (TextView) rootView.findViewById(R.id.textView);
        output.setMovementMethod(new ScrollingMovementMethod());
        editText = (EditText) rootView.findViewById(R.id.editText);

        lv = (ListView) rootView.findViewById(R.id.list_item);

        Button submit = (Button) rootView.findViewById(R.id.buttonSubmit);
        submit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        output.setText(editText.getText());
                        //inputUrl(editText.getText().toString());
                        new MyTask().execute();
                    }
                }
        );
        Button btn_close = (Button) rootView.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getFragmentManager().beginTransaction().remove(OneFragment.this).commit();
            }
        });


        return rootView;
    }

    void inputUrl(String s) {
        url = "https://owlbot.info/api/v1/dictionary/" + s + "?format=json";
    }

    private class MyTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... strings) {
            HttpHandler sh = new HttpHandler();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG,"Response from url: " + jsonStr);

            if(!jsonStr.isEmpty()){
                try{
                    JSONArray jsonAr = new JSONArray(jsonStr);
                    for (int i=0; i<jsonAr.length();i++){
                        JSONObject o = jsonAr.getJSONObject(i);
                        Dictionary dict = new Dictionary();
                        dict.setType(o.getString("type"));
                        dict.setDefenition(o.getString("defenition"));
                        dict.setExample(o.getString("example"));
                        dictList.add(dict);
                    }
                }catch (final JSONException e){
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),Toast.LENGTH_SHORT)
                            .show();
                        }
                    });
                }
            }else {
                Log.e(TAG, "Couldn't get json from server.");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();
            
            String test = null;
            if(!dictList.isEmpty()){
                for(Dictionary d : dictList){
                   test = d.getDefenition();
                    break;
                }
            }
            Toast.makeText(getActivity().getApplicationContext(),test,Toast.LENGTH_LONG).show();
        }
    }
}

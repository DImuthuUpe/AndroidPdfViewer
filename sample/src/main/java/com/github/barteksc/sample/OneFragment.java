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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nrtdemo-NB on 11/12/2016.
 */

public class OneFragment extends Fragment{

    private ProgressDialog pDialog;
    private String TAG = OneFragment.class.getSimpleName();

    private static String url;
    private EditText editText;
    private TextView output;
    private ListView lv;

    ArrayList<HashMap<String, String>> contactList;

    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_one, container, false);
        contactList = new ArrayList<>();

        output = (TextView) rootView.findViewById(R.id.textView);
        output.setMovementMethod(new ScrollingMovementMethod());
        editText=(EditText)rootView.findViewById(R.id.editText);

        lv = (ListView) rootView.findViewById(R.id.list_item);

        Button submit = (Button)rootView.findViewById(R.id.buttonSubmit);
        submit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        output.setText(editText.getText());
                        inputUrl(editText.getText().toString());
                        new GetContacts().execute();
                    }
                }
        );
        Button btn_close = (Button)rootView.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               getFragmentManager().beginTransaction().remove(OneFragment.this).commit();
            }
        });


        return rootView;
    }

    void inputUrl(String s) {
        url = "https://owlbot.info/api/v1/dictionary/"+ s +"?format=json";
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            //Toast.makeText(OneFragment.this,jsonStr,Toast.LENGTH_SHORT).show();

            if (jsonStr != null) {
                try {
                    //JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray word = new JSONArray(jsonStr);
                    // Getting JSON Array node
                    //JSONArray contacts = jsonObj.getJSONArray("contacts");

                    // looping through All Contacts
                    for (int i = 0; i < word.length(); i++) {
                        JSONObject c = word.getJSONObject(i);

                        String type = c.getString("type");
                        String defenition = c.getString("defenition");
                        String example = c.getString("example");
//                        String address = c.getString("address");
//                        String gender = c.getString("gender");

                        // Phone node is JSON Object
//                        JSONObject phone = c.getJSONObject("phone");
//                        String mobile = phone.getString("mobile");
//                        String home = phone.getString("home");
//                        String office = phone.getString("office");

                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();
                        // adding each child node to HashMap key => value
                        contact.put("type", type);
                        contact.put("defenition", defenition);
                        contact.put("example", example);
//                        contact.put("mobile", mobile);

                        // adding contact to contact list
                        contactList.add(contact);

                        //Toast.makeText(getApplicationContext());
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(),
//                                    "Json parsing error: " + e.getMessage(),
//                                    Toast.LENGTH_LONG)
//                                    .show();
//                        }
//                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(),
//                                "Couldn't get json from server. Check LogCat for possible errors!",
//                                Toast.LENGTH_LONG)
//                                .show();
//                    }
//                });

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    OneFragment.this, contactList,R.layout.list_item,
                    new String[]{"type", "defenition","example"},
                    new int[]{R.id.tvType,R.id.tvDefenition, R.id.tvExample});

//            lv.setAdapter(adapter);
        }


    }


}

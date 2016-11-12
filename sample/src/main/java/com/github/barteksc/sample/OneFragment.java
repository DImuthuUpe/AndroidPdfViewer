package com.github.barteksc.sample;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;

/**
 * Created by Nrtdemo-NB on 11/12/2016.
 */

public class OneFragment extends Fragment{
//    private ProgressDialog pDialog;

    private EditText editText;
    private TextView output;
    private static String url ;

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

        output = (TextView) rootView.findViewById(R.id.textView);
        output.setMovementMethod(new ScrollingMovementMethod());

        editText=(EditText)rootView.findViewById(R.id.editText);

        Button submit = (Button)rootView.findViewById(R.id.buttonSubmit);
        submit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        output.setText(editText.getText());
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

    void writeUrl(String s){
        url = "https://owlbot.info/api/v1/dictionary/"+ s +"?format=json";
    }


}

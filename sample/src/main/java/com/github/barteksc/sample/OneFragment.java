package com.github.barteksc.sample;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Nrtdemo-NB on 11/12/2016.
 */

public class OneFragment extends Fragment{
    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

//        View rootView = inflater.inflate(R.layout.fragment_one, container, false);
//
//        Button btn_close = (Button)rootView.findViewById(R.id.btn_close);
//        btn_close.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                getFragmentManager().beginTransaction().remove(OneFragment.this).commit();
//            }
//        });
        return inflater.inflate(R.layout.fragment_one, container, false);
    }
}

package com.mirror.demoedgecomputing.ui.home;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mirror.demoedgecomputing.R;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Timestamp;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private HomeViewModel homeViewModel;
    private  EditText edit_range;
    private Button submit_button;
    private TextView tv_result;
    Timestamp timestamp1;
    public String url = "";
    boolean edge_status = false;
    boolean isWifiConn = false;
    boolean isMobileConn = false;
    int ii;
    String range;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        edit_range = root.findViewById(R.id.editTextRange);
        submit_button = root.findViewById(R.id.button);
        tv_result = root.findViewById(R.id.textViewResult);

        submit_button.setOnClickListener(this);
        /*final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        return root;
    }

    public String generateFibanocci(int ii) {
        StringBuffer s = new StringBuffer();
        BigInteger a = new BigInteger("0");
        BigInteger b = new BigInteger("0");
        BigInteger c = new BigInteger("1");

        for (int i = 1; i <= ii; i++) {
            a = b;
            b = c;
            c = a.add(b);
            s.append(c.toString() + "\n");
             System.out.println("Ouput : " + s);


        }
        return s.toString();
    }

    public void generate(String url){
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.

                        Timestamp timestamp3 = new Timestamp(System.currentTimeMillis());
                        long diff = timestamp3.getTime() - timestamp1.getTime();
                        double diff1 = diff/1000;
                        Toast.makeText(getActivity(),"Time Taken " + diff1+" seconds",Toast.LENGTH_LONG).show();
                        tv_result.setText("Time taken : " + diff + "\n Response is: "+ response);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                tv_result.setText("Error while connecting");
            }
        });

// Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        int socketTimeout = 1200000;//2 minutes - change to what you want
        //RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        queue.add(stringRequest);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.button:
                timestamp1 = new Timestamp(System.currentTimeMillis());
//2016-11-16 06:43:19.77

                range = edit_range.getText().toString();
                ii = Integer.parseInt(range);
                System.out.println("Number entered" + range );



                // Instantiate the RequestQueue.




                ConnectivityManager connMgr =
                        (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                isWifiConn = false;
                isMobileConn = false;
                edge_status = false;

                for (Network network : connMgr.getAllNetworks()) {
                    NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        isWifiConn |= networkInfo.isConnected();
                    }
                    if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        isMobileConn |= networkInfo.isConnected();
                    }
                }

                Log.d("Edge", "Wifi connected: " + isWifiConn);
                Log.d("Edge", "Mobile connected: " + isMobileConn);

                url = "";
                Socket socket = new Socket();


                if(isWifiConn){
                    url ="http://192.168.0.102:5000/";
                    //url ="http://192.168.43.43:5000/" + range;

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Display the first 500 characters of the response string.

                                    edge_status = true;
                                    Log.d("Edge","Edge status:" + edge_status);
                                    url ="http://192.168.0.102:5000/fibanocci/" + range;
                                    generate(url);


                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            edge_status = false;
                            Log.d("Edge","Edge status:" + edge_status);
                            if(isMobileConn) {
                                url ="http://100.25.164.29/fibanocci/" + range;
                                generate(url);
                            }else{
                                String result = generateFibanocci(ii);

                                Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
                                long diff = timestamp2.getTime() - timestamp1.getTime();
                                double diff1 = diff/1000;
                                Toast.makeText(getActivity(),"Time Taken " + diff1+" seconds",Toast.LENGTH_LONG).show();
                                tv_result.setText("Time taken : " + diff1 + "\n"+ result);


                            }

                        }
                    });

// Add the request to the RequestQueue.
                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    int socketTimeout = 300;//2 minutes - change to what you want
                    //RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    stringRequest.setRetryPolicy(policy);
                    queue.add(stringRequest);
                }else if(isMobileConn){
                    url ="http://100.25.164.29/fibanocci/" + range;
                    generate(url);
                }

                if(!isWifiConn && !isMobileConn){
                    Log.d("Edge Debug","Device"+ ii);
                    String s= generateFibanocci(ii);
                    Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
                    long diff = timestamp2.getTime() - timestamp1.getTime();
                    double diff1 = diff/1000;
                    Toast.makeText(getActivity(),"Time Taken " + diff1+" seconds",Toast.LENGTH_LONG).show();
                    tv_result.setText("Time taken : " + diff1 + "\n"+ s);
                    
                    //tv_result.setText(s);
                }







/*
                    //String url ="http://192.168.0.101:5000/fibanocci/" + range;

// Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Display the first 500 characters of the response string.

                                    Timestamp timestamp3 = new Timestamp(System.currentTimeMillis());
                                    long diff = timestamp3.getTime() - timestamp1.getTime();
                                    double diff1 = diff/1000;
                                    Toast.makeText(getActivity(),"Time Taken " + diff1+" seconds",Toast.LENGTH_LONG).show();
                                    tv_result.setText("Time taken : " + diff + "\n Response is: "+ response);


                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            tv_result.setText("Error while connecting");
                        }
                    });

// Add the request to the RequestQueue.
                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    int socketTimeout = 1200000;//2 minutes - change to what you want
                    //RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    stringRequest.setRetryPolicy(policy);
                    queue.add(stringRequest);

                    */



                break;
        }
    }
}
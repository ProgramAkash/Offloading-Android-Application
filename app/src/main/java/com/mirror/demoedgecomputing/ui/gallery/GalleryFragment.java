package com.mirror.demoedgecomputing.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mirror.demoedgecomputing.R;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class GalleryFragment extends Fragment implements View.OnClickListener {

    private GalleryViewModel galleryViewModel;

    private TextView textView_image;
    int PICK_IMAGE_REQUEST = 111;
    private Bitmap bitmap;
    private ImageView img ;
    private Button button_submit;
    Timestamp timestamp1;
    public String url = "";
    boolean edge_status = false;
    boolean isWifiConn = false;
    boolean isMobileConn = false;
    String imageString;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        //  textView_image = root.findViewById(R.id.text_image);
        //  textView_image.setOnClickListener(this);
        img = root.findViewById(R.id.imgView);
        img.setOnClickListener(this);

        button_submit = root.findViewById(R.id.button_submit);
        button_submit.setOnClickListener(this);
        /*final TextView textView = root.findViewById(R.id.text_gallery);
        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        }); */
        return root;
    }

    @Override
    public void onClick(View v) {
        Log.d("Edge Computing","clicked");
        switch (v.getId()){

            case R.id.imgView:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
                break;

            case R.id.button_submit:
                //converting image to base64 string
                timestamp1 = new Timestamp(System.currentTimeMillis());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                //sending image to server
                //String url ="http://192.168.29.43:5000/gesture";
                //String url ="http://192.168.0.101:5000/gesture";

                //String url ="http://arunkumar-latitude-e6420:5000/gesture";

                ConnectivityManager connMgr =
                        (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                isWifiConn = false;
                isMobileConn = false;
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
                                    url ="http://192.168.0.102:5000/gesture" ;
                                    imageprocess(url);


                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            edge_status = false;
                            Log.d("Edge","Edge status:" + edge_status);
                            if(isMobileConn) {
                                url ="http://100.25.164.29/gesture" ;
                                imageprocess(url);
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
                    url ="http://100.25.164.29/gesture";
                    imageprocess(url);
                }
/*
                StringRequest request = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>(){
                    @Override
                    public void onResponse(String s) {
                        //progressDialog.dismiss();
                        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();

                        Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
                        long diff = timestamp2.getTime() - timestamp1.getTime();
                        double diff1 = diff/1000;
                        Toast.makeText(getActivity(),"Time Taken "+ diff1+" seconds",Toast.LENGTH_LONG).show();


                    }
                },new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getActivity(), "Some error occurred -> "+volleyError, Toast.LENGTH_LONG).show();;
                    }
                }) {
                    //adding parameters to send
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> parameters = new HashMap<String, String>();
                        parameters.put("image", imageString);
                        return parameters;
                    }
                };

                RequestQueue rQueue = Volley.newRequestQueue(getActivity());




                int socketTimeout = 120000;//2 minutes - change to what you want
                //RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                request.setRetryPolicy(policy);
                rQueue.add(request); */


        }

    }

    public void imageprocess(String url){

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String s) {
                        //progressDialog.dismiss();
                        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();

                        Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
                        long diff = timestamp2.getTime() - timestamp1.getTime();
                        double diff1 = diff/1000;
                        Toast.makeText(getActivity(),"Time Taken "+ diff1+" seconds \n" + s,Toast.LENGTH_LONG).show();


                    }
                },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), "Some error occurred -> "+volleyError, Toast.LENGTH_LONG).show();;
            }
        }) {
            //adding parameters to send
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("image", imageString);
                return parameters;
            }
        };

        RequestQueue rQueue = Volley.newRequestQueue(getActivity());




        int socketTimeout = 120000;//2 minutes - change to what you want
        //RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        rQueue.add(request);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            Log.d("Edge",filePath.toString());

            try {
                //getting image from gallery
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);

                //Setting image to ImageView
                img.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
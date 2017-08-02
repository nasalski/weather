package com.example.wwwsl.testweather;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.places.PlaceManager;
import com.facebook.places.model.PlaceFields;
import com.facebook.places.model.PlaceInfoRequestParams;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends Activity {

    private CallbackManager callbackManager;
    private TextView info;
    private LoginButton loginButton;

    private String firstName;
    private String lastName;
    private String email;
    private String birthday;
    private String gender;
    private String location;
    private String city;
    private String country;
    private URL profilePicture;
    private String userId;
    private String TAG = "LoginActivity";
    public String url;
    private long high;
    private long low;
    Const consts;
    //firebase
    private DatabaseReference mSimpleDatabaseReference;
    private FirebaseRecyclerAdapter<FireMessage, FireMsgViewHolder>
            mFirebaseAdapter;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);
        //mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);


        //info = (TextView) findViewById(R.id.info);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_location");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                //"Auth Token: " + loginResult.getAccessToken().getToken());
                request();



                /*PlaceInfoRequestParams.Builder builder = new PlaceInfoRequestParams.Builder();

                builder.addField(PlaceFields.LOCATION);
                GraphRequest request = PlaceManager.newPlaceInfoRequest(builder.build());

                request.setCallback(new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        Log.v("","");

                    }
                });

                request.executeAsync();*/
                /*GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("","");
                    }
                });*/
            }

            @Override
            public void onCancel() {
                //info.setText("Login attempt cancelled.");
            }

            @Override
            public void onError(FacebookException e) {
                //info.setText("Login attempt failed.");
            }
        });


        request();


    }

    private void request() {
        /*info.setText("User ID:  " +
                AccessToken.getCurrentAccessToken().getUserId());// + "\n" +*/
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.e(TAG, object.toString());
                Log.e(TAG, response.toString());

                try {
                    userId = object.getString("id");
                    /*profilePicture = new URL("https://graph.facebook.com/" + userId + "/picture?width=500&height=500");
                    if (object.has("first_name"))
                        firstName = object.getString("first_name");
                    if (object.has("last_name"))
                        lastName = object.getString("last_name");
                    if (object.has("email"))
                        email = object.getString("email");
                    if (object.has("birthday"))
                        birthday = object.getString("birthday");
                    if (object.has("gender"))
                        gender = object.getString("gender");*/
                    if (object.has("location"))
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject = object.getJSONObject("location");
                            location = jsonObject.getString("name");
                            String[] arrayLocation = location.split(", ", 2);
                            city = arrayLocation[0];
                            country = arrayLocation[1];
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    consts = new Const();
                    url = consts.BASE_URL;
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(url)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    ApiInterface service = retrofit.create(ApiInterface.class);
                    String query = "select * from weather.forecast where woeid in (select woeid from geo.places where text='" + city + "') and u='c'";
                    //String query ="select%20*%20from%20weather.forecast%20where%20woeid%20%3D%202502265" ; //test
                    String format = "json";
                    Call<Resp> call = service.getWeather(query, format);
                    call.enqueue(new Callback<Resp>() {
                        @Override
                        public void onResponse(Call<Resp> call, Response<Resp> response) {
                            Log.v("", "");
                            List<Forecast> forecasts;
                            forecasts = response.body().getQuery().getResults().getChannel().getItem().getForecast();
                            high = forecasts.get(0).getHigh();
                            low = forecasts.get(0).getLow();
                            workWithFireBase();
                            sendWeather();
                        }

                        @Override
                        public void onFailure(Call<Resp> call, Throwable t) {
                            Log.v("", "");
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                } /*catch (MalformedURLException e) {
                    e.printStackTrace();  // url dont need now
                }*/
            }
        });
        //Here we put the requested fields to be returned from the JSONObject
        Bundle parameters = new Bundle();
        //parameters.putString("fields", "id, first_name, last_name, email, birthday, gender, location");
        parameters.putString("fields", "id, location");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void workWithFireBase() {
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mSimpleDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<FireMessage,
                FireMsgViewHolder>(
                FireMessage.class,
                R.layout.message_item,
                FireMsgViewHolder.class,
                mSimpleDatabaseReference.child("information")) {

            @Override
            protected void populateViewHolder(FireMsgViewHolder viewHolder, FireMessage friendlyMessage, int position) {
                //mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.cityTextView.setText(friendlyMessage.getCity());
                viewHolder.highTextView.setText(Long.toString(friendlyMessage.getHigh()));
                viewHolder.lowTextView.setText(Long.toString(friendlyMessage.getLow()));

            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    public static class FireMsgViewHolder extends RecyclerView.ViewHolder {
        public TextView cityTextView;
        public TextView highTextView;
        public TextView lowTextView;
        //public CircleImageView userImageView;

        public FireMsgViewHolder(View v) {
            super(v);
            cityTextView = (TextView) itemView.findViewById(R.id.cityTextView);
            highTextView = (TextView) itemView.findViewById(R.id.highTextView);
            lowTextView = (TextView) itemView.findViewById(R.id.lowTextView);
            //userImageView = (CircleImageView) itemView.findViewById(R.id.userImageView);
        }
    }
    private void sendWeather(){
        FireMessage friendlyMessage = new
                FireMessage(city, high, low);
        mSimpleDatabaseReference.child("information")
                .push().setValue(friendlyMessage);
    }
}

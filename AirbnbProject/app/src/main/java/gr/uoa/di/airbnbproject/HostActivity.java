package gr.uoa.di.airbnbproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

import fromRESTful.Residences;
import fromRESTful.Users;
import util.ListAdapterHostResidences;
import util.RetrofitCalls;
import util.Utils;

public class HostActivity extends AppCompatActivity {
    String username, token;
    Users host;

    ImageButton baddResidence;

    ListAdapterHostResidences adapter;
    ListView residencesList;
    int[] residenceId;

    Boolean user;
    private static final String USER_LOGIN_PREFERENCES = "login_preferences";
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    private boolean isUserLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefs = getApplicationContext().getSharedPreferences(USER_LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        isUserLoggedIn = sharedPrefs.getBoolean("userLoggedInState", false);
        username = sharedPrefs.getString("currentLoggedInUser", "");

        if (!isUserLoggedIn) {
            Intent intent = new Intent(this, GreetingActivity.class);
            startActivity(intent);
            return;
        }

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        setContentView(R.layout.activity_host);

        user=false;
        Bundle buser = getIntent().getExtras();
        token = buser.getString("token");
        baddResidence = (ImageButton)findViewById(R.id.addResidence);

        baddResidence.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent addResidenceIntent = new Intent(HostActivity.this, AddResidenceActivity.class);
                Bundle buser = new Bundle();
                buser.putBoolean("type", user);
                addResidenceIntent.putExtras(buser);
                try {
                    startActivity(addResidenceIntent);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        RetrofitCalls retrofitCalls = new RetrofitCalls();
        ArrayList<Users> hostUsers = retrofitCalls.getUserbyUsername(token, username);
        host = hostUsers.get(0);
        ArrayList<Residences> storedResidences = retrofitCalls.getResidencesByHost(token, host.getId().toString());

        String[] representativePhoto    = new String [storedResidences.size()];
        String[] title                  = new String[storedResidences.size()];
        String[] city                   = new String[storedResidences.size()];
        Double[] price                  = new Double[storedResidences.size()];
        float[] rating                  = new float[storedResidences.size()];
        residenceId                     = new int[storedResidences.size()];

        for(int i=0; i<storedResidences.size();i++){
            representativePhoto[i]  = storedResidences.get(i).getPhotos();
            title[i]                = storedResidences.get(i).getTitle();
            city[i]                 = storedResidences.get(i).getCity();
            price[i]                = storedResidences.get(i).getMinPrice();
            rating[i]               = (float)storedResidences.get(i).getAverageRating();
            residenceId[i]          = storedResidences.get(i).getId();
        }

        adapter = new ListAdapterHostResidences(this, representativePhoto, title, city, price, rating, residenceId);
        residencesList = (ListView)findViewById(R.id.residenceList);
        residencesList.setAdapter(adapter);

        residencesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent showResidenceIntent = new Intent(HostActivity.this, ResidenceActivity.class);
                Bundle btype = new Bundle();
                btype.putBoolean("type",user);
                btype.putString("token", token);
                btype.putInt("residenceId", residenceId[position]);
                showResidenceIntent.putExtras(btype);
                try {
                    startActivity(showResidenceIntent);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        /** FOOTER TOOLBAR **/
        Utils.manageFooter(HostActivity.this, user, token);
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(this, HomeActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//        super.onBackPressed();
//        return;
        moveTaskToBack(true);
    }
}

package gr.uoa.di.airbnbproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import fromRESTful.Residences;
import fromRESTful.Users;
import util.RecyclerAdapterHostResidences;
import util.RetrofitCalls;
import util.Session;
import util.Utils;

import static util.Utils.DELETE_ACTION;
import static util.Utils.EDIT_ACTION;
import static util.Utils.RESERVATIONS_ACTION;
import static util.Utils.VIEW_RESIDENCE_ACTION;
import static util.Utils.goToActivity;
import static util.Utils.reloadActivity;

public class HostActivity extends AppCompatActivity {
    String username, token;
    Users host;
    Toolbar toolbar;

    Button baddResidence;
    int[] residenceId;

    Boolean user;
    Context c;

    RecyclerView residencesRecyclerView;
    RecyclerView.Adapter residencesAdapter;
    RecyclerView.LayoutManager residencesLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Session sessionData = Utils.getSessionData(HostActivity.this);
        token = sessionData.getToken();
        user=false;
        //c=this;

        if (!sessionData.getUserLoggedInState()) {
            Utils.logout(this);
            return;
        }
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        if(Utils.isTokenExpired(sessionData.getToken())){
            Toast.makeText(c, "Session is expired", Toast.LENGTH_SHORT).show();
            Utils.logout(this);
            finish();
            return;
        }

        setContentView(R.layout.activity_host);
        toolbar = (Toolbar) findViewById(R.id.backToolbar);
        toolbar.setTitle("Your Residences");
        setSupportActionBar(toolbar);

        baddResidence = (Button)findViewById(R.id.addResidence);
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
                    Log.i("",ex.getMessage());
                }
            }
        });
        RetrofitCalls retrofitCalls = new RetrofitCalls();
        ArrayList<Users> hostUsers = retrofitCalls.getUserbyUsername(token, sessionData.getUsername());
        host = hostUsers.get(0);

        residencesRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewHost);
        residencesLayoutManager = new GridLayoutManager(this, 1);
        residencesRecyclerView.setLayoutManager(residencesLayoutManager);
        residencesRecyclerView.setHasFixedSize(true);

        ArrayList<Residences> storedResidences = retrofitCalls.getResidencesByHost(token, host.getId().toString());

        try {
            if (storedResidences.size() > 0) {
                System.out.println(storedResidences);
                residencesAdapter = new RecyclerAdapterHostResidences(this, user, storedResidences);
                residencesRecyclerView.setAdapter(residencesAdapter);
            }
        } catch (Exception e) {
            Log.e("", e.getMessage());
        }

        /** FOOTER TOOLBAR **/
        Utils.manageFooter(HostActivity.this, user);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle("Reservations - Host Options");

        menu.add(0, info.position, 0, VIEW_RESIDENCE_ACTION);
        menu.add(0, info.position, 1, EDIT_ACTION);
        menu.add(0, info.position, 2, RESERVATIONS_ACTION);
        menu.add(0, info.position, 3, DELETE_ACTION);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        super.onContextItemSelected(item);
        final Bundle btype = new Bundle();
        btype.putBoolean("type", user);

        if (item.getTitle().equals(VIEW_RESIDENCE_ACTION)) {
            btype.putInt("residenceId", residenceId[item.getItemId()]);
            goToActivity(this, ResidenceActivity.class, btype);
        }
        else if (item.getTitle().equals(EDIT_ACTION)) {
            btype.putInt("residenceId", residenceId[item.getItemId()]);
            goToActivity(this, EditResidenceActivity.class, btype);
        }
        else if (item.getTitle().equals(RESERVATIONS_ACTION)) {
            btype.putInt("residenceId", residenceId[item.getItemId()]);
            goToActivity(this, HistoryReservationsActivity.class, btype);
        }
        else if (item.getTitle().equals(DELETE_ACTION)) {
            new AlertDialog.Builder(this)
                .setTitle("Delete Residence").setMessage("Do you really want to delete this residence?").setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        RetrofitCalls retrofitCalls = new RetrofitCalls();
                        if (retrofitCalls.deleteResidenceById(token, Integer.toString(residenceId[item.getItemId()])) == null) {
                            Toast.makeText(HostActivity.this, "Residence was successfully deleted!", Toast.LENGTH_SHORT).show();
                            reloadActivity(HostActivity.this, btype);
                        } else {
                            Toast.makeText(HostActivity.this, "Something went wrong, residence is not deleted. Please try again!", Toast.LENGTH_SHORT).show();
                        }
                    }})
                .setNegativeButton(android.R.string.no, null).show();

        } else {
            Toast.makeText(this, item.getTitle(), Toast.LENGTH_LONG).show();
        }
        return true;
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
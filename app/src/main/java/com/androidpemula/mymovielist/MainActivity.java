package com.androidpemula.mymovielist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    RecyclerView rvCategory;
    TextView tvErrorMessage;
    public ArrayList<Movie> allMoviesList;
    final String STATE_TITLE = "state_string";
    final String STATE_LIST = "state_list";
    final String STATE_MODE = "state_mode";
    final String sortBy = "popular";
    int mode;
    int previous_mode;
    public static final String MODE_PREF = "current_mode_pref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvCategory = findViewById(R.id.rv_category);
        rvCategory.setHasFixedSize(true);
        tvErrorMessage = findViewById(R.id.tv_error_message);

        allMoviesList = new ArrayList<>();
        previous_mode = R.id.action_list;

        SharedPreferences prefs = getSharedPreferences(MODE_PREF, MODE_PRIVATE);
        int currentMode = prefs.getInt("current_mode", R.id.action_list);
        loadMovieData(sortBy);
        setMode(currentMode);

        /*if (savedInstanceState == null) {
            setActionBarTitle("Mode List");
            loadMovieData(sortBy);

        } else {
            String stateTitle = savedInstanceState.getString(STATE_TITLE);
            ArrayList<Movie> stateList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            previous_mode = savedInstanceState.getInt(STATE_MODE);
            setActionBarTitle(stateTitle);
            allMoviesList.addAll(stateList);
            setMode(previous_mode);

            //
            int previousMode = getIntent().getIntExtra("previous_mode", R.id.action_list);
            setMode(previousMode);
        }*/
    }

    public void showErrorMessage() {
        rvCategory.setVisibility(View.INVISIBLE);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    public void showData() {
        rvCategory.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void loadMovieData(String sortBy) {
        new MovieTask().execute(sortBy);
    }

    class MovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        @Override
        protected ArrayList<Movie> doInBackground(String... strings) {
            String sortBy = strings[0];
            URL url = Networking.buildURL(sortBy);
            ArrayList<Movie> movieDataArrList = new ArrayList<>();
            String jsonData = null;
            try {
                jsonData = Networking.getHttpResponse(url);
                if (jsonData != null && !jsonData.equals("")) {
                    movieDataArrList = setArrayListData(jsonData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return movieDataArrList;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movieDataArrList) {
            super.onPostExecute(movieDataArrList);
            if (movieDataArrList.size() == 0) {
                showErrorMessage();
            } else {
                showData();
                SharedPreferences prefs = getSharedPreferences(MODE_PREF, MODE_PRIVATE);
                int currentMode = prefs.getInt("current_mode", R.id.action_list);
                switch (currentMode) {
                    case R.id.action_list:
                        showRecyclerList();
                        break;
                    case R.id.action_grid:
                        showRecyclerGrid();
                        break;
                    case R.id.action_cardview:
                        showRecyclerCardView();
                        break;
                }
            }
        }
    }

    public ArrayList<Movie> setArrayListData(String jsonData) throws JSONException {
        ArrayList<Movie> arrayListData = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray jsonArray = jsonObject.getJSONArray("results");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject movieJsonObject = jsonArray.getJSONObject(i);
            arrayListData.add(new Movie(
                    movieJsonObject.getString("original_title"),
                    movieJsonObject.getString("poster_path"),
                    movieJsonObject.getString("overview"),
                    movieJsonObject.getString("vote_average"),
                    movieJsonObject.getString("release_date")
            ));
        }
        allMoviesList = arrayListData;
        return allMoviesList;
    }

    private void showSelectedMovie(Movie movie) {
        Toast.makeText(this, "Kamu memilih " + movie.getOriginal_title(), Toast.LENGTH_SHORT).show();
    }

    private void showRecyclerList() {
        rvCategory.setLayoutManager(new LinearLayoutManager(this));
        ListMovieAdapter listMovieAdapter = new ListMovieAdapter(this);
        listMovieAdapter.setListMovie(allMoviesList);
        rvCategory.setAdapter(listMovieAdapter);

        ItemClickSupport.addTo(rvCategory).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
                intent.putExtra("movieDetails", allMoviesList.get(position));
                startActivity(intent);
            }
        });
    }

    private void showRecyclerGrid() {
        rvCategory.setLayoutManager(new GridLayoutManager(this, 2));
        GridMovieAdapter gridMovieAdapter = new GridMovieAdapter(this);
        gridMovieAdapter.setListMovie(allMoviesList);
        rvCategory.setAdapter(gridMovieAdapter);

        ItemClickSupport.addTo(rvCategory).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
                intent.putExtra("movieDetails", allMoviesList.get(position));
                startActivity(intent);
            }
        });
    }

    private void showRecyclerCardView() {
        rvCategory.setLayoutManager(new LinearLayoutManager(this));
        CardViewMovieAdapter cardViewMovieAdapter = new CardViewMovieAdapter(this);
        cardViewMovieAdapter.setListMovie(allMoviesList);
        rvCategory.setAdapter(cardViewMovieAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        setMode(item.getItemId());

        return super.onOptionsItemSelected(item);
    }

    public void setMode(int selectedMode) {
        String title = null;
        SharedPreferences.Editor editor = getSharedPreferences(MODE_PREF, MODE_PRIVATE).edit();
        switch (selectedMode) {
            case R.id.action_list:
                title = "Mode List";
                editor.putInt("current_mode", R.id.action_list);
                editor.apply();
                showRecyclerList();
                break;

            case R.id.action_grid:
                title = "Mode Grid";
                editor.putInt("current_mode", R.id.action_grid);
                editor.apply();
                showRecyclerGrid();
                break;

            case R.id.action_cardview:
                title = "Mode CardView";
                editor.putInt("current_mode", R.id.action_cardview);
                editor.apply();
                showRecyclerCardView();
                break;
        }
        setActionBarTitle(title);
        SharedPreferences prefs = getSharedPreferences(MODE_PREF, MODE_PRIVATE);
        int currentMode = prefs.getInt("current_mode", R.id.action_list);
        mode = currentMode;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_TITLE, getSupportActionBar().getTitle().toString());
        outState.putParcelableArrayList(STATE_LIST, allMoviesList);
        outState.putInt(STATE_MODE, mode);
    }
}
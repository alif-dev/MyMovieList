package com.androidpemula.mymovielist;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


public class MovieDetailsActivity extends AppCompatActivity {
    TextView title;
    ImageView movieImage;
    TextView userRating;
    TextView releaseDate;
    TextView overview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        title = findViewById(R.id.movie_title);
        movieImage = findViewById(R.id.movie_image_detail);
        userRating = findViewById(R.id.movie_rating);
        releaseDate = findViewById(R.id.movie_release_date);
        overview = findViewById(R.id.movie_overview);

        Bundle movieBundle = getIntent().getExtras();
        Movie movie = movieBundle.getParcelable("movieDetails");

        title.setText(movie.getOriginal_title());
        Glide.with(this)
                .load("http://image.tmdb.org/t/p/w342" + movie.getMovie_poster())
                .apply(new RequestOptions().override(350, 550))
                .into(movieImage);
        userRating.setText(getString(R.string.user_rating_text) + movie.getUser_rating());
        releaseDate.setText(getString(R.string.release_date_text) + movie.getRelease_date());
        overview.setText(movie.getPlot_synopsis());
    }
}


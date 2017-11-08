package com.example.chris.popularMovies2.utilities;

/**
 * Created by chris on 11/7/17.
 */

public class MovieReview {
    String review;
    String author;

    public MovieReview(String author,String review) {
        this.review = review;
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }
    public String getReview() {
        return review;
    }
}

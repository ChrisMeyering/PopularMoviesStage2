package com.example.chris.popularMovies2.utilities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chris on 9/26/17.
 */

public class MovieInfo implements Parcelable {
    private int vote_count = -1;
    private int id = -1;
    private boolean video = false;
    private double vote_average = -1;
    private String title = "Title not found";
    private long popularity = 0;
    private String poster_path = null;
    private String original_language = "Original language unknown";
    private String original_title = "Original title not provided";
    private String [] genre_names;
    private String backdrop_path = "Backdrop path undefined";
    private boolean adult = false;
    private String overview = "No description provided.";
    private String release_date = "yyyy-mm-dd";
    private MovieReview[] reviews = null;
    private String[] trailers;

    protected MovieInfo() {    }
    protected MovieInfo(Parcel in) {
        vote_count = in.readInt();
        id = in.readInt();
        video = in.readByte() != 0;
        vote_average = in.readDouble();
        title = in.readString();
        popularity = in.readLong();
        poster_path = in.readString();
        original_language = in.readString();
        original_title = in.readString();
        genre_names = in.createStringArray();
        backdrop_path = in.readString();
        adult = in.readByte() != 0;
        overview = in.readString();
        release_date = in.readString();
    }

    public static final Creator<MovieInfo> CREATOR = new Creator<MovieInfo>() {
        @Override
        public MovieInfo createFromParcel(Parcel in) {
            return new MovieInfo(in);
        }

        @Override
        public MovieInfo[] newArray(int size) {
            return new MovieInfo[size];
        }
    };

    void setVote_count(int vote_count) {
        this.vote_count = vote_count;
    }
    void setId(int id) {
        this.id = id;
    }
    void setVideo (boolean video) {
        this.video = video;
    }
    void setVote_average(double vote_average) {
        this.vote_average = vote_average;
    }
    void setTitle(String title) {
        this.title = title;
    }
    void setPopularity(long popularity) {
        this.popularity = popularity;
    }
    void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }
    void setOriginal_language(String original_language) {
        this.original_language = original_language;
    }
    void setOriginal_title(String original_title) {
        this.original_title = original_title;
    }
    void setGenre_names(String[] genre_names) {
        this.genre_names = genre_names;
    }
    void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }
    void setAdult(boolean adult) {
        this.adult = adult;
    }
    void setOverview(String overview){
        this.overview = overview;
    }
    void setRelease_date(String release_date) {
        this.release_date = release_date;
    }
    void setTrailers(String[] trailer_keys) { this.trailers = trailer_keys;}
    void setReviews(MovieReview[] reviews) { this.reviews = reviews;}
    public int getVote_count() {
        return vote_count;
    }
    public int getId() {
        return id;
    }
    public boolean hasVideo() {
        return video;
    }
    public double getVote_average() {
        return vote_average;
    }
    public String getTitle() {
        return title;
    }
    public long getPopularity() {
        return popularity;
    }
    public String getPoster_path() {
        return poster_path;
    }
    public String getOriginal_language() {
        return original_language;
    }
    public String getOriginal_title() {
        return original_title;
    }
    public String getGenre_names() {
        String result = "";
        for (String s :genre_names) {
            result += s;
            if (s == genre_names[genre_names.length -1])
                result += ".";
            else
                result += ", ";
        }
        return result;
    }
    public String getBackdrop_path(){
        return backdrop_path;
    }
    public boolean isAdult(){
        return adult;
    }
    public String getOverview() {
        return overview;
    }
    public String getRelease_date() {
        return release_date;
    }
    public String getInfo() {
        return "id : " + String.valueOf(id) +
                "\ntitle : " + title +
                "\nvote_average : " + String.valueOf(vote_average) +
                "\npopularity : " + String.valueOf(popularity) +
                "\nposter path : " + poster_path +
                "\ndescription : " + overview +
                "\nrelease date : " + release_date;
    }

    public String[] getTrailers() {
        return trailers;
    }
    public MovieReview[] getReviews() {
        return reviews;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(vote_count);
        parcel.writeInt(id);
        parcel.writeByte((byte) (video ? 1 : 0));
        parcel.writeDouble(vote_average);
        parcel.writeString(title);
        parcel.writeLong(popularity);
        parcel.writeString(poster_path);
        parcel.writeString(original_language);
        parcel.writeString(original_title);
        parcel.writeStringArray(genre_names);
        parcel.writeString(backdrop_path);
        parcel.writeByte((byte) (adult ? 1 : 0));
        parcel.writeString(overview);
        parcel.writeString(release_date);
    }
}

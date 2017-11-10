package com.example.chris.popularMovies2.utilities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chris on 9/26/17.
 */

public class MovieInfo implements Parcelable {
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
    private int voteCount = -1;
    private int id = -1;
    private boolean video = false;
    private double voteAverage = -1;
    private String title = "Title not found";
    private long popularity = 0;
    private String posterPath = null;
    private String originalLanguage = "Original language unknown";
    private String originalTitle = "Original title not provided";
    private String[] genreNames;
    private String backdropPath = "Backdrop path undefined";
    private boolean adult = false;
    private String overview = "No description provided.";
    private String releaseDate = "yyyy-mm-dd";
    private MovieReview[] reviews = null;
    private String[] trailers;

    protected MovieInfo() {
    }

    protected MovieInfo(Parcel in) {
        voteCount = in.readInt();
        id = in.readInt();
        video = in.readByte() != 0;
        voteAverage = in.readDouble();
        title = in.readString();
        popularity = in.readLong();
        posterPath = in.readString();
        originalLanguage = in.readString();
        originalTitle = in.readString();
        genreNames = in.createStringArray();
        backdropPath = in.readString();
        adult = in.readByte() != 0;
        overview = in.readString();
        releaseDate = in.readString();
    }

    void setVideo(boolean video) {
        this.video = video;
    }

    public int getVoteCount() {
        return voteCount;
    }

    void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public boolean hasVideo() {
        return video;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public long getPopularity() {
        return popularity;
    }

    void setPopularity(long popularity) {
        this.popularity = popularity;
    }

    public String getPosterPath() {
        return posterPath;
    }

    void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getGenreNames() {
        String result = "";
        for (String s : genreNames) {
            result += s;
            if (s == genreNames[genreNames.length - 1])
                result += ".";
            else
                result += ", ";
        }
        return result;
    }

    void setGenreNames(String[] genreNames) {
        this.genreNames = genreNames;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public boolean isAdult() {
        return adult;
    }

    void setAdult(boolean adult) {
        this.adult = adult;
    }

    public String getOverview() {
        return overview;
    }

    void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getInfo() {
        return "id : " + String.valueOf(id) +
                "\ntitle : " + title +
                "\nvoteAverage : " + String.valueOf(voteAverage) +
                "\npopularity : " + String.valueOf(popularity) +
                "\nposter path : " + posterPath +
                "\ndescription : " + overview +
                "\nrelease date : " + releaseDate;
    }

    public String[] getTrailers() {
        return trailers;
    }

    void setTrailers(String[] trailerKeys) {
        this.trailers = trailerKeys;
    }

    public MovieReview[] getReviews() {
        return reviews;
    }

    void setReviews(MovieReview[] reviews) {
        this.reviews = reviews;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(voteCount);
        parcel.writeInt(id);
        parcel.writeByte((byte) (video ? 1 : 0));
        parcel.writeDouble(voteAverage);
        parcel.writeString(title);
        parcel.writeLong(popularity);
        parcel.writeString(posterPath);
        parcel.writeString(originalLanguage);
        parcel.writeString(originalTitle);
        parcel.writeStringArray(genreNames);
        parcel.writeString(backdropPath);
        parcel.writeByte((byte) (adult ? 1 : 0));
        parcel.writeString(overview);
        parcel.writeString(releaseDate);
    }
}

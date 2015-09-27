package com.sayo.android.popularmovies;

import android.graphics.Bitmap;

/**
 * Created by simona2 on 21/09/2015.
 */
public class MovieInfo {

    private String originalTitle;
    private String synopsis;
    private String posterURL;
    private String rating;
    private String releaseDate;
    private String id;
    private String duration;

    private Bitmap posterImage;

    public MovieInfo() {}
    public MovieInfo(String id, String originalTitle, String posterURL, String synopsis,
                     String rating, String releaseDate){
        this.id = id;
        this.originalTitle = originalTitle;
        this.synopsis = synopsis;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.posterURL = posterURL;
    }

    public String getDuration() { return duration; }

    public void setDuration(String duration) { this.duration = duration; }

    public Bitmap getPosterImage() { return posterImage; }

    public void setPosterImage(Bitmap posterImage) { this.posterImage = posterImage; }

    public String getId() {  return id; }

    public void setId(String id) { this.id = id;}

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getPosterURL() { return posterURL; }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}

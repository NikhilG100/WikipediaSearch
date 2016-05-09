package com.example.nikhil.wikipediasearch;

/**
 * Created by Nikhil on 4/30/2016.
 */
public interface NotifySearchResults {
    void OnSearchCompleted();
    void OnSearchInProgress();
    void OnSearchCancelled();
    void OnSearchFailed();
}

package edu.inha.hellocookieya.video;

public interface VideoListFragmentCallback {
    public void onVideoEmpty();
    public void onVideoAdded();
    public void onFragmentCreateView();
    public void onVideoDeleted();
    public void resetToolbarText();
}

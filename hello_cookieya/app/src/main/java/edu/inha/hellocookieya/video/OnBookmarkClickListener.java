package edu.inha.hellocookieya.video;

public interface OnBookmarkClickListener {
    public void onBookmarkClicked(int position);
    public void onBookmarkDeleteClicked(int position);
    public void onBookmarkNameEdited(int position, String editedName);
    public void onBookmarkBreakpointClicked(int position);
    public void onBookmarkRepeatClicked(int position);
}

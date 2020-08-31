package edu.inha.hellocookieya.video;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import edu.inha.hellocookieya.R;

import java.util.ArrayList;

import timber.log.Timber;

public class VideoPreviewAdapter extends RecyclerView.Adapter<VideoPreviewAdapter.ViewHolder>
                                implements OnVideoPreviewItemClickListener{

    private Fragment parentFragment;
    private ArrayList<VideoItem> items = new ArrayList<VideoItem>();
    private OnVideoPreviewItemClickListener previewItemClickListener;

    public VideoPreviewAdapter(Fragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    public ArrayList<VideoItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<VideoItem> items) {
        this.items = items;
    }

    public void addItem(VideoItem item) {
        items.add(item);
    }

    public VideoItem getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    public int getItemPosition(VideoItem item) {
        int position = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).get_id() == item.get_id()) {
                position = i;
                break;
            }
        }
        return position;
    }

    public void setItem(int position, VideoItem item) {
        items.set(position, item);
    }

    public VideoItem deleteItem(int position) {
        return items.remove(position);
    }

    public void setPreviewItemClickListener(OnVideoPreviewItemClickListener previewItemClickListener) {
        this.previewItemClickListener = previewItemClickListener;
    }

    @Override
    public void onItemClicked(int position) {
        if(previewItemClickListener != null) {
            previewItemClickListener.onItemClicked(position);
        }
    }

    @Override
    public void onItemDeleteClicked(int position) {
        if (previewItemClickListener != null) {
            previewItemClickListener.onItemDeleteClicked(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.video_preview, viewGroup, false);

        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if (items != null) {
            VideoItem item = items.get(position);
            if (parentFragment != null) {

                Glide.with(viewHolder.videoPreviewThumbnailView)
                        .load("https://img.youtube.com/vi/" + item.getVideo_youtube_id() + "/sddefault.jpg")
                        .into(viewHolder.videoPreviewThumbnailView);

                viewHolder.videoIdTextView.setText(String.valueOf(position + 1));
                viewHolder.videoPreviewVideoTitle.setText(item.getTitle());
            }
        } else {
            Timber.e("VideoPreviewAdapter 의 items 가 null 상태");
        }
    }

    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView videoPreviewThumbnailView;
        private TextView videoIdTextView;
        private TextView videoPreviewVideoTitle;

        public ViewHolder(@NonNull View itemView, final OnVideoPreviewItemClickListener listener) {
            super(itemView);

            videoPreviewThumbnailView = itemView.findViewById(R.id.videoPreviewThumbnailImageView);
            videoIdTextView = itemView.findViewById(R.id.videoIdTextView);
            videoPreviewVideoTitle = itemView.findViewById(R.id.videoPreviewVideoTitle);
            ImageButton deleteButton = itemView.findViewById(R.id.deleteButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null) {
                        listener.onItemClicked(position);
                    }
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null) {
                        listener.onItemDeleteClicked(position);
                    }
                }
            });

        }
    }
}

package edu.inha.hellocookieya.video;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import edu.inha.hellocookieya.R;

import java.util.ArrayList;
import java.util.Collections;

import timber.log.Timber;

public class PlayVideoContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
                                    implements OnBookmarkClickListener {

    private Context context;
    private ArrayList<PlayVideoContent> items = new ArrayList<PlayVideoContent>();
    private OnBookmarkClickListener onBookmarkClickListener;

    public PlayVideoContentAdapter(Context context) {
        this.context = context;
    }

    public void setOnBookmarkClickListener(OnBookmarkClickListener onBookmarkClickListener) {
        this.onBookmarkClickListener = onBookmarkClickListener;
    }

    public ArrayList<PlayVideoContent> getItems() {
        return items;
    }

    public void setItems(ArrayList<PlayVideoContent> items) {
        this.items = items;
    }

    public PlayVideoContent getItem(int position) {
        return items.get(position);
    }

    public void addItem(PlayVideoContent item) {
        items.add(item);
    }

    public void addBookmarkItem(PlayVideoContent bookmarkItem) {
        for (int i = 0; i < items.size(); i++) {
            PlayVideoContent item = items.get(i);
            if (item.getContentType() != PlayVideoContent.TYPE_BOOKMARK)
                continue;

            if (bookmarkItem.getBookmarkTime() < item.getBookmarkTime()) {
                addItemTo(i, bookmarkItem);
                return;
            }
        }
        addItemTo(items.size() - 1, bookmarkItem);
    }

    public void addItemTo(int index, PlayVideoContent item) {
        items.add(index, item);
    }

    public void addItems(ArrayList<PlayVideoContent> contents) {
        items.addAll(contents);
    }

    public void setItem(int position, PlayVideoContent item) {
        items.set(position, item);
    }

    public void deleteItem(int position) {
        items.remove(position);
    }

    public PlayVideoContent getBookmarkItemFromID(int _id) {
        PlayVideoContent ret = null;
        for (PlayVideoContent item : items) {
            if (item.getContentType() == PlayVideoContent.TYPE_BOOKMARK
                    && item.getBookmarkNumber() == _id) {
                ret = item;
            }
        }
        return ret;
    }

    public PlayVideoContent getBookmarkItem(int position) {
        position--;   // 보여지는 북마크 번호는 1부터, 실제 인덱싱은 0부터
        position += 2;    // description 이랑 separator
        PlayVideoContent ret = null;
        for (PlayVideoContent item : items) {
            if (item.getContentType() == PlayVideoContent.TYPE_BOOKMARK
            && item.getAdapterIndex() == position) {
                ret = item;
            }
        }
        return ret;
    }

    public void sortItems() {
        Collections.sort(items);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView;
        if (viewType == PlayVideoContent.TYPE_DESCRIPTION) {
            itemView = inflater.inflate(R.layout.play_video_content_description, viewGroup, false);
            return new DescriptionViewHolder(itemView);
        }
        else if (viewType == PlayVideoContent.TYPE_SEPARATOR) {
            itemView = inflater.inflate(R.layout.play_video_content_bookmark_separator, viewGroup, false);
            return new SeparatorViewHolder(itemView);
        }
        else if (viewType == PlayVideoContent.TYPE_BOOKMARK) {
            itemView = inflater.inflate(R.layout.play_video_content_bookmark, viewGroup, false);
            return new BookmarkViewHolder(itemView, this);
        }
        else if (viewType == PlayVideoContent.TYPE_FOOTER) {
            itemView = inflater.inflate(R.layout.play_video_content_footer, viewGroup, false);
            return new FooterViewHolder(itemView);
        }

        itemView = inflater.inflate(R.layout.play_video_content_footer, viewGroup, false);
        return new FooterViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (items != null) {

            int type = getItemViewType(position);
            PlayVideoContent content = items.get(position);
            content.setAdapterIndex(position);

            if (type == PlayVideoContent.TYPE_DESCRIPTION) {
                ((DescriptionViewHolder)holder).setDescriptionContent(content);
            }
            else if (type == PlayVideoContent.TYPE_BOOKMARK) {
                ((BookmarkViewHolder)holder).setBookmarkContent(content);
            }

        } else {
            Timber.e("PlayVideoContentAdapter 의 items 가 null 상태");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        PlayVideoContent item = items.get(position);
        return item.getContentType();
    }

    @Override
    public void onBookmarkClicked(int position) {
        if (onBookmarkClickListener != null) {
            onBookmarkClickListener.onBookmarkClicked(position);
        }
    }

    @Override
    public void onBookmarkDeleteClicked(int position) {
        if (onBookmarkClickListener != null) {
            onBookmarkClickListener.onBookmarkDeleteClicked(position);
        }
    }

    @Override
    public void onBookmarkNameEdited(int position, String editedName) {
        if (onBookmarkClickListener != null) {
            onBookmarkClickListener.onBookmarkNameEdited(position, editedName);
        }
    }

    @Override
    public void onBookmarkBreakpointClicked(int position) {
        if (onBookmarkClickListener != null) {
            onBookmarkClickListener.onBookmarkBreakpointClicked(position);
        }
    }

    @Override
    public void onBookmarkRepeatClicked(int position) {
        if (onBookmarkClickListener != null) {
            onBookmarkClickListener.onBookmarkRepeatClicked(position);
        }
    }

    public static class DescriptionViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout playVideoDescriptionLayout;
        private TextView playVideoTitleTextView;
        private TextView playVideoDescriptionTextView;
        private ImageView extendImage;

        private boolean isExtended = false;

        public DescriptionViewHolder(@NonNull View itemView) {
            super(itemView);

            playVideoDescriptionLayout = itemView.findViewById(R.id.playVideoDescriptionLayout);
            playVideoTitleTextView = itemView.findViewById(R.id.playVideoTitleTextView);
            playVideoDescriptionTextView = itemView.findViewById(R.id.playVideoDescriptionTextView);
            extendImage = itemView.findViewById(R.id.extendImage);

            playVideoDescriptionLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isExtended) {
                        playVideoTitleTextView.setMaxLines(2);
                        playVideoDescriptionTextView.setVisibility(TextView.GONE);
                    }
                    else {
                        playVideoTitleTextView.setMaxLines(1000);
                        playVideoDescriptionTextView.setVisibility(TextView.VISIBLE);
                    }
                    isExtended = !isExtended;
                }
            });
        }

        public void setDescriptionContent(PlayVideoContent content) {
            playVideoTitleTextView.setText(content.getTitle());
            playVideoDescriptionTextView.setText(content.getDescription());
        }
    }

    public static class SeparatorViewHolder extends RecyclerView.ViewHolder {

        public SeparatorViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class BookmarkViewHolder extends RecyclerView.ViewHolder {

        private CardView bookmarkCardView;
        private TextView bookmarkNumberTextView;
        private TextView bookmarkTimeTextView;
        private TextView bookmarkDescriptionTextView;
        private EditText bookmarkEditText;
//        private ImageButton breakpointButton;
//        private ImageButton repeatButton;


//        private boolean isRepeatActivated = false;
//        private boolean isBPActivated = false;
        private boolean isEditBookmarkActivated = false;

        public BookmarkViewHolder(@NonNull View itemView, final OnBookmarkClickListener listener) {
            super(itemView);

            bookmarkCardView = itemView.findViewById(R.id.bookmarkCardView);
            bookmarkNumberTextView = itemView.findViewById(R.id.bookmarkNumberTextView);
            bookmarkTimeTextView = itemView.findViewById(R.id.bookmarkTimeTextView);
            bookmarkDescriptionTextView = itemView.findViewById(R.id.bookMarkDescriptionTextView);
            bookmarkEditText = itemView.findViewById(R.id.bookmarkEditText);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null) {
                        listener.onBookmarkClicked(position);
                    }
                }
            });

            ImageButton deleteBookmarkButton = itemView.findViewById(R.id.deleteBookmarkButton);
            deleteBookmarkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("안내")
                            .setMessage("이 북마크를 제거하시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int position = getAdapterPosition();
                                    if (listener != null) {
                                        listener.onBookmarkDeleteClicked(position);
                                    }
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            final ImageButton editBookmarkButton = itemView.findViewById(R.id.editBookmarkButton);
            editBookmarkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isEditBookmarkActivated) {
                        isEditBookmarkActivated = false;
                        String editedName = bookmarkEditText.getText().toString();
                        bookmarkDescriptionTextView.setText(editedName);

                        bookmarkEditText.setVisibility(View.GONE);
                        bookmarkDescriptionTextView.setVisibility(View.VISIBLE);

                        int position = getAdapterPosition();
                        if (listener != null) {
                            listener.onBookmarkNameEdited(position, editedName);
                        }
                    } else {
                        isEditBookmarkActivated = true;
                        String name = bookmarkDescriptionTextView.getText().toString();
                        bookmarkEditText.setText(name);

                        bookmarkDescriptionTextView.setVisibility(View.GONE);
                        bookmarkEditText.setVisibility(View.VISIBLE);
                        bookmarkEditText.requestFocus();
                    }
                    editBookmarkButton.setActivated(isEditBookmarkActivated);
                }
            });

//            breakpointButton = itemView.findViewById(R.id.breakpointButton);
//            breakpointButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int position = getAdapterPosition();
//                    if (listener != null) {
//                        listener.onBookmarkBreakpointClicked(position);
//                    }
//
//                    processBPActivation();
//                }
//            });
//
//            repeatButton = itemView.findViewById(R.id.repeatButton);
//            repeatButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int position = getAdapterPosition();
//                    if (listener != null) {
//                        listener.onBookmarkRepeatClicked(position);
//                    }
//
//                    processRepeatActivation();
//                }
//            });
        }

//        private void processBPActivation() {
//            isBPActivated = !isBPActivated;
//            breakpointButton.setActivated(isBPActivated);
//        }
//
//        private void processRepeatActivation() {
//            isRepeatActivated = !isRepeatActivated;
//            repeatButton.setActivated(isRepeatActivated);
//        }

        public void setBookmarkContent(PlayVideoContent content) {
            // description, separator 이후에 북마크 나오기 때문에 -2
            // 1부터 인덱싱 하고 싶어서 + 1
            bookmarkNumberTextView.setText(String.valueOf(content.getAdapterIndex() - 2 + 1));
            bookmarkTimeTextView.setText(formattingBookmarkTime(content.getBookmarkTime() / 1000));
            bookmarkDescriptionTextView.setText(content.getBookmarkDescription());
        }

        private String formattingBookmarkTime(int time) {
            int hour = time / 3600;
            time %= 3600;
            int minute = time / 60;
            time %= 60;
            int second = time;
            return String.format("%d:%d:%d", hour, minute, second);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

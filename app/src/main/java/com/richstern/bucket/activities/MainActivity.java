package com.richstern.bucket.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxSeekBar;
import com.richstern.bucket.R;
import com.richstern.bucket.rx.OnlyNextObserver;
import com.richstern.bucket.util.Intents;
import com.richstern.bucket.util.ViewResizeHelper;
import com.richstern.bucket.util.ViewResizeHelper.AspectRatio;
import com.richstern.bucket.util.ViewResizeHelper.ResizeAnchor;
import com.squareup.picasso.Picasso;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

public class MainActivity extends RxAppCompatActivity {

    private static final int REQUEST_CODE_PICK_PHOTO = 0x00;

    @BindView(R.id.photo_frame) View photoFrame;
    @BindView(R.id.empty_photo) View emptyPhoto;
    @BindView(R.id.image) ImageView image;
    @BindView(R.id.red) SeekBar red;
    @BindView(R.id.green) SeekBar green;
    @BindView(R.id.blue) SeekBar blue;
    @BindView(R.id.color) View color;
    @BindView(R.id.red_level) View redLevel;
    @BindView(R.id.green_level) View greenLevel;
    @BindView(R.id.blue_level) View blueLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        ViewResizeHelper.resize(photoFrame, AspectRatio.SQUARE, ResizeAnchor.WIDTH);
        ViewResizeHelper.resize(redLevel, AspectRatio.SQUARE, ResizeAnchor.WIDTH);
        ViewResizeHelper.resize(greenLevel, AspectRatio.SQUARE, ResizeAnchor.WIDTH);
        ViewResizeHelper.resize(blueLevel, AspectRatio.SQUARE, ResizeAnchor.WIDTH);

        RxView.clicks(emptyPhoto)
            .compose(bindToLifecycle())
            .subscribe(OnlyNextObserver.forAction(__ -> {
                Intent intent = Intents.pickPhoto();
                startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
            }));

        Observable.combineLatest(
            RxSeekBar.changes(red),
            RxSeekBar.changes(green),
            RxSeekBar.changes(blue),
            this::toRGB)
            .compose(bindToLifecycle())
            .subscribe(OnlyNextObserver.forAction(color::setBackgroundColor));
    }

    private int toRGB(int red, int green, int blue) {
        return 0xFF000000 |
            ((red << 16) & 0x00FF0000) |
            ((green << 8) & 0x0000FF00) |
            (blue & 0x000000FF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_PICK_PHOTO:
                    if (data != null && data.getData() != null) {
                        photoSelected(data.getData());
                    }
                    break;
            }
        }
    }

    private void photoSelected(Uri data) {
        Picasso.with(this).load(data).fit().centerCrop().into(image);
        emptyPhoto.setVisibility(View.GONE);
        image.setVisibility(View.VISIBLE);
    }
}

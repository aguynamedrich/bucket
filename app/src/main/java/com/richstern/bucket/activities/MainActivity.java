package com.richstern.bucket.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
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

public class MainActivity extends RxAppCompatActivity {

    private static final int REQUEST_CODE_PICK_PHOTO = 0x00;

    @BindView(R.id.photo_frame) View photoFrame;
    @BindView(R.id.empty_photo) View emptyPhoto;
    @BindView(R.id.image) ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        ViewResizeHelper.resize(photoFrame, AspectRatio.SQUARE, ResizeAnchor.WIDTH);

        RxView.clicks(emptyPhoto)
            .compose(bindToLifecycle())
            .subscribe(OnlyNextObserver.forAction(__ -> {
                Intent intent = Intents.pickPhoto();
                startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
            }));

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

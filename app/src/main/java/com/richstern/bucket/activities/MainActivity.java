package com.richstern.bucket.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxSeekBar;
import com.richstern.bucket.R;
import com.richstern.bucket.image.Images;
import com.richstern.bucket.picasso.AbstractTarget;
import com.richstern.bucket.rx.OnlyNextObserver;
import com.richstern.bucket.util.Colors;
import com.richstern.bucket.util.Intents;
import com.richstern.bucket.util.ViewResizeHelper;
import com.richstern.bucket.util.ViewResizeHelper.AspectRatio;
import com.richstern.bucket.util.ViewResizeHelper.ResizeAnchor;
import com.squareup.picasso.Picasso;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

public class MainActivity extends RxAppCompatActivity {

    private static final int REQUEST_CODE_PICK_PHOTO = 0x00;

    @BindView(R.id.photo_frame) View photoFrame;
    @BindView(R.id.empty_photo) View emptyPhoto;
    @BindView(R.id.image_container) View imageContainer;
    @BindView(R.id.image) ImageView image;
    @BindView(R.id.red) SeekBar red;
    @BindView(R.id.green) SeekBar green;
    @BindView(R.id.blue) SeekBar blue;
    @BindView(R.id.color) View color;
    @BindView(R.id.red_level) View redLevel;
    @BindView(R.id.green_level) View greenLevel;
    @BindView(R.id.blue_level) View blueLevel;
    @BindView(R.id.threshold_text) TextView thresholdText;
    @BindView(R.id.threshold) SeekBar threshold;

    private int[][] pixelData;
    private int selectedColor;

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

        RxView.touches(image)
            .map(event -> new PointF(event.getX(), event.getY()))
            .distinctUntilChanged()
            .compose(bindToLifecycle())
            .subscribe(OnlyNextObserver.forAction(this::floodFill));

        Observable.combineLatest(
            RxSeekBar.changes(red),
            RxSeekBar.changes(green),
            RxSeekBar.changes(blue),
            Colors::toRGB)
            .doOnNext(selectedColor -> this.selectedColor = selectedColor)
            .compose(bindToLifecycle())
            .subscribe(OnlyNextObserver.forAction(color::setBackgroundColor));

        RxSeekBar.changes(threshold)
            .distinctUntilChanged()
            .compose(bindToLifecycle())
            .subscribe(OnlyNextObserver.forAction(value ->
                thresholdText.setText(String.format(Locale.US, "%d %%", value))));

    }

    private void floodFill(PointF point) {
        image.setClickable(false);
        int originX = (int) (point.x / image.getWidth() * pixelData.length);
        int originY = (int) (point.y / image.getHeight() * pixelData[0].length);
        Images.floodFill(pixelData, originX, originY, selectedColor, threshold.getProgress());
        int[] pixels = Images.flatten(pixelData);
        Bitmap bitmap = Bitmap.createBitmap(pixels, pixelData.length, pixelData[0].length, Bitmap.Config.ARGB_8888);
        image.setImageBitmap(bitmap);
        image.setClickable(true);
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
        Picasso.with(this)
            .load(data)
            .resize(400, 400) // arbitrary small square
            .centerCrop()
            .into(new AbstractTarget() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    emptyPhoto.setVisibility(View.GONE);
                    imageContainer.setVisibility(View.VISIBLE);
                    getBitmapData(bitmap);
                }
            });
    }

    private void getBitmapData(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        pixelData = Images.to2dArray(pixels, width, height);
        image.setImageBitmap(bitmap);
    }
}

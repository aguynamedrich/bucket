package com.richstern.bucket.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxSeekBar;
import com.richstern.bucket.R;
import com.richstern.bucket.image.Images;
import com.richstern.bucket.picasso.AbstractTarget;
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
import rx.Subscriber;

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
    @BindView(R.id.threshold_text) View thresholdText;
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
        ViewResizeHelper.resize(thresholdText, AspectRatio.SQUARE, ResizeAnchor.WIDTH);

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
            .subscribe(new Subscriber<PointF>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Log.d("Rich", e.getClass().getSimpleName() + "," + e.getMessage());
                }

                @Override
                public void onNext(PointF pointF) {
                    floodFill(pointF);
                }
            });

        Observable.combineLatest(
            RxSeekBar.changes(red),
            RxSeekBar.changes(green),
            RxSeekBar.changes(blue),
            this::toRGB)
            .doOnNext(selectedColor -> this.selectedColor = selectedColor)
            .compose(bindToLifecycle())
            .subscribe(OnlyNextObserver.forAction(color::setBackgroundColor));
    }

    private void floodFill(PointF point) {
        int originX = (int) (point.x / image.getWidth() * pixelData.length);
        int originY = (int) (point.y / image.getHeight() * pixelData[0].length);
        Images.floodFill(pixelData, originX, originY, selectedColor, threshold.getProgress());
        int[] pixels = Images.flatten(pixelData);
        Bitmap bitmap = Bitmap.createBitmap(pixels, pixelData.length, pixelData[0].length, Bitmap.Config.ARGB_8888);
        image.setImageBitmap(bitmap);
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
        Picasso.with(this)
            .load(data)
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
        int top, left, width, height;

        // Let's just make this a square for simplicity
        if (bitmap.getWidth() < bitmap.getHeight()) {
            left = 0;
            width = height = bitmap.getWidth();
            top = (bitmap.getHeight() - width) / 2;
        } else {
            top = 0;
            width = height = bitmap.getHeight();
            left = (bitmap.getWidth() - height) / 2;
        }

        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, left, top, width, height);

        // Get pixel data and fill our 2D structure
        int[] pixels = new int[width * height];
        squareBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        pixelData = Images.to2dArray(pixels, width, height);

        image.setImageBitmap(squareBitmap);
    }
}

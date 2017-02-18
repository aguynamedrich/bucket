package com.richstern.bucket.util;

import android.content.Intent;
import android.provider.MediaStore;

public class Intents {

    public static Intent pickPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        return intent;
    }
}

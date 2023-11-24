package com.jupiter.tusa.cache.template;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jupiter.tusa.cache.CacheStorage;
import com.jupiter.tusa.cache.lru.MemCacheBitmap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CacheBitmap extends CacheStorageTemplate<Bitmap>{
    public CacheBitmap(CacheStorage cacheStorage) {
        super(new MemCacheBitmap(cacheStorage.getMaxMemoryKilobytes()), cacheStorage);
    }

    @Override
    protected Bitmap fromInputStream(InputStream inputStream) {
        return BitmapFactory.decodeStream(inputStream);
    }

    @Override
    protected void write(OutputStream outputStream, Bitmap body) throws IOException {
        body.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
    }
}

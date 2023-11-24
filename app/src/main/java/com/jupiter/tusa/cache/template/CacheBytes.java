package com.jupiter.tusa.cache.template;

import android.os.Build;
import com.jupiter.tusa.cache.CacheStorage;
import com.jupiter.tusa.cache.lru.MemCacheBytes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CacheBytes extends CacheStorageTemplate<byte[]> {
    public CacheBytes(CacheStorage cacheStorage) {
        super(new MemCacheBytes(cacheStorage.getMaxMemoryKilobytes()), cacheStorage);
    }

    @Override
    protected byte[] fromInputStream(InputStream inputStream) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return inputStream.readAllBytes();
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                return byteArrayOutputStream.toByteArray();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    protected void write(OutputStream outputStream, byte[] body) throws IOException {
        outputStream.write(body);
    }
}

package com.welty.novello.eval;

import com.orbanova.common.feed.Feed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Feed that reads Objects from a file.
 * <p/>
 * Objects are returned until the underlying ObjectInputStream reaches its end or the file access
 * throws an IOException; once this happens the underlying inputStream is closed.
 * <p/>
 * next() will never throw an IOException - it can't, because it's not in the interface.
 */
public class ObjectFeed<T> extends Feed<T> implements AutoCloseable {
    private final @NotNull DataInputStream in;
    private final @NotNull Deserializer<T> deserializer;

    public ObjectFeed(Path path, @NotNull Deserializer<T> deserializer) throws IOException {
        this.deserializer = deserializer;
        in = new DataInputStream(Files.newInputStream(path));
    }

    @Nullable @Override public T next() {
        try {
            return deserializer.read(in);
        } catch (IOException e) {
            close();
            return null;
        }
    }

    @Override public void close() {
        try {
            in.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public static interface Deserializer<T> {
        /**
         * Read in an object from a DataInputStream.
         * <p/>
         * If an IOException occurs when reading from the DataInputStream, this method
         * throws that exception rather than handling it.
         * <p/>
         * This function can return either a new object or a previously existing object, for example a cached object.
         *
         * @param in input stream to read from.
         * @return an object
         */
        T read(DataInputStream in) throws IOException;
    }
}

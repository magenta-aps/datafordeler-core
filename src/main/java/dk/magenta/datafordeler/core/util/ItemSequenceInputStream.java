package dk.magenta.datafordeler.core.util;

import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ItemSequenceInputStream<T> extends SequenceInputStream {

    // For some weird reason we get an exception when reading from a flattened
    // ItemSequenceInputStream that only contains one ItemInputStream.
    // So store that one stream and return it when flattening
    private ItemInputStream<T> singleStream = null;

    public ItemSequenceInputStream(Collection<ItemInputStream<T>> streams) {
        super(Collections.enumeration(streams));
        if (streams.size() == 1) {
            this.singleStream = streams.iterator().next();
        }
    }

    public static <T> ItemSequenceInputStream<T> from(Collection<ItemInputStream<? extends T>> streams) {
        ArrayList<ItemInputStream<T>> list = new ArrayList<>();
        for (ItemInputStream<? extends T> stream : streams) {
            list.add((ItemInputStream<T>) stream);
        }
        return new ItemSequenceInputStream(list);
    }

    public ItemInputStream<T> flatten() throws IOException {
        if (this.singleStream != null) {
            return this.singleStream;
        }
        return new ItemInputStream<>(this);
    }

}

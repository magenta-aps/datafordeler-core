package dk.magenta.datafordeler.core.util;

import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ItemSequenceInputStream<T> extends SequenceInputStream {

    public ItemSequenceInputStream(Collection<ItemInputStream<T>> streams) {
        super(Collections.enumeration(streams));
    }

    public static <T> ItemSequenceInputStream<T> from(Collection<ItemInputStream<? extends T>> streams) {
        ArrayList<ItemInputStream<T>> list = new ArrayList<>();
        for (ItemInputStream<? extends T> stream : streams) {
            list.add((ItemInputStream<T>) stream);
        }
        return new ItemSequenceInputStream(list);
    }

    public ItemInputStream<T> flatten() throws IOException {
        return new ItemInputStream<>(this);
    }

}

package org.happypeng.roomrecyclerviewexample;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MyEntry {
    @PrimaryKey int key;
    String value;

    public MyEntry() {}

    public MyEntry(int aKey, String aValue) {
        key = aKey;
        value = aValue;
    }

    public static class MyDiffUtil extends DiffUtil.ItemCallback<MyEntry> {
        @Override
        public boolean areItemsTheSame(@NonNull MyEntry oldItem, @NonNull MyEntry newItem) {
            return oldItem.key == newItem.key;
        }

        @Override
        public boolean areContentsTheSame(@NonNull MyEntry oldItem, @NonNull MyEntry newItem) {
            if (oldItem.value == null && newItem.value != null) {
                return false;
            }

            if (newItem.value == null && oldItem.value != null) {
                return false;
            }

            // If one of them is null, both of them are at this point
            if (newItem.value == null) {
                return true;
            }

            return newItem.value.equals(oldItem.value);
        }
    }
}

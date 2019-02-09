package org.happypeng.roomrecyclerviewexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class MyPagedListAdapter extends PagedListAdapter<MyEntry, MyPagedListAdapter.MyViewHolder> {
    public interface ClickListener {
        void onClick(View aView, MyEntry aEntry);
    }

    private ClickListener m_clickListener;

    public MyPagedListAdapter(ClickListener aListener) {
        super(new MyEntry.MyDiffUtil());

        m_clickListener = aListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.card_view, parent, false);
        return new MyViewHolder(view, m_clickListener);
    }

    // Position in the query is key - 1 (because key starts at 1, position at 0)
    public int getItemKey(int aPosition) {
        MyEntry entry = getItem(aPosition);

        return (entry == null) ? 0 : (entry.key - 1);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindTo(getItem(position));
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView m_idTV;
        private TextView m_contentTV;
        private Button m_toggleB;

        private ClickListener m_clickListener;

        private MyViewHolder(@NonNull View itemView, ClickListener aListener) {
            super(itemView);

            m_idTV = itemView.findViewById(R.id.dbIdText);
            m_contentTV = itemView.findViewById(R.id.dbContentText);
            m_toggleB = itemView.findViewById(R.id.dbToggle);

            m_clickListener = aListener;
        }

        private void bindTo(final MyEntry aEntry) {
            if (aEntry != null) {
                m_idTV.setText(Integer.toString(aEntry.key));
                m_contentTV.setText(aEntry.value);
            } else {
                m_idTV.setText("");
                m_contentTV.setText("");
            }

            m_toggleB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_clickListener.onClick(v, aEntry);
                }
            });
        }
    }
}

package org.happypeng.roomrecyclerviewexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private MyDatabase mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Shouldn't do that on the main thread, just for testing
        mDB = Room.databaseBuilder(this, MyDatabase.class,
                MyDatabase.DATABASE_NAME).build();

        Button populate_b = (Button) findViewById(R.id.dbPopulate);
        Button delete_b = (Button) findViewById(R.id.dbDelete);

        RecyclerView recycler_v = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager layout_m = new LinearLayoutManager(this);
        layout_m.setOrientation(RecyclerView.VERTICAL);
        recycler_v.setLayoutManager(layout_m);

        final MyPagedListAdapter adapter = new MyPagedListAdapter
                (new MyPagedListAdapter.ClickListener() {
                    @Override
                    public void onClick(View aView, final MyEntry aEntry) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                if (aEntry.value.equals("INITIAL")) {
                                    mDB.getMyDao().update(new MyEntry(aEntry.key, "FINAL"));
                                } else {
                                    mDB.getMyDao().update(new MyEntry(aEntry.key, "INITIAL"));
                                }

                                return null;
                            }
                        }.execute();
                    }
                });

        recycler_v.setAdapter(adapter);

        populate_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        mDB.beginTransaction();

                        for (int i = 0; i < 1000; i++) {
                            mDB.getMyDao().insertEntry(new MyEntry(i+1, "INITIAL"));
                        }

                        mDB.setTransactionSuccessful();
                        mDB.endTransaction();

                        return null;
                    }
                }.execute();
            }
        });

        delete_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        // Not so efficient maybe, but simple
                        mDB.getMyDao().deleteMany(mDB.getMyDao().getAll());

                        return null;
                    }
                }.execute();
            }
        });

        PagedList.Config plConfig =
                new PagedList.Config.Builder().setEnablePlaceholders(false)
                .setPrefetchDistance(30)
                .setPageSize(50).build();

        new LivePagedListBuilder<>
                (mDB.getMyDao().getAllPaged(), plConfig)
                .build()
                .observe(this, new Observer<PagedList<MyEntry>>() {
                    @Override
                    public void onChanged(PagedList<MyEntry> myList) {
                        adapter.submitList(myList);
                    }
                });
    }
}

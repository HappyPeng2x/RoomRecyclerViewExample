package org.happypeng.roomrecyclerviewexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.happypeng.roomrecyclerviewexample.paging.ViewableLivePagedListBuilder;

public class MainActivity extends AppCompatActivity {
    private MyDatabase mDB;
    private boolean mViewableModeActivated;
    private LinearLayoutManager mLayoutManager;
    private MyPagedListAdapter mAdapter;
    private ViewableLivePagedListBuilder<Integer, MyEntry> mViewablePlb;
    private PagedList.Config mPlConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Shouldn't do that on the main thread, just for testing
        mDB = Room.databaseBuilder(this, MyDatabase.class,
                MyDatabase.DATABASE_NAME).build();

        Button populate_b = (Button) findViewById(R.id.dbPopulate);
        Button delete_b = (Button) findViewById(R.id.dbDelete);
        Button activate_b = (Button) findViewById(R.id.activateViewableMode);

        RecyclerView recycler_v = (RecyclerView) findViewById(R.id.recyclerView);

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recycler_v.setLayoutManager(mLayoutManager);

         mAdapter = new MyPagedListAdapter
                (new MyPagedListAdapter.ClickListener() {
                    @Override
                    public void onClick(View aView, final MyEntry aEntry) {
                        Log.d("TEST_LIST_BUILDER", "Clicked entry: " + aEntry.key);

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

        recycler_v.setAdapter(mAdapter);

        populate_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        mDB.beginTransaction();

                        for (int i = 0; i < 300; i++) {
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

        mPlConfig =
                new PagedList.Config.Builder().setEnablePlaceholders(false)
                .setPrefetchDistance(10).setInitialLoadSizeHint(20)
                .setPageSize(20).build();

        mViewablePlb =
                new ViewableLivePagedListBuilder<>
                (mDB.getMyDao().getAllPaged(), mPlConfig);

        mViewablePlb.setBoundaryCallback(new PagedList.BoundaryCallback<MyEntry>() {
            @Override
            public void onItemAtEndLoaded(@NonNull MyEntry itemAtEnd) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        int maxKey = mDB.getMyDao().getMax();

                        mDB.beginTransaction();

                        for (int i = 0; i < 100; i++) {
                            mDB.getMyDao().insertEntry(new MyEntry(maxKey+i+1, "INITIAL"));
                        }

                        mDB.setTransactionSuccessful();
                        mDB.endTransaction();

                        return null;
                    }
                }.execute();
            }
        });

        mViewableModeActivated = false;

        Log.d("TEST_LIST_BUILDER", "Viewable mode: " + mViewableModeActivated);

        activate_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewableModeActivated = !mViewableModeActivated;

                Log.d("TEST_LIST_BUILDER", "Viewable mode: " + mViewableModeActivated);

                setVisibleRange();
            }
        });

        recycler_v.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                setVisibleRange();
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                setVisibleRange();
            }
        });

        mViewablePlb.build()
                .observe(this, new Observer<PagedList<MyEntry>>() {
                    @Override
                    public void onChanged(PagedList<MyEntry> myList) {
                        mAdapter.submitList(myList);
                    }
                });
    }

    private void setVisibleRange() {
        int firstVisiblePosition = mLayoutManager.findFirstVisibleItemPosition();
        int lastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();

        Log.d("TEST_SCROLL_POSITION", "Visible position range: " + firstVisiblePosition + " for " + (lastVisiblePosition - firstVisiblePosition) + " items");

        if (mViewableModeActivated) {
            int firstKeyToLoad = mAdapter.getItemKey(Math.max(0, firstVisiblePosition - mPlConfig.pageSize));
            int loadMinSize = 2 * mPlConfig.pageSize + lastVisiblePosition - firstVisiblePosition;

            Log.d("TEST_SCROLL_POSITION", "Target loading key range: " + firstKeyToLoad + " for " + loadMinSize + " items");

            mViewablePlb.setLoadRange(firstKeyToLoad, loadMinSize);
        } else {
            mViewablePlb.setLoadRange(null, 0);
        }
    }
}

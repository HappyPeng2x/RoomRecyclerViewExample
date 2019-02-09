/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.happypeng.roomrecyclerviewexample.paging;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.ComputableLiveData;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.PagedList;

import java.util.concurrent.Executor;

/**
 * Builder for {@code LiveData<PagedList>}, given a {@link DataSource.Factory} and a
 * {@link PagedList.Config}.
 * <p>
 * The required parameters are in the constructor, so you can simply construct and build, or
 * optionally enable extra features (such as initial load key, or BoundaryCallback).
 *
 * @param <Key> Type of input valued used to load data from the DataSource. Must be integer if
 *             you're using PositionalDataSource.
 * @param <Value> Item type being presented.
 */
public final class ViewableLivePagedListBuilder<Key, Value> {
    private static class KeyHolder<Key> {
        private Key mKey;

        KeyHolder(Key key) {
            mKey = key;
        }

        Key getKey() {
            return mKey;
        }

        void setKey(Key key) {
            mKey = key;
        }
    }

    private Key mInitialLoadKey;
    private KeyHolder<Key> mFirstKeyToLoadHolder;
    private KeyHolder<Integer> mLoadMinSizeHolder;
    private PagedList.Config mConfig;
    private DataSource.Factory<Key, Value> mDataSourceFactory;
    private PagedList.BoundaryCallback mBoundaryCallback;
    @SuppressLint("RestrictedApi")
    private Executor mFetchExecutor = ArchTaskExecutor.getIOThreadExecutor();

    /**
     * Creates a LivePagedListBuilder with required parameters.
     *
     * @param dataSourceFactory DataSource factory providing DataSource generations.
     * @param config Paging configuration.
     */
    public ViewableLivePagedListBuilder(@NonNull DataSource.Factory<Key, Value> dataSourceFactory,
                                @NonNull PagedList.Config config) {
        //noinspection ConstantConditions
        if (config == null) {
            throw new IllegalArgumentException("PagedList.Config must be provided");
        }
        //noinspection ConstantConditions
        if (dataSourceFactory == null) {
            throw new IllegalArgumentException("DataSource.Factory must be provided");
        }

        mDataSourceFactory = dataSourceFactory;
        mConfig = config;

        mFirstKeyToLoadHolder = new KeyHolder<>(null);
        mLoadMinSizeHolder = new KeyHolder<>(null);
    }

    /**
     * Creates a LivePagedListBuilder with required parameters.
     * <p>
     * This method is a convenience for:
     * <pre>
     * LivePagedListBuilder(dataSourceFactory,
     *         new PagedList.Config.Builder().setPageSize(pageSize).build())
     * </pre>
     *
     * @param dataSourceFactory DataSource.Factory providing DataSource generations.
     * @param pageSize Size of pages to load.
     */
    public ViewableLivePagedListBuilder(@NonNull DataSource.Factory<Key, Value> dataSourceFactory,
                                int pageSize) {
        this(dataSourceFactory, new PagedList.Config.Builder().setPageSize(pageSize).build());
    }

    /**
     * First loading key passed to the first PagedList/DataSource.
     * <p>
     * When a new PagedList/DataSource pair is created after the first, it acquires a load key from
     * the previous generation so that data is loaded around the position already being observed.
     *
     * @param key Initial load key passed to the first PagedList/DataSource.
     * @return this
     */
    @NonNull
    public ViewableLivePagedListBuilder<Key, Value> setInitialLoadKey(@Nullable Key key) {
        mInitialLoadKey = key;
        return this;
    }

    /**
     * Sets a {@link PagedList.BoundaryCallback} on each PagedList created, typically used to load
     * additional data from network when paging from local storage.
     * <p>
     * Pass a BoundaryCallback to listen to when the PagedList runs out of data to load. If this
     * method is not called, or {@code null} is passed, you will not be notified when each
     * DataSource runs out of data to provide to its PagedList.
     * <p>
     * If you are paging from a DataSource.Factory backed by local storage, you can set a
     * BoundaryCallback to know when there is no more information to page from local storage.
     * This is useful to page from the network when local storage is a cache of network data.
     * <p>
     * Note that when using a BoundaryCallback with a {@code LiveData<PagedList>}, method calls
     * on the callback may be dispatched multiple times - one for each PagedList/DataSource
     * pair. If loading network data from a BoundaryCallback, you should prevent multiple
     * dispatches of the same method from triggering multiple simultaneous network loads.
     *
     * @param boundaryCallback The boundary callback for listening to PagedList load state.
     * @return this
     */
    @SuppressWarnings("unused")
    @NonNull
    public ViewableLivePagedListBuilder<Key, Value> setBoundaryCallback(
            @Nullable PagedList.BoundaryCallback<Value> boundaryCallback) {
        mBoundaryCallback = boundaryCallback;
        return this;
    }

    /**
     * Sets executor used for background fetching of PagedLists, and the pages within.
     * <p>
     * If not set, defaults to the Arch components I/O thread pool.
     *
     * @param fetchExecutor Executor for fetching data from DataSources.
     * @return this
     */
    @SuppressWarnings("unused")
    @NonNull
    public ViewableLivePagedListBuilder<Key, Value> setFetchExecutor(
            @NonNull Executor fetchExecutor) {
        mFetchExecutor = fetchExecutor;
        return this;
    }

    /**
     * Constructs the {@code LiveData<PagedList>}.
     * <p>
     * No work (such as loading) is done immediately, the creation of the first PagedList is is
     * deferred until the LiveData is observed.
     *
     * @return The LiveData of PagedLists
     */
    @NonNull
    @SuppressLint("RestrictedApi")
    public LiveData<PagedList<Value>> build() {
        return create(mInitialLoadKey, mFirstKeyToLoadHolder, mLoadMinSizeHolder, mConfig, mBoundaryCallback, mDataSourceFactory,
                ArchTaskExecutor.getMainThreadExecutor(), mFetchExecutor);
    }

    public ViewableLivePagedListBuilder<Key, Value>
    setLoadRange(Key firstKeyToLoad, int loadMinSize) {
        mFirstKeyToLoadHolder.setKey(firstKeyToLoad);
        mLoadMinSizeHolder.setKey(loadMinSize);

        return this;
    }

    @AnyThread
    @NonNull
    @SuppressLint("RestrictedApi")
    private static <Key, Value> LiveData<PagedList<Value>> create(
            @Nullable final Key initialLoadKey,
            @NonNull final KeyHolder<Key> firstVisibleKey,
            @NonNull final KeyHolder<Integer> loadSizeHint,
            @NonNull final PagedList.Config config,
            @Nullable final PagedList.BoundaryCallback boundaryCallback,
            @NonNull final DataSource.Factory<Key, Value> dataSourceFactory,
            @NonNull final Executor notifyExecutor,
            @NonNull final Executor fetchExecutor) {
        return new ComputableLiveData<PagedList<Value>>(fetchExecutor) {
            @Nullable
            private PagedList<Value> mList;
            @Nullable
            private DataSource<Key, Value> mDataSource;

            private final DataSource.InvalidatedCallback mCallback =
                    new DataSource.InvalidatedCallback() {
                        @Override
                        public void onInvalidated() {
                            invalidate();
                        }
                    };

            @SuppressWarnings("unchecked") // for casting getLastKey to Key
            @Override
            protected PagedList<Value> compute() {
                @Nullable Key initializeKey = initialLoadKey;
                @Nullable Key firstVisibleKeyValue = firstVisibleKey.getKey();
                @Nullable Integer loadSizeHintValue = loadSizeHint.getKey();
                PagedList.Config localConfig = config;

                if (firstVisibleKeyValue != null) {
                    initializeKey = firstVisibleKeyValue;
                } else if (mList != null) {
                    initializeKey = (Key) mList.getLastKey();
                }

                if (loadSizeHintValue != null && loadSizeHintValue > config.initialLoadSizeHint) {
                    loadSizeHintValue = config.pageSize * (((loadSizeHintValue % config.pageSize == 0) ? 0 : 1) + (loadSizeHintValue / config.pageSize));

                    localConfig =
                            new PagedList.Config.Builder()
                                    .setEnablePlaceholders(config.enablePlaceholders)
                                    .setPrefetchDistance(config.prefetchDistance)
                                    .setMaxSize(config.maxSize)
                                    .setInitialLoadSizeHint(loadSizeHintValue)
                                    .setPageSize(config.pageSize).build();
                }

                do {
                    if (mDataSource != null) {
                        mDataSource.removeInvalidatedCallback(mCallback);
                    }

                    mDataSource = dataSourceFactory.create();
                    mDataSource.addInvalidatedCallback(mCallback);

                    Log.d("TEST_LIST_BUILDER", "Initialize key: " + initializeKey + ", range size = " + localConfig.initialLoadSizeHint);

                    mList = new PagedList.Builder<>(mDataSource, localConfig)
                            .setNotifyExecutor(notifyExecutor)
                            .setFetchExecutor(fetchExecutor)
                            .setBoundaryCallback(boundaryCallback)
                            .setInitialKey(initializeKey)
                            .build();
                } while (mList.isDetached());
                return mList;
            }
        }.getLiveData();
    }
}
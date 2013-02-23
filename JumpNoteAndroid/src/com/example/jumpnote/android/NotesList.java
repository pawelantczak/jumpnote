/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jumpnote.android;

import android.accounts.Account;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Displays a list of notes. Will display notes from the {@link Uri} provided in
 * the intent if there is one, otherwise defaults to displaying the contents of
 * the {@link JumpNoteProvider}
 */
public class NotesList extends ListActivity implements SyncStatusObserver {
    static final String TAG = Config.makeLogTag(NotesList.class);

    // Menu item ids
    public static final int MENU_DELETE = Menu.FIRST;
    public static final int MENU_SYNC_NOW = Menu.FIRST + 1;
    public static final int MENU_INSERT = Menu.FIRST + 2;
    public static final int MENU_ACCOUNT_LIST = Menu.FIRST + 3;

    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
        JumpNoteContract.Notes._ID, // 0
        JumpNoteContract.Notes.TITLE, // 1
        JumpNoteContract.Notes.BODY, // 2
    };

    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;

    private Account mAccount = null;

    private Object mSyncObserverHandle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.notes_list);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.notes_list_title);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            throw new IllegalArgumentException("Invalid JumpNote content URI");
        }

        String accountName = JumpNoteContract.getAccountNameFromUri(intent.getData());
        if (accountName != null && !accountName.equals(JumpNoteContract.EMPTY_ACCOUNT_NAME)) {
            mAccount = new Account(accountName, SyncAdapter.GOOGLE_ACCOUNT_TYPE);
            setTitle(mAccount.name);
        } else {
            mAccount = null;
            setTitle(getString(R.string.local_notes));
        }

        // Perform a managed query. The Activity will handle closing and
        // requerying the cursor when needed.
        Cursor cursor = managedQuery(getIntent().getData(), PROJECTION,
                JumpNoteContract.Notes.PENDING_DELETE + " = 0", null,
                JumpNoteContract.Notes.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.notes_list_item,
                cursor, new String[] {
                    JumpNoteContract.Notes.TITLE,
                    JumpNoteContract.Notes.BODY
                }, new int[] {
                    android.R.id.text1,
                    android.R.id.text2
                });
        setListAdapter(adapter);

        findViewById(R.id.home_button).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showAccounts();
            }
        });

        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        ((TextView) findViewById(android.R.id.title)).setText(title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSyncStatus();
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(
                mask, this);
    }

    @Override
    protected void onPause() {
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (mAccount != null) {
            menu.add(0, MENU_SYNC_NOW, 0, R.string.menu_sync_now).setShortcut('1', 's').setIcon(
                    R.drawable.ic_menu_refresh);
        }
        menu.add(0, MENU_INSERT, 0, R.string.menu_insert).setShortcut('3', 'a').setIcon(
                R.drawable.ic_menu_compose);
        menu.add(0, MENU_ACCOUNT_LIST, 0, R.string.menu_account_list).setIcon(
                R.drawable.ic_menu_account_list);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SYNC_NOW:
                doSync();
                return true;

            case MENU_INSERT:
                // Launch activity to insert a new item
                startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
                return true;

            case MENU_ACCOUNT_LIST:
                showAccounts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        Uri uri = ContentUris.withAppendedId(getIntent().getData(), info.id);

        // Build menu... always starts with the EDIT action...
        Intent[] specifics = new Intent[1];
        specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
        MenuItem[] items = new MenuItem[1];

        // ... is followed by whatever other actions are available...
        Intent intent = new Intent(null, uri);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null, specifics, intent, 0, items);

        // Add a menu item to delete the note
        menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
            case MENU_DELETE: {
                // Delete the note that the context menu is for
                Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
                ContentValues values = new ContentValues();
                values.put(JumpNoteContract.Notes.MODIFIED_DATE, System.currentTimeMillis());
                values.put(JumpNoteContract.Notes.PENDING_DELETE, true);
                getContentResolver().update(noteUri, values, null, null);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return a note selected by
            // the user. The have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }

    /**
     * Called when observed sync statuses change.
     */
    public void onStatusChanged(int which) {
        updateSyncStatus();
    }

    private void updateSyncStatus() {
        if (!Config.ENABLE_SYNC_UI)
            return;

        if (mAccount != null) {
            final int syncFlags;
            if (ContentResolver.isSyncPending(mAccount, JumpNoteContract.AUTHORITY))
                syncFlags = AccountList.FLAG_SYNC_PENDING;
            else if (ContentResolver.isSyncActive(mAccount, JumpNoteContract.AUTHORITY))
                syncFlags = AccountList.FLAG_SYNC_ACTIVE;
            else
                syncFlags = 0;

            runOnUiThread(new Runnable() {
                public void run() {
                    ImageView syncIndicator = (ImageView) findViewById(R.id.sync_indicator);
                    if ((syncFlags & AccountList.FLAG_SYNC_PENDING) != 0) {
                        syncIndicator.setVisibility(View.VISIBLE);
                        syncIndicator.setImageResource(R.drawable.ic_title_sync_anim0);
                    } else if ((syncFlags & AccountList.FLAG_SYNC_ACTIVE) != 0) {
                        final AnimationDrawable syncingDrawable = (AnimationDrawable)
                                getResources().getDrawable(R.drawable.ic_title_sync_anim);
                        syncIndicator.setVisibility(View.VISIBLE);
                        syncIndicator.setImageDrawable(syncingDrawable);
                        syncIndicator.post(new Runnable() {
                            public void run() {
                                syncingDrawable.start();
                            }
                        });
                    } else {
                        syncIndicator.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void showAccounts() {
        Intent i = new Intent(this, AccountList.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void doSync() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(mAccount, JumpNoteContract.AUTHORITY, extras);
    }
}

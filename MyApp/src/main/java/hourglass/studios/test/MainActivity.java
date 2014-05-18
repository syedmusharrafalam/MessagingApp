package hourglass.studios.test;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import static hourglass.studios.test.R.layout.activity_main;

public class MainActivity extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private Class textMessage, contactMessages;

    private static final int URL_LOADER = 0;


    //List of array strings which will serve as list items
    private ArrayList<ThreadItem> listThreadItems = new ArrayList<ThreadItem>();

    //Defining string adapter which will handle data of listview
    private ArrayAdapter<ThreadItem> threadAdapter;

    private Cursor threadCursor;

    //Keeps track of list items' thread number, to pass to contact's messages
    private ArrayList<String> listThreadNumb = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_main);

        //layout int value not used
        threadAdapter = new ThreadListAdapter(this, 0, listThreadItems);
        setListAdapter(threadAdapter);

        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    private void createConversations() {

        String[] uriSms = new String[]{
                "content://sms/inbox",
                "content://sms/sent",
                "content://sms/drafts",
                "content://sms/outbox",
                "content://sms/failed"};

        if (!listThreadItems.isEmpty()) {
            listThreadItems.clear();
            listThreadNumb.clear();
        }


        if (threadCursor != null && threadCursor.getCount() > 0) {
            threadCursor.moveToLast();
            do {
                String smsBody = threadCursor.getString(threadCursor.getColumnIndexOrThrow("snippet"));
                String thread = threadCursor.getString(threadCursor.getColumnIndexOrThrow("thread_id"));
                String msgCount = threadCursor.getString(threadCursor.getColumnIndexOrThrow("msg_count"));
                String number = "";
                String name = "";

                listThreadNumb.add(thread);

                int counter = 0;
                while (number.equals("")) {
                    number = getPhoneNumber(uriSms[counter], thread);
                    counter++;
                }
                // arranjar para suportar varios numeros
                if (!number.equals(""))
                    name = getContactName(number);

                listThreadItems.add(new ThreadItem(name, smsBody, msgCount));
            } while (threadCursor.moveToPrevious());

            threadCursor.close();
        }

        threadAdapter.notifyDataSetChanged();
    }

    private String getContactName(String number) {
        String name;
        Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        if (uri_cont != null) {
            Cursor cs = getContentResolver().query(uri_cont, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, ContactsContract.PhoneLookup.NUMBER + "='" + number + "'", null, null);

            if (cs != null && cs.moveToFirst()) {
                name = cs.getString(cs.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                cs.close();
            } else
                name = number;
        } else
            name = number;
        return name;
    }

    private String getPhoneNumber(String uriString, String thread) {
        Uri uri = Uri.parse(uriString);
        String where = "thread_id=" + thread;
        Cursor cursorPhone = getContentResolver().query(uri, null, where, null, null);
        String phone = "";

        if (cursorPhone != null && cursorPhone.moveToFirst()) {
            phone = cursorPhone.getString(cursorPhone.getColumnIndexOrThrow("address"));
            cursorPhone.close();
        }
        return phone;
    }


    public void buttonSmsOnClick(View v) {


        try {
            textMessage = Class.forName("hourglass.studios.test.Message");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Intent sms = new Intent(MainActivity.this, textMessage);
        startActivity(sms);


    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        try {
            contactMessages = Class.forName("hourglass.studios.test.ContactMessages");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Intent contactSms = new Intent(this, contactMessages);
        contactSms.putExtra("thread", listThreadNumb.get(position));
        startActivity(contactSms);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent startSettings = new Intent("hourglass.studios.test.SETTINGS");
                startActivity(startSettings);
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        getLoaderManager().destroyLoader(URL_LOADER);
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle)
    {
    /*
     * Takes action based on the ID of the Loader that's being created
     */

        String strUriCon = "content://sms/conversations";
        Uri uriSmsThreads = Uri.parse(strUriCon);

        switch (loaderID) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getBaseContext(),   // Parent activity context
                        uriSmsThreads,        // Table to query
                        null,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        "date"             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        threadCursor = data;

        createConversations();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

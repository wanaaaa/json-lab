package generalassembly.yuliyakaleda.solution_code;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

  private static final String URL_CEREAL =
      "https://api.usa.gov/jobs/search.json?query=nursing+jobs";


  private ListView mListView;
  private List<String> mItems;
  private ArrayAdapter<String> mAdapter;

  private Button mCerealButton;

  private DownloadTask mCerealTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mListView = (ListView) findViewById(R.id.list);
    mItems = new ArrayList<>(); // initialize as an empty list - will be populated by AsyncTasks
    mAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, mItems);
    mListView.setAdapter(mAdapter);

    mCerealButton = (Button) findViewById(R.id.cereal);
    mCerealButton.setOnClickListener(this);

  }

  @Override
  public void onClick(View v) {
    ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      switch (v.getId()) {
        case R.id.cereal:
          //There should be just one AsyncTask running to prevent memory leaks. Thus all other
          // tasks should be cancelled before starting a new one.

          mCerealTask = new DownloadTask();
          mCerealTask.execute(URL_CEREAL);
          break;
      }
    } else {
      Toast.makeText(this, R.string.network_connection_check, Toast.LENGTH_LONG).show();
    }
  }

  // Given a URL, establishes an HttpUrlConnection and retrieves the web page content as a
  // InputStream, which it returns as a string.
  private List<String> downloadUrl(String myUrl) throws IOException, JSONException {
    InputStream is = null;

    try {
      URL url = new URL(myUrl);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setDoInput(true);
      // Starts the query
      conn.connect();
      is = conn.getInputStream();

      // Convert the InputStream into a string
      String contentAsString = readIt(is);
      return parseJson(contentAsString);

      // Makes sure that the InputStream is closed after the app is
      // finished using it.
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

  private List<String> parseJson(String contentAsString) throws JSONException {
    List<String> items = new ArrayList<>();

    JSONArray root = new JSONArray(contentAsString);

    //forloop
    for(int i = 0; i < root.length();i++) {
      JSONObject object = root.getJSONObject(i);
      String organization = object.getString("organization_name");
      items.add(organization);
    }

    //endforloop

    //JSONObject root = new JSONObject(contentAsString);
    //JSONArray array = root.getJSONArray();
//    for (int i = 0; i < array.length(); i++)
//    {
//      JSONObject item = array.getJSONObject(i);
//      String name = item.getString("name");
//      items.add(name);
//    }
    return items;
  }

  public String readIt(InputStream stream) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
    String read;

    while((read = br.readLine()) != null) {
      sb.append(read);
    }
    return sb.toString();
  }

  private class DownloadTask extends AsyncTask<String, Void, List<String>> {
    @Override
    protected List<String> doInBackground(String... urls) {
      List<String> items = new ArrayList<>();

      try {
        items = downloadUrl(urls[0]);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (JSONException e) {
        e.printStackTrace();
      }
      return items;
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(List<String> items) {
      mItems.clear();
      mItems.addAll(items);
      mAdapter.notifyDataSetChanged();
    }
  }
}

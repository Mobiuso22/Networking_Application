package com.tutorialsbuzz.navigationdrawer;




import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetDataFragment extends DialogFragment {

    // declare the dialog as a member field of your activity
    ProgressDialog mProgressDialog;
    EditText txtURL;

    public GetDataFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_get_data, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


        txtURL = (EditText) view.findViewById(R.id.editText);
        txtURL.setText("https://s3.ap-south-1.amazonaws.com/training.mobiuso.com/contents.json");
        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Downloading JSON File from URL");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        Button btnSubmit = (Button)view.findViewById(R.id.button);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final DownloadFileFromURL downloadTask = new DownloadFileFromURL(getActivity());
                downloadTask.execute(txtURL.getText().toString());

                File file = new File(getActivity().getFilesDir(), "download.json");
                InputStream is = null;
                String mediaURL = null;
                String htmlURL = null;
                try {
                    is = new FileInputStream(file);
                    int size = 0;
                    size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    String json = new String(buffer, "UTF-8");
                    JSONObject jsonObject = new JSONObject(json);
                    mediaURL = jsonObject.getString("Media_URL");
                    htmlURL = jsonObject.getString("HTML_URL");


                } catch (Exception e) {

                }

                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadTask.cancel(true);
                    }
                });
            }

        });

    };
        class DownloadFileFromURL extends AsyncTask<String, Integer, String> {

            private Context context;
         //   private PowerManager.WakeLock mWakeLock;

            public DownloadFileFromURL(Context context) {
                this.context = context;
            }

            @Override
            protected String doInBackground(String... sUrl) {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(sUrl[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();


                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        return "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage();
                    }


                    int fileLength = connection.getContentLength();


                    input = connection.getInputStream();

                    System.out.println(getActivity().getFilesDir()+"\n");
                    File file = new File(getActivity().getFilesDir(), "download.json");

                    output = new FileOutputStream(file);
                    byte data[] = new byte[1024];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {

                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        total += count;

                        if (fileLength > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }


                } catch (Exception e) {
                    return e.toString();
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    } catch (IOException ignored) {
                    }

                    if (connection != null)
                        connection.disconnect();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

                mProgressDialog.show();
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                super.onProgressUpdate(progress);

                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgress(progress[0]);
            }

            @Override
            protected void onPostExecute(String result) {

                mProgressDialog.dismiss();
                if (result != null)
                    Toast.makeText(getActivity(), "Download error: " + result, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getActivity(), "JSON File Downloaded Successfully", Toast.LENGTH_SHORT).show();
            }

        }







}


package com.tutorialsbuzz.navigationdrawer;



import android.app.ProgressDialog;
import android.content.Context;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class DisplayContentFragment extends Fragment {


    private ProgressDialog mProgressDialog;
    WebView webview;
    public DisplayContentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_display_content, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        webview = (WebView) view.findViewById(R.id.webView);
        Button btnDisplayContent = (Button) view.findViewById(R.id.displaycontent);
        btnDisplayContent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

 try {
            unzip(getActivity().getFilesDir().toString()+"/contents.zip", getActivity().getFilesDir().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


                webview.loadUrl("file:///"+getActivity().getFilesDir()+"/contents/contents.html");

            }
            });


        Button btnDownloadZip = (Button) view.findViewById(R.id.downloadzip);
        btnDownloadZip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setMessage("Downloading Zip File from URL");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);


                File file = new File(getActivity().getFilesDir(), "download.json");
                InputStream is = null;

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
                    htmlURL = jsonObject.getString("HTML_URL");


                } catch (Exception e) {

                }

                final DownloadFileFromURL downloadTask = new DownloadFileFromURL(getActivity());
                downloadTask.execute(htmlURL);

            }

        });




    }

    class DownloadFileFromURL extends AsyncTask<String, Integer, String> {

        private Context context;


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

                System.out.println("play video"+sUrl[0]+"\n");
                System.out.println(getActivity().getFilesDir()+"\n");
                File file = new File(getActivity().getFilesDir(), "contents.zip");

                output = new FileOutputStream(file);
                byte data[] = new byte[5 * 1000 * 1000];
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
                Toast.makeText(getActivity(), "Zip File Downloaded Successfully", Toast.LENGTH_SHORT).show();


        }

    }

    public static void unzip(String zipFile, String location) throws IOException {
        try {
            File f = new File(location);
            if(!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + File.separator +ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if(!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    }
                    else {
                        FileOutputStream fout = new FileOutputStream(path, false);
                        try {
                            for (int c = zin.read(); c != -1; c = zin.read()) {
                                fout.write(c);
                            }
                            zin.closeEntry();
                        }
                        finally {
                            fout.close();
                        }
                    }
                }
            }
            finally {
                zin.close();
            }
        }
        catch (Exception e) {

        }
    }

}

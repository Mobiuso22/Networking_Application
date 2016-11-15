package com.tutorialsbuzz.navigationdrawer;




        import android.app.ProgressDialog;
        import android.content.Context;
        import android.media.MediaPlayer;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.PowerManager;
        import android.support.v4.app.Fragment;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.MediaController;
        import android.widget.Toast;
        import android.widget.VideoView;

        import org.json.JSONObject;

        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.net.HttpURLConnection;
        import java.net.URL;


public class PlayVideoFragment extends Fragment {


    private VideoView myVideoView;
    private int position = 0;
    private ProgressDialog progressDialog;
    private MediaController mediaControls;
    ProgressDialog mProgressDialog;
    boolean bVideoDownloaded = false;
    private final Object pauseLock = new Object();


    public PlayVideoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_play_video, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {



        Button btnDownloadVideo = (Button) view.findViewById(R.id.downloadvideo);
        btnDownloadVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setMessage("Downloading Video File from URL");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);


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


                } catch (Exception e) {

                }

                final DownloadFileFromURL downloadTask = new DownloadFileFromURL(getActivity());
                downloadTask.execute(mediaURL);

            }

        });

        if (mediaControls == null) {

            mediaControls = new MediaController(getActivity());

        }




        myVideoView = (VideoView) view.findViewById(R.id.video_view);


        Button btnPlayVideo = (Button) view.findViewById(R.id.playvideo);
        btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {




                progressDialog = new ProgressDialog(getActivity());



                progressDialog.setTitle("Playing Video");



                progressDialog.setMessage("Loading...");



                progressDialog.setCancelable(false);



                progressDialog.show();


                try {



                    myVideoView.setMediaController(mediaControls);




                    myVideoView.setVideoURI(Uri.parse(getActivity().getFilesDir() + "/Sample.mp4"));


                } catch (Exception e) {

                    Log.e("Error", e.getMessage());

                    e.printStackTrace();

                }


                myVideoView.requestFocus();



                myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {


                    public void onPrepared(MediaPlayer mediaPlayer) {



                        progressDialog.dismiss();



                        myVideoView.seekTo(position);

                        if (position == 0) {
                            myVideoView.start();

                        } else {

                            //if we come from a resumed activity, video playback will be paused

                            myVideoView.pause();

                        }

                    }

                });


            }


        });
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);

        //we use onSaveInstanceState in order to store the video playback position for orientation change

        savedInstanceState.putInt("Position", myVideoView.getCurrentPosition());

        myVideoView.pause();

    }





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

                System.out.println("play video"+sUrl[0]+"\n");
                System.out.println(getActivity().getFilesDir()+"\n");
                File file = new File(getActivity().getFilesDir(), "Sample.mp4");

                output = new FileOutputStream(file);
                byte data[] = new byte[20 * 1000 * 1000];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // System.out.println("mediaurl"+mediaURL); allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
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
                Toast.makeText(getActivity(), "Video File Downloaded Successfully", Toast.LENGTH_SHORT).show();

            synchronized (pauseLock) {

                pauseLock.notifyAll(); // Unblocks thread
            }
            bVideoDownloaded = true;
        }

    }

}

package downloader.netease.liang.lance;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.*;
import io.reactivex.disposables.*;
import java.util.List;
//import java.util.Observer;


import io.reactivex.disposables.Disposable;

public class Data {
}

class Song {
    public static class Artist {
        public String id, name, img1v1Url;
    }
    public static class Album {
        public static class Artist2 {
            public String img1v1Url;
        }
        public Artist2 artist;
        public String name;
    }
    public String name;
    public long id;
    public List<Artist> artists;
    public Album album;
    static public String getFilename(Song s) {
        String filename = "";
        for (Song.Artist a: s.artists)
            filename = filename + a.name + "、";
        filename = filename.substring(0, filename.length() - 1);
        filename = filename + " - " + s.name + ".mp3";
        return filename;
    }
}


class NetEaseMusicData {
    public int code;
    public Result result;
    public static class Result {
        public List<Song> songs;
        public int songCount;
    }
}

class Playlist {
    public static class Creator {
        public String nickname;
    }
    public String name, coverImgUrl, description;
    public String id;
    public Creator creator;
}


class NetEasePlaylistData {
    public int code;
    public Result result;
    public static class Result {
        public int playlistCount;
        public List<Playlist> playlists;
    }
}

class Track {
    static public class Ar {
        public long id;
        public String name;
    }
    static public class Al {
        public String name, picUrl;
        public long id;
    }
    public String name;
    public long id;
    public List<Ar> ar;
    public Al al;
    static public String getFilename(Track s) {
        String filename = "";
        for (Track.Ar a: s.ar)
            filename = filename + a.name + "、";
        filename = filename.substring(0, filename.length() - 1);
        filename = filename + " - " + s.name + ".mp3";
        return filename;
    }
}

class NetEasePlaylistContentData {
    public int code;
    static public class Playlist {
        static public class Creator {
            public String nickname;
        }
        public Creator creator;
        public String coverImgUrl, description, name;
        public long id;
        public List<Track> tracks;
    }
    public Playlist playlist;
}

class NetEaseMusicDownloadData {
    public int code;
    static public class Data {
        public String url;
    }
    public List<Data> data;
}

class NetEaseAPI {
    static String SONG = "SONG", PLAYLIST = "PLAYLIST",
            url_search = "https://v1.hitokoto.cn/nm/search/",
            url_playlist = "https://v1.hitokoto.cn/nm/playlist/",
            api_download = "https://music.163.com/song/media/outer/url?id=",
            url_download = "https://v1.hitokoto.cn/nm/url/";

    static public String form_download_url(int id) {
        return api_download + id + ".mp3";
    }

    static public String form_download_url(long id) {
        return api_download + id + ".mp3";
    }

    static public void getSongs(Context context, String key, int offset, StringCallback callback) {
        OkGo.<String>get(url_search + key)
                .params("type", SONG)
                .params("limit", "30")
                .params("offset", offset)
                .execute(callback);
    }

    static public void getPlaylists(Context context, String key, int offset, StringCallback callback) {
        OkGo.<String>get(url_search + key)
                .params("type", PLAYLIST)
                .params("limit", "30")
                .params("offset", offset)
                .execute(callback);
    }

    static public void getPlaylist(Context context, String id, StringCallback callback) {
        OkGo.<String>get(url_playlist + id)
                .execute(callback);
    }

    static public void download(final Activity context, long id, final String filename) {
        final long gid = id;
        StringCallback callback = new StringCallback() {
            @Override
            public void onError(Response<String> response) {
                onSuccess(response);
                super.onError(response);
            }

            @Override
            public void onSuccess(Response<String> p1) {
                final NetEaseMusicDownloadData data = new Gson().fromJson(p1.body(), NetEaseMusicDownloadData.class);
                if (data == null) {
                    RxPermissions rxPermissions = new RxPermissions(context);
                    rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(new Observer<Boolean>() {
                                @Override
                                public void onError(Throwable p1) {}
                                @Override
                                public void onComplete() {}
                                @Override
                                public void onSubscribe(Disposable d) {}
                                @Override
                                public void onNext(Boolean aBoolean) {
                                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                                    Uri uri = Uri.parse(NetEaseAPI.form_download_url(gid));
                                    DownloadManager.Request request = new DownloadManager.Request(uri);
                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);
                                    request.setDestinationInExternalPublicDir("Music/", filename);
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    downloadManager.enqueue(request);

//                                    OkGo.<>
                                }});
                    return;
                }
                if (data.code == 200) {
                    RxPermissions rxPermissions = new RxPermissions(context);
                    rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(new Observer<Boolean>() {
                                @Override
                                public void onError(Throwable p1) {}
                                @Override
                                public void onComplete() {}
                                @Override
                                public void onSubscribe(Disposable d) {}
                                @Override
                                public void onNext(Boolean aBoolean) {
                                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

//                                    if (data.data.get(0).url == null) {
//                                        Toast.makeText(MyApplication.getMyApplication(), "Download Failed", Toast.LENGTH_LONG);
//                                        return;
//                                    }
                                    Uri uri = Uri.parse(data.data.get(0).url);
                                    DownloadManager.Request request = new DownloadManager.Request(uri);
                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);
                                    request.setDestinationInExternalPublicDir("Music/", filename);
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    downloadManager.enqueue(request);
                                }});
                }
            }
        };
        OkGo.<String>get(url_download + id)
                .execute(callback);
//        OkGo.<String>get(form_download_url(id))
//                .execute(callback);
    }

    static public void listenOnline(Context context, long id, StringCallback callback) {
        OkGo.<String>get(url_download + id)
                .execute(callback);
//        OkGo.<String>get(form_download_url(id))
//                .execute(callback);
    }

}


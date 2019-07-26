package downloader.netease.liang.lance;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import downloader.netease.liang.lance.Data.*;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ListMusicActivity extends AppCompatActivity {
    private EditText text_search_key;
    private ListView list = null;
    private MusicAdapter madp;
    private PlaylistContent padp;
    LinearLayout bg;
    ModeCallback mcallback;
    ModeCallbackPlaylist pcallback;
    int offset = 0;
    private LinearLayout footer;
    int search_res = 0;

    void initList()
    {
        list = (ListView) findViewById(R.id.list_music_ListView);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(mcallback);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
//        setTheme(Config.get(this).data.settings.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_music);

        String int_playlist_id = "0";
        try {
            Bundle extras = getIntent().getExtras();
            if (extras != null)
                int_playlist_id = extras.getString("playlist_id");
        } catch (NullPointerException e) {
            Logger.getLogger("NeteaseMusic").info("No playlist_id put!");
        }

//        ActionBar bar = getSupportActionBar();
//        bar.setTitle("网易云音乐下载器");
//        bar.setDisplayHomeAsUpEnabled(true);
//        bar.setHomeButtonEnabled(true);

        madp = new MusicAdapter(this);
        padp = new PlaylistContent(this);
        mcallback = new ModeCallback();
        pcallback = new ModeCallbackPlaylist();

        //    @Override
        //    protected void onCreate(Bundle savedInstanceState) {
        //        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_list_music);
        //    }
        Button btn_search_songs = (Button) findViewById(R.id.list_music_Button_search_songs);
        Button btn_search_playlists = (Button) findViewById(R.id.list_music_Button_search_playlist);
        text_search_key = (EditText) findViewById(R.id.list_music_EditText_text);
        bg = (LinearLayout) findViewById(R.id.list_music_LinearLayout1);
        if (!"0".equals(int_playlist_id)) {
            bg.setVisibility(View.GONE);
            getPlaylistSongs(int_playlist_id);
        }
//        else {
        if (list == null) {
            initList();
            list.setAdapter(madp);
        }
//        }

        btn_search_songs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                getSongs();
            }
        });
        text_search_key.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView p1, int p2, KeyEvent p3) {
                if (text_search_key.getText().toString().endsWith("\n"))
                    text_search_key.setText(text_search_key.getText().toString().substring(0, text_search_key.getText().toString().length()));
                getSongs();
                return false;
            }
        });
        btn_search_playlists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                Bundle bundle = new Bundle();
                bundle.putString("key", text_search_key.getText().toString());
                startActivity(new Intent().setClass(ListMusicActivity.this, PlaylistActivity.class).putExtras(bundle));
            }
        });

        footer = new LinearLayout(this);
        TextView header_text = new TextView(this);
        header_text.setText("\n加载更多\n");
        footer.addView(header_text);
        footer.setGravity(Gravity.CENTER);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                offset = offset + 30;
                refreshSongs();
            }
        });
        list.addFooterView(footer);
        footer.setVisibility(View.GONE);

        list.setDividerHeight(0);
    }

    void getPlaylistSongs(String id)
    {
        NetEaseAPI.getPlaylist(this, id, new StringCallback() {
            @Override
            public void onSuccess(Response<String> p1) {
                NetEasePlaylistContentData data = new Gson().fromJson(p1.body(), NetEasePlaylistContentData.class);
                if (data.code != 200) return;
                View inview = LayoutInflater.from(ListMusicActivity.this).inflate(R.layout.playlist_head, null);
                TextView text_name = (TextView) inview.findViewById(R.id.playlistheadTextView_name),
                        text_id = (TextView) inview.findViewById(R.id.playlistheadTextView_id),
                        text_desc = (TextView) inview.findViewById(R.id.playlistheadTextView_desc),
                        text_creator = (TextView) inview.findViewById(R.id.playlistheadTextView_creator);
                ImageView image = (ImageView) inview.findViewById(R.id.playlistheadImageView_image);
                //final ImageView bg = (ImageView) inview.findViewById(R.id.playlistheadImageView_bg);
                RelativeLayout layout_bg = (RelativeLayout) inview.findViewById(R.id.playlistheadRelativeLayout_bg);
                int colorbg = Utils.getPrimaryColor(ListMusicActivity.this);
                layout_bg.setBackgroundColor(colorbg & 0x00FFFFFF + 0x50000000);

                text_name.setText(data.playlist.name);
                text_id.setText("" + data.playlist.id);
                text_creator.setText(data.playlist.creator.nickname);
                text_desc.setText(data.playlist.description);

					/*
					Glide.with(ListMusicActivity.this).load(data.playlist.coverImgUrl)
						.apply(new RequestOptions().centerCrop()
							.diskCacheStrategy(DiskCacheStrategy.DATA)
							.transform(new BlurTransformation(25)))
						.transition(DrawableTransitionOptions.withCrossFade())
						//.into(new SimpleTarget<Drawable>() {
						//	@Override
						//	public void onResourceReady(Drawable p1, Transition<? super Drawable> p2) {
						//		bg.setBackgroundDrawable(p1);
						//	}
						//})
							.into(bg);*/

                Glide.with(ListMusicActivity.this).load(data.playlist.coverImgUrl)
                        .apply(new RequestOptions().centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(image);

//                if(list.mAdapter != null)
//                    list.mAdapter = null;
                if(list.getAdapter() != null)
                    list.setAdapter(null);
                try {
                    list.addHeaderView(inview);
                    list.setMultiChoiceModeListener(pcallback);

                    list.setAdapter(padp);
                } catch(IllegalStateException e) {}
                padp.list_data.clear();
                padp.list_data.add(data.playlist.tracks.get(0));
//                for (Track s: data.playlist.tracks)
//                    padp.list_data.add(s);
                padp.list_data.addAll(data.playlist.tracks);
                padp.notifyDataSetChanged();
                list.setOnItemClickListener(playlist_click);
            }
        });
    }

    AdapterView.OnItemClickListener music_click = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
            if (p3 < 0) return;
            final Song bean = madp.list_data.get(p3);
            /*
            NetEaseAPI.listenOnline(ListMusicActivity.this, bean.id, new StringCallback() {
                @Override
                public void onSuccess(Response<String> p1) {
                    NetEaseMusicDownloadData data = new Gson().fromJson(p1.body(), NetEaseMusicDownloadData.class);
                    if (data == null) {
                        Intent mIntent = new Intent();
                        mIntent.setAction(android.content.Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(NetEaseAPI.form_download_url(bean.id));
                        mIntent.setDataAndType(uri , "audio/mp3");
                        startActivity(mIntent);
                        return;
                    }

//                    if (data.code != 200) return;
                    Intent mIntent = new Intent();
                    mIntent.setAction(android.content.Intent.ACTION_VIEW);
                    Uri uri;
                    if (data.data.get(0).url == null)
                    {
                        uri = Uri.parse(data.data.get(0).url);
                    }
                    else {
                        uri = Uri.parse(NetEaseAPI.form_download_url(bean.id));
                    }
//                    Uri uri = Uri.parse('/')
                    mIntent.setDataAndType(uri , "audio/mp3");
                    startActivity(mIntent);
                }

                @Override
                public void onError(Response<String> response) {
//                    Logger.getLogger("NeteaseMusic").info("onError");
                    onSuccess(response);
                    super.onError(response);
                }
            });
            */
            NetEaseAPI.download(ListMusicActivity.this, bean.id, Song.getFilename(bean));
            Toast.makeText(ListMusicActivity.this, "开始下载" + Song.getFilename(bean), Toast.LENGTH_LONG)
                    .show();
        }
    };
    AdapterView.OnItemClickListener playlist_click = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
            if (p3 < 1) return;
            final Track bean = padp.list_data.get(p3 - 1);
            /*
            NetEaseAPI.listenOnline(ListMusicActivity.this, bean.id, new StringCallback() {
                @Override
                public void onSuccess(Response<String> p1) {
                    NetEaseMusicDownloadData data = new Gson().fromJson(p1.body(), NetEaseMusicDownloadData.class);
                    if (data == null) {
                        Intent mIntent = new Intent();
                        mIntent.setAction(android.content.Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(NetEaseAPI.form_download_url(bean.id));
                        mIntent.setDataAndType(uri , "audio/mp3");
                        startActivity(mIntent);
                        return;
                    }
                    if (data.code != 200) return;
                    Intent mIntent = new Intent();
                    mIntent.setAction(android.content.Intent.ACTION_VIEW);
                    Uri uri = Uri.parse(data.data.get(0).url);
                    mIntent.setDataAndType(uri , "audio/mp3");
                    startActivity(mIntent);
                }

                @Override
                public void onError(Response<String> response) {
//                    Logger.getLogger("NeteaseMusic").info("onError");
                    onSuccess(response);
                    super.onError(response);
                }
            });
            */
            NetEaseAPI.download(ListMusicActivity.this, bean.id, Track.getFilename(bean));
            Toast.makeText(ListMusicActivity.this, "开始下载" + Track.getFilename(bean), Toast.LENGTH_LONG)
                .show();
        }
    };

    void getSongs() {
        offset = 0;
        footer.setVisibility(View.VISIBLE);
        NetEaseAPI.getSongs(this, text_search_key.getText().toString(), offset, new StringCallback() {
            @Override
            public void onSuccess(Response<String> p1) {
                NetEaseMusicData data = new Gson().fromJson(p1.body(), NetEaseMusicData.class);
                if (data.code != 200) return;
                search_res = data.result.songCount;
                if (search_res == 0) return;
                //list.setAdapter(madp);
                madp.list_data.clear();
//                for (Song s: data.result.songs) {
//                    madp.list_data.add(s);
//                }
                madp.list_data.addAll(data.result.songs);
                if (offset + 30 < search_res)
                    footer.setVisibility(View.VISIBLE);
                else
                    footer.setVisibility(View.GONE);
                madp.notifyDataSetChanged();
                list.setOnItemClickListener(music_click);
            }
        });
    }

    void refreshSongs() {
        NetEaseAPI.getSongs(this, text_search_key.getText().toString(), offset, new StringCallback() {
            @Override
            public void onSuccess(Response<String> p1) {
                NetEaseMusicData data = new Gson().fromJson(p1.body(), NetEaseMusicData.class);
                if (data.code != 200) return;
                search_res = data.result.songCount;
                if (search_res == 0) return;
                //list.setAdapter(madp);
//                for (Song s: data.result.songs) {
//                    madp.list_data.add(s);
//                }
                madp.list_data.addAll(data.result.songs);
                if (offset + 30 < search_res)
                    footer.setVisibility(View.VISIBLE);
                else
                    footer.setVisibility(View.GONE);
                madp.notifyDataSetChanged();
                list.setOnItemClickListener(music_click);
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.music_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.enter_mode:
                list.setItemChecked(0, true);
                list.clearChoices();
                if (bg.getVisibility() != View.GONE)
                    mcallback.updateSelectedCount();
                else
                    pcallback.updateSelectedCount();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class ModeCallback implements AbsListView.MultiChoiceModeListener {

        View actionBarView;
        TextView selectedNum;

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        //退出多选模式时调用
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            list.clearChoices();
        }

        //进入多选模式调用，初始化ActionBar的菜单和布局
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.multiple_mode_menu, menu);
            if(actionBarView == null) {
                actionBarView = LayoutInflater.from(ListMusicActivity.this).inflate(R.layout.actionbar_view, null);
                selectedNum = (TextView) actionBarView.findViewById(R.id.selected_num);
            }
            mode.setCustomView(actionBarView);
            return true;
        }

        //ActionBar上的菜单项被点击时调用
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()) {
                case R.id.select_all:
                    for(int i = 0; i < madp.getCount(); i++) {
                        list.setItemChecked(i, true);
                    }
                    updateSelectedCount();
                    madp.notifyDataSetChanged();
                    break;
                case R.id.unselect_all:
                    list.clearChoices();
                    updateSelectedCount();
                    madp.notifyDataSetChanged();
                    break;
                case R.id.select_download:
                    for (int i=0; i<madp.list_data.size(); i++) {
                        if (!list.isItemChecked(i)) continue;
                        final Song bean = madp.list_data.get(i);
                        NetEaseAPI.download(ListMusicActivity.this, bean.id, Song.getFilename(bean));
                    }
                    list.setItemChecked(0, false);
                    list.clearChoices();
                    mcallback.updateSelectedCount();
                    break;
            }
            return true;
        }

        //列表项的选中状态被改变时调用
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            updateSelectedCount();
            mode.invalidate();
            madp.notifyDataSetChanged();
        }

        public void updateSelectedCount() {
            int selectedCount = list.getCheckedItemCount();
            selectedNum.setText(selectedCount + "");
        }
    }

    class ModeCallbackPlaylist implements AbsListView.MultiChoiceModeListener {

        View actionBarView;
        TextView selectedNum;

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        //退出多选模式时调用
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            list.clearChoices();
        }

        //进入多选模式调用，初始化ActionBar的菜单和布局
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.multiple_mode_menu, menu);
            if(actionBarView == null) {
                actionBarView = LayoutInflater.from(ListMusicActivity.this).inflate(R.layout.actionbar_view, null);
                selectedNum = (TextView) actionBarView.findViewById(R.id.selected_num);
            }
            mode.setCustomView(actionBarView);
            return true;
        }

        //ActionBar上的菜单项被点击时调用
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()) {
                case R.id.select_all:
                    for(int i = 0; i <= padp.getCount(); i++) {
                        list.setItemChecked(i, true);
                    }
                    updateSelectedCount();
                    padp.notifyDataSetChanged();
                    break;
                case R.id.unselect_all:
                    list.clearChoices();
                    updateSelectedCount();
                    padp.notifyDataSetChanged();
                    break;
                case R.id.select_download:
                    for (int i=1; i<padp.list_data.size(); i++) {
                        int d = i - 1;
                        if (!list.isItemChecked(i)) continue;
                        final Track bean = padp.list_data.get(d);
                        NetEaseAPI.download(ListMusicActivity.this, bean.id, Track.getFilename(bean));
                    }
                    list.setItemChecked(0, false);
                    list.clearChoices();
                    pcallback.updateSelectedCount();
                    break;
            }
            return true;
        }

        //列表项的选中状态被改变时调用
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                               long id, boolean checked) {
            updateSelectedCount();
            mode.invalidate();
            padp.notifyDataSetChanged();
        }

        public void updateSelectedCount() {
            int selectedCount = list.getCheckedItemCount();
            selectedNum.setText(selectedCount + "");
        }
    }

    class MusicAdapter extends BaseAdapter
    {
        public List<Song> list_data = new ArrayList<Song>();
        Context context;
        MusicAdapter(Context context) {
            this.context = context;
        }
        @Override
        public int getCount() {
            return list_data.size();
        }
        @Override
        public Object getItem(int p1) {
            return list_data.get(p1);
        }
        @Override
        public long getItemId(int p1) {
            return p1;
        }
        @Override
        public View getView(int p1, View p2, ViewGroup p3) {
            //if (p1 < 0) return new View(context);
            Song bean = list_data.get(p1);
            LinearLayout view = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.item_music, null);
            TextView text_name = (TextView) view.findViewById(R.id.itemmusicTextView_name),
                    text_id = (TextView) view.findViewById(R.id.itemmusicTextView_id),
                    text_artists = (TextView) view.findViewById(R.id.itemmusicTextView_artist);
//		ImageView image = (ImageView) view.findViewById(R.id.itemmusicImageView_image);
            text_name.setText(bean.name);
            text_id.setText("" + bean.id);
            String artists = "";
            for (Song.Artist a: bean.artists)
                artists = artists + a.name + "/";
            artists = artists.substring(0, artists.length() - 1);
            text_artists.setText(artists);
//		Glide.with(context).load(bean.album.artist.img1v1Url)
//			.apply(new RequestOptions().centerCrop())
//			.transition(DrawableTransitionOptions.withCrossFade())
//			.into(image);
            if (list.isItemChecked(p1))
                view.setBackgroundColor(Utils.getAccentColor(ListMusicActivity.this));
            else
                view.setBackgroundColor(Color.TRANSPARENT);
            return view;
        }
    }

    class PlaylistContent extends BaseAdapter
    {
        public List<Track> list_data = new ArrayList<Track>();
        Context context;
        PlaylistContent(Context context) {
            this.context = context;
        }
        @Override
        public int getCount() {
            return list_data.size();
        }
        @Override
        public Object getItem(int p1) {
            return list_data.get(p1);
        }
        @Override
        public long getItemId(int p1) {
            return p1;
        }
        @Override
        public View getView(int p1, View p2, ViewGroup p3) {
            //if (p1 < 0) return new View(context);
            Track bean = list_data.get(p1);
            LinearLayout view = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.item_music, null);
            TextView text_name = (TextView) view.findViewById(R.id.itemmusicTextView_name),
                    text_id = (TextView) view.findViewById(R.id.itemmusicTextView_id),
                    text_artists = (TextView) view.findViewById(R.id.itemmusicTextView_artist);
//		ImageView image = (ImageView) view.findViewById(R.id.itemmusicImageView_image);
            text_name.setText(bean.name);
            text_id.setText("" + bean.id);
            String artists = "";
            for (Track.Ar a: bean.ar)
                artists = artists + a.name + "/";
            artists = artists.substring(0, artists.length() - 1);
            text_artists.setText(artists);
//		Glide.with(context).load(bean.album.artist.img1v1Url)
//			.apply(new RequestOptions().centerCrop())
//			.transition(DrawableTransitionOptions.withCrossFade())
//			.into(image);
            if (list.isItemChecked(p1 + 1))
                view.setBackgroundColor(Utils.getAccentColor(ListMusicActivity.this));
            else
                view.setBackgroundColor(Color.TRANSPARENT);
            return view;
        }
    }
}

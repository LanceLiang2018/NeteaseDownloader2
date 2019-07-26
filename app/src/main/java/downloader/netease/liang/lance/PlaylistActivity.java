package downloader.netease.liang.lance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity
{
    private Button btn_search_songs, btn_search_playlists;
    private EditText text_search_key;
    private ListView list;
    private LinearLayout bg;
    PlaylistAdapter adp;
    int search_res = 0, offset = 0;
    LinearLayout footer;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
//        setTheme(Config.get(this).data.settings.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_music);

        btn_search_songs = (Button) findViewById(R.id.list_music_Button_search_songs);
        btn_search_playlists = (Button) findViewById(R.id.list_music_Button_search_playlist);
        text_search_key = (EditText) findViewById(R.id.list_music_EditText_text);
        bg = (LinearLayout) findViewById(R.id.list_music_LinearLayout1);
        list = (ListView) findViewById(R.id.list_music_ListView);

        adp = new PlaylistAdapter(this);
        list.setAdapter(adp);

        ActionBar bar = getSupportActionBar();
        bar.setTitle("网易云歌单下载");
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        String key = "";
        try {
            key = getIntent().getExtras().getString("key");
        } catch (Exception e) {}
        if (!"".equals(key)) {
            text_search_key.setText(key);
            searchPlaylist(key);
        }

        btn_search_songs.setVisibility(View.GONE);
        btn_search_playlists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                offset = 0;
                adp.list_data.clear();
                adp.notifyDataSetChanged();
                searchPlaylist(text_search_key.getText().toString());
            }
        });
        list.setOnItemClickListener(playlist_click);

        text_search_key.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView p1, int p2, KeyEvent p3) {
                offset = 0;
                adp.list_data.clear();
                adp.notifyDataSetChanged();
                searchPlaylist(text_search_key.getText().toString());
                return false;
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
                searchPlaylist(text_search_key.getText().toString());
            }
        });
        list.addFooterView(footer);
        list.setDividerHeight(0);
        footer.setVisibility(View.GONE);

    }

    void searchPlaylist(String key) {
        NetEaseAPI.getPlaylists(this, key, offset, new StringCallback() {
            @Override
            public void onSuccess(Response<String> p1) {
                NetEasePlaylistData data = new Gson().fromJson(p1.body(), NetEasePlaylistData.class);
                if (data.code != 200) return;
                search_res = data.result.playlistCount;
                for (Playlist s: data.result.playlists) {
                    adp.list_data.add(s);
                }
                if (offset + 30 < search_res)
                    footer.setVisibility(View.VISIBLE);
                else
                    footer.setVisibility(View.GONE);
                adp.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    AdapterView.OnItemClickListener playlist_click = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
            if (p3 < 0) return;
            Playlist bean = adp.list_data.get(p3);
            Bundle bundle = new Bundle();
            bundle.putString("playlist_id", bean.id);
            startActivity(new Intent().setClass(PlaylistActivity.this, ListMusicActivity.class).putExtras(bundle));
        }
    };


    class PlaylistAdapter extends BaseAdapter
    {
        List<Playlist> list_data = new ArrayList<Playlist>();
        Context context;

        PlaylistAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() { return list_data.size(); }

        @Override
        public Object getItem(int p1) { return list_data.get(p1); }

        @Override
        public long getItemId(int p1) { return p1; }

        @Override
        public View getView(int p1, View p2, ViewGroup p3)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, null);
            ImageView image = (ImageView) view.findViewById(R.id.itemplaylistImageView_image);
            TextView text_name = (TextView) view.findViewById(R.id.itemplaylistTextView_name),
                    text_id = (TextView) view.findViewById(R.id.itemplaylistTextView_id),
                    text_desc = (TextView) view.findViewById(R.id.itemplaylistTextView_desc),
                    text_creator = (TextView) view.findViewById(R.id.itemplaylistTextView_creator);
            Playlist bean = list_data.get(p1);
            text_name.setText(bean.name);
            text_id.setText("" + bean.id);
            text_desc.setText(bean.description);
            text_creator.setText(bean.creator.nickname);

            Glide.with(context).load(bean.coverImgUrl)
                    .apply(new RequestOptions().centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(image);

            return view;
        }
    }
}


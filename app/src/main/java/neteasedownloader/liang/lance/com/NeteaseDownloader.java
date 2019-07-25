package neteasedownloader.liang.lance.com;

import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NeteaseDownloader extends AppCompatActivity {

    Snackbar snackbar;
    ConstraintLayout layout_back;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netease_downloader);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        layout_back = (ConstraintLayout)findViewById(R.id.layout_back);
        snackbar = Snackbar.make(layout_back, "正在加载...", Snackbar.LENGTH_INDEFINITE);
        recyclerView = (RecyclerView)findViewById(R.id.list_music);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.show();
            }
        });

        // 设置LIST
        List<String> data = new ArrayList<>();
        for (int i=0; i<100; i++) {
            data.add("" + i);
        }
        QuickAdapter mAdapter = new QuickAdapter<String>(data) {
            @Override
            public int getLayoutId(int viewType) {
                return R.layout.activity_netease_downloader;
            }

            @Override
            public void convert(VH holder, String data, int position) {
                if (holder != null)
                    holder.setText(R.id.text, data);
                //holder.itemView.setOnClickListener(); 此处还可以添加点击事件
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

//        Button button = (Button)findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                snackbar.dismiss();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_netease_downloader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package atua.anddev.globaltv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import atua.anddev.globaltv.adapters.ChannelHolderAdapter;
import atua.anddev.globaltv.entity.Channel;

public class GlobalFavoriteActivity extends AppCompatActivity implements GlobalServices,
        ChannelHolderAdapter.OnItemClickListener {
    private List<Channel> favoriteList;
    private ChannelHolderAdapter mAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set sub.xml as user interface layout
        setContentView(R.layout.globalfavorite);

        favoriteList = new ArrayList<>();
        setupActionBar();
        showFavorites();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Channel item, int viewId) {
        switch (viewId) {
            case R.id.favoriteIcon:
                changeFavorite(item);
                break;
            case R.id.title:
                guideActivity(item);
                break;
            default:
                setTick(item);
                openFavorite(item);
                break;
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void showFavorites() {
        favoriteList = favoriteService.getFavoriteList();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mAdapter = new ChannelHolderAdapter(this, R.layout.item_channellist, favoriteList, true);
        mAdapter.setOnItemClickListener(GlobalFavoriteActivity.this);
        recyclerView.setAdapter(mAdapter);
        updateInfo();
    }

    private void updateInfo() {
        TextView textview = (TextView) findViewById(R.id.globalfavoriteTextView1);
        textview.setText(getResources().getString(R.string.favorites) + " - " +
                mAdapter.getItemCount() + " " + getString(R.string.channels));
    }

    private void openFavorite(Channel item) {
        String getProvName = item.getProvider();
        int numA = playlistService.indexNameForActivePlaylist(getProvName);
        if (numA == -1) {
            Toast.makeText(GlobalFavoriteActivity.this, getString(R.string.playlistnotexist), Toast.LENGTH_SHORT).show();
            return;
        }
        playlistService.readPlaylist(numA);
        for (Channel chn : channelService.getAllChannels()) {
            if (chn.getName().equals(item.getName())) {
                openChannel(chn);
                break;
            }
        }
    }

    private void setTick(Channel channel) {
        mAdapter.setSelected(channel);
        mAdapter.notifyDataSetChanged();
    }

    private void changeFavorite(Channel item) {
        favoriteService.deleteFromFavoritesByChannel(item);
        favoriteList.remove(item);
        try {
            favoriteService.saveFavorites(GlobalFavoriteActivity.this);
        } catch (IOException ignored) {
        }
        mAdapter.notifyDataSetChanged();
    }

    private void guideActivity(Channel channel) {
        Intent intent = new Intent(this, GuideActivity.class);
        intent.putExtra("channel", channel);
        startActivity(intent);
    }

    private void openChannel(Channel channel) {
        if (Global.useInternalPlayer) {
            Intent intent = new Intent(GlobalFavoriteActivity.this, PlayerActivity.class);
            intent.putExtra("channel", channel);
            startActivity(intent);
        } else
            channelService.openChannel(GlobalFavoriteActivity.this, channel);
    }

}

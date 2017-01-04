package atua.anddev.globaltv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import atua.anddev.globaltv.adapters.ChannelHolderAdapter;
import atua.anddev.globaltv.entity.Channel;
import atua.anddev.globaltv.entity.Favorites;

public class GlobalFavoriteActivity extends MainActivity implements GlobalServices, ChannelHolderAdapter.OnItemClickListener {
    private List<Channel> favoriteList;
    private ChannelHolderAdapter mAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set sub.xml as user interface layout
        setContentView(R.layout.globalfavorite);

        favoriteList = new ArrayList<Channel>();
        showFavorites();
    }

    @Override
    public void onItemClick(Channel item, int viewId) {
        switch (viewId) {
            case R.id.favoriteIcon:
                changeFavorite(item);
                break;
            case R.id.title:
                guideActivity(item.getName());
                break;
            default:
                setTick(item);
                openFavorite(item);
                break;
        }
    }


    public void showFavorites() {
        TextView textview = (TextView) findViewById(R.id.globalfavoriteTextView1);
        textview.setText(getResources().getString(R.string.favorites));

        if (favoriteList.size() == 0) {
            for (Favorites favorite : favoriteService.getFavoriteList()) {
                Channel channel = new Channel(favorite.getName(), "", "");
                channel.setProvider(favorite.getProv());
                favoriteList.add(channel);
            }
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mAdapter = new ChannelHolderAdapter(this, R.layout.item_channellist, favoriteList, true);
        mAdapter.setOnItemClickListener(GlobalFavoriteActivity.this);
        recyclerView.setAdapter(mAdapter);
    }

    private void openFavorite(Channel item) {
        String getProvName = item.getProvider();
        int numA = playlistService.indexNameForActivePlaylist(getProvName);
        if (numA == -1) {
            Toast.makeText(GlobalFavoriteActivity.this, getResources().getString(R.string.playlistnotexist), Toast.LENGTH_SHORT).show();
            return;
        }
        playlistService.readPlaylist(numA);
        for (Channel chn : channelService.getAllChannels()) {
            if (chn.getName().equals(item.getName())) {
                channelService.openURL(chn.getUrl(), GlobalFavoriteActivity.this);
                break;
            }
        }
    }

    private void setTick(Channel channel) {
        mAdapter.setSelected(channel);
        mAdapter.notifyDataSetChanged();
    }

    private void changeFavorite(Channel item) {
        Boolean changesAllowed = true;
        for (Favorites fav : favoriteService.getFavoriteList()) {
            if (item.getName().equals(fav.getName())
                    && item.getProvider().equals(fav.getProv()))
                changesAllowed = false;
        }
        if (changesAllowed)
            favoriteService.addToFavoriteList(item.getName(), item.getProvider());
        else
            favoriteService.deleteFromFavoritesByNameAndProv(item.getName(), item.getProvider());
        try {
            favoriteService.saveFavorites(GlobalFavoriteActivity.this);
        } catch (IOException ignored) {
        }
        mAdapter.notifyDataSetChanged();
    }

    private void guideActivity(String chName) {
        Intent intent = new Intent(this, GuideActivity.class);
        intent.putExtra("name", chName);
        startActivity(intent);
    }

}

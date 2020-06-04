package com.example.android.gpslog_test;

import android.Manifest;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION};

    public static int[] listImg = {
            R.drawable.ic_directions_walk_black_24dp,
            R.drawable.ic_directions_run_black_24dp,
            R.drawable.ic_directions_bike_black_24dp
    };

    public static int[] listImgTint = {
            R.drawable.ic_baseline_directions_walk_24,
            R.drawable.ic_baseline_directions_run_24,
            R.drawable.ic_baseline_directions_bike_24
    };

    public static int[] listFabIcon = {
            R.drawable.ic_play_arrow_black_24dp,
            R.drawable.ic_stop_black_24dp,
            R.drawable.ic_close_black_24dp
    };

    private AppViewModel appViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.requestMultiplePermissions(this, permissions);

        MapManage.setup(this);
        setContentView(R.layout.activity_main);

        MapManage.displayMap();
        ChartManage.getInstance().setup(this);

        FloatingActionButton startFab = findViewById(R.id.startFab);
        startFab.setOnClickListener(view -> appViewModel.nextState());

        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        final ActivityAdapter mAdapter = new ActivityAdapter(this);

        bottomAppBar.setOnMenuItemClickListener(this::menuSwitch);

//-------------RecyclerView---------------
        RecyclerView activitiesView = (RecyclerView) findViewById(R.id.recView);

        activitiesView.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        activitiesView.setLayoutManager(linearLayoutManager);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        activitiesView.setLayoutParams(params);

        mAdapter.setOnItemClickListener(new ActivityAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ExerciseEntity exer) throws ExecutionException, InterruptedException {
                bottomAppBar.getMenu().performIdentifierAction(R.id.historyShow, 0);
                // todo ladowanie danych do wyswietlania;
                appViewModel.setCurrentExercise(exer);
                appViewModel.loadHistoryTracks();
                appViewModel.setState(AppViewModel.States.HISTORY);
            }
        });

        activitiesView.setHasFixedSize(true);

        Chip chipSportData = findViewById(R.id.chipSportData);

        appViewModel = new ViewModelProvider(this).get(AppViewModel.class);

        // update recyclerview list
        appViewModel.getExercises().observe(this, new Observer<List<ExerciseEntity>>() {
            @Override
            public void onChanged(@Nullable final List<ExerciseEntity> exerList) {
                mAdapter.setExercises(exerList);
            }
        });

        // change chip data
        appViewModel.getCurrentExercise().observe(this, new Observer<ExerciseEntity>() {
            @Override
            public void onChanged(@Nullable final ExerciseEntity exer) {
                // change chip data
                String text = "";
                if (exer != null) {
                    Long start = exer.getStart();
                    SimpleDateFormat sdfStart = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    text += sdfStart.format(start);
                }
                chipSportData.setText(text);
            }
        });

        // change sport icon
        appViewModel.getLastType().observe(this, new Observer<TypeEntity>() {
            @Override
            public void onChanged(@Nullable final TypeEntity type) {
                // icon
                if (type!=null) {
                    int ind =0;
                    switch (type.getTypeName()){
                        case "WALK" : ind=0;break;
                        case "RUN" : ind=1;break;
                        case "BIKE" : ind=2;break;
                    }
                    MenuItem item = bottomAppBar.getMenu().findItem(R.id.actSet);
                    MaterialCardView mcv = findViewById(R.id.settingsView);
                    if (mcv.getVisibility() == View.VISIBLE) {
                        item.setIcon(listImgTint[ind]);
                    } else {
                        item.setIcon(listImg[ind]);
                    }
                    appViewModel.matchCurrExerWithLastType();
                }
            }
        });

        // start/stop fectching track data
        appViewModel.getGpsOk().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean isGpsOk) {

            }
        });

        appViewModel.getTracks().observe(this, new Observer<List<TrackEntity>>() {
            @Override
            public void onChanged(@Nullable final List<TrackEntity> tracks) {
                // todo mapa
                if (tracks != null && tracks.size() > 0) {
                    appViewModel.setLastTrack(tracks.get(tracks.size() - 1));
                }
                ChartManage.setData(tracks);
                MapManage.setData(tracks);

            }
        });

        // last track from current exer
        appViewModel.getLastTrack().observe(this, new Observer<TrackEntity>() {
            @Override
            public void onChanged(@Nullable final TrackEntity track) {
                // czas na bottomappbar

                String barText;
                long seconds = 0;
                long minutes = 0;
                long hours = 0;

                if (track != null) {
                    seconds = TimeUnit.SECONDS.convert(track.getTime(), TimeUnit.NANOSECONDS) % 60;
                    minutes = TimeUnit.MINUTES.convert(track.getTime(), TimeUnit.NANOSECONDS) % 60;
                    hours = TimeUnit.HOURS.convert(track.getTime(), TimeUnit.NANOSECONDS);
                }
                barText = String.format("%2d s", seconds);
                if (hours > 0 || minutes > 0) {
                    barText = String.format("%2d min ", minutes) + barText;
                } else {
                    barText = String.format("%7s", "") + barText;
                }
                if (hours > 0) {
                    barText = String.format("%2d h ", hours) + barText;
                } else {
                    barText = String.format("%7s", "") + barText;
                }
                bottomAppBar.getMenu().findItem(R.id.actDur).setTitle(barText);
                // todo
                // dodanie punktu do mapy
                MapManage.addTrack(track);
                // dodanie punktu do wykresu
                ChartManage.addTrack(track);

            }
        });

        // last track ever
        appViewModel.getAbsoluteLastTrack().observe(this, new Observer<TrackEntity>() {
            @Override
            public void onChanged(@Nullable final TrackEntity track) {
                    appViewModel.setLastTrack(track);
//                }

            }
        });

        // icon of startFab
        // data collection
        // data show
        appViewModel.getState().observe(this, new Observer<AppViewModel.AppState>() {
            @Override
            public void onChanged(@Nullable final AppViewModel.AppState state) {
                if (state != null) {
                    int iconInd = 0;
                    switch (state.getState()) {
                        case NOTHING:
                            chipSportData.setVisibility(View.INVISIBLE);
                            bottomAppBar.getMenu().findItem(R.id.historyShow).setEnabled(true);
                            bottomAppBar.getMenu().findItem(R.id.historyShow).getIcon()
                                    .setTint(getResources().getColor(android.R.color.white));
                            appViewModel.doNothing();
                            iconInd = 0;
                            break;
                        case GPS:
                            appViewModel.doNothing();
                            chipSportData.setVisibility(View.INVISIBLE);
                            bottomAppBar.getMenu().findItem(R.id.historyShow).getIcon()
                                    .setTint(getResources().getColor(android.R.color.darker_gray));                            
                            // if history visible perform id action
                            // todo moze zoptymalizowac
                            MaterialCardView mcv2 = findViewById(R.id.historyView);
                            if (mcv2.getVisibility() == View.VISIBLE) {
                                bottomAppBar.getMenu().performIdentifierAction(R.id.historyShow, 0);
                            }
                            // todo end
                            bottomAppBar.getMenu().findItem(R.id.historyShow).setEnabled(false);
                            appViewModel.collectData();
                            iconInd = 1;
                            break;
                        case HISTORY:
                            appViewModel.stopGps();
                            chipSportData.setVisibility(View.VISIBLE);
                            bottomAppBar.getMenu().findItem(R.id.historyShow).setEnabled(true);
                            bottomAppBar.getMenu().findItem(R.id.historyShow).getIcon()
                                    .setTint(getResources().getColor(android.R.color.white));
                            iconInd = 2;
                            // jesli wchodzimy tu z GPS, to wszystko powinno byc juz pokazane, wiec nie zmieniamy
                            // jesli wchodzi tu z NOTHING/HISTOTY, to robimy to przez recyclerview, ktory odpowiada
                            // za zaladowanie danych
                            break;
                    }
                    startFab.setImageResource(listFabIcon[iconInd]);
                }
            }
        });
        

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                appViewModel.deleteExercise(mAdapter.getExercise(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(activitiesView);

        FloatingActionButton zoominFab = findViewById(R.id.zoomIn);
        zoominFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MapManage.map.canZoomIn())
                    MapManage.map.getController().zoomIn();
            }
        });

        FloatingActionButton zoomoutFab = findViewById(R.id.zoomOut);
        zoomoutFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapManage.map.getController().zoomOut();
            }
        });

        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, @IdRes int checkedId) {
                MenuItem sportType = ((BottomAppBar) findViewById(R.id.bottom_app_bar)).getMenu().findItem(R.id.actSet);
                int typeInd = 0;
                switch (checkedId) {
                    case R.id.chipWalk:
                        typeInd = 0;
                        break;
                    case R.id.chipRun:
                        typeInd = 1;
                        break;
                    case R.id.chipBike:
                        typeInd = 2;
                        break;
                }
                appViewModel.setLastType(typeInd);
            }
        });

    }





    private boolean menuSwitch(MenuItem item) {
        MaterialCardView mcv2 = findViewById(R.id.historyView);
        MaterialCardView mcv = findViewById(R.id.cardView);
        MaterialCardView mcv1 = findViewById(R.id.settingsView);

        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        int height = bottomAppBar.getHeight();

        mcv.setTranslationY(-(int) (height));
        mcv1.setTranslationY(-(int) (height));
        mcv2.setTranslationY(-(int) (height));

        // hide all cardviews
        if (mcv.getVisibility() == View.VISIBLE && item.getItemId() != R.id.plotShow) {
            bottomAppBar.getMenu().performIdentifierAction(R.id.plotShow, 0);
        }
        if (mcv1.getVisibility() == View.VISIBLE && item.getItemId() != R.id.actSet) {
            bottomAppBar.getMenu().performIdentifierAction(R.id.actSet, 0);
        }
        if (mcv2.getVisibility() == View.VISIBLE && item.getItemId() != R.id.historyShow) {
            bottomAppBar.getMenu().performIdentifierAction(R.id.historyShow, 0);
        }

        switch (item.getItemId()) {

            case R.id.historyShow:

                if (mcv2.getVisibility() == View.INVISIBLE) {

                    View rootView = bottomAppBar.getRootView();
                    int height3 = rootView.getHeight();

                    ViewGroup.LayoutParams lp = mcv2.getLayoutParams();
                    // todo zrobic cos z magic numbers

                    int h1 = lp.height;
                    lp.height = height3 - (int) (2.75 * height);
                    mcv2.setLayoutParams(lp);
                    // todo layout params powinny sie zmieniac podczas rotacji

                    mcv2.setVisibility(View.VISIBLE);
//

                    Drawable d1 = item.getIcon();
                    d1.setTint(getColor(R.color.colorPrimaryDark));
                    item.setIcon(d1);

                    mcv2.invalidate();
                    return true;
                }

                if (mcv2.getVisibility() == View.VISIBLE) {
                    mcv2.setVisibility(View.INVISIBLE);
                    Drawable d1 = item.getIcon();
                    d1.setTint(Color.WHITE);
                    item.setIcon(d1);
                    return true;
                }

                return true;

            case R.id.plotShow:

                if (mcv.getVisibility() == View.INVISIBLE) {
                    View rootView = bottomAppBar.getRootView();
                    int height3 = rootView.getHeight();

                    ViewGroup.LayoutParams lp = mcv.getLayoutParams();
                    lp.height = height3 / 3;
                    mcv.setLayoutParams(lp);
                    // todo layout params powinny sie zmieniac podczas rotacji

                    mcv.setVisibility(View.VISIBLE);
//
                    Drawable d1 = item.getIcon();
                    d1.setTint(getColor(R.color.colorPrimaryDark));
                    item.setIcon(d1);

                    mcv.invalidate();

                    int heightMCV = mcv.getHeight();
                    MapManage.setMapOffset(0, -(heightMCV + height) / 2);
                    MapManage.selectedMarker.setAlpha(0.5f);
                    return true;
                }

                if (mcv.getVisibility() == View.VISIBLE) {
                    mcv.setVisibility(View.INVISIBLE);
                    Drawable d1 = item.getIcon();
                    d1.setTint(Color.WHITE);
                    item.setIcon(d1);
                    MapManage.setMapOffset(0, 0);
                    MapManage.selectedMarker.setVisible(false);
                    return true;
                }

                return true;
            case R.id.actDur:
                return true;
            case R.id.actSet:

                if (mcv1.getVisibility() == View.INVISIBLE) {

//                    // todo layout params powinny sie zmieniac podczas rotacji
                    mcv1.setVisibility(View.VISIBLE);
                    item.setIcon(listImgTint[appViewModel.getLastTypeInd()]);
                    mcv1.invalidate();
                    return true;
                }

                if (mcv1.getVisibility() == View.VISIBLE) {
                    mcv1.setVisibility(View.INVISIBLE);
                    item.setIcon(listImg[appViewModel.getLastTypeInd()]);
                    return true;
                }

                return true;

        }

        return false;

    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
//        MapView map = MapManage.getInstance().getMap();
//        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
//        MapView map = MapManage.getInstance().getMap();
//        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

}


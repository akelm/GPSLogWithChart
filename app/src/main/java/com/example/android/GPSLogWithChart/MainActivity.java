package com.example.android.GPSLogWithChart;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // permission codes
    public static final int ACCESS_FINE_LOCATION_PERMISSION_CODE = 101;
    private static final int INTERNET_PERMISSION_CODE = 100;

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

        MapManage.setup(this);
        setContentView(R.layout.activity_main);

        // permissions
        requestSinglePermission(Manifest.permission.INTERNET, INTERNET_PERMISSION_CODE);
        requestSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION_PERMISSION_CODE);

        MapManage.displayMap();
        ChartManage.getInstance().setup(this);

        FloatingActionButton startFab = findViewById(R.id.startFab);
        startFab.setOnClickListener(view -> appViewModel.nextState());

        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        final ActivityAdapter mAdapter = new ActivityAdapter(this);

        bottomAppBar.setOnMenuItemClickListener(this::menuSwitch);

//-------------RecyclerView---------------
        RecyclerView activitiesView = findViewById(R.id.recView);

        activitiesView.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        activitiesView.setLayoutManager(linearLayoutManager);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        activitiesView.setLayoutParams(params);

        mAdapter.setOnItemClickListener(exer -> {
            bottomAppBar.getMenu().performIdentifierAction(R.id.historyShow, 0);
            // todo ladowanie danych do wyswietlania;
            appViewModel.setCurrentExercise(exer);
            appViewModel.loadHistoryTracks();
            appViewModel.setState(AppViewModel.States.HISTORY);
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
        appViewModel.getCurrentExercise().observe(this, exer -> {
            // change chip data
            String text = "";
            if (exer != null) {
                Long start = exer.getStart();
                SimpleDateFormat sdfStart = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
                text += sdfStart.format(start);
            }
            chipSportData.setText(text);
        });

        // change sport icon
        appViewModel.getLastType().observe(this, type -> {
            // icon
            if (type != null) {
                int ind = 0;
                switch (type.getTypeName()) {
                    case "WALK":
                        ind = 0;
                        break;
                    case "RUN":
                        ind = 1;
                        break;
                    case "BIKE":
                        ind = 2;
                        break;
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
        });

        // start/stop fectching track data
        appViewModel.getGpsOk().observe(this, isGpsOk -> {
            if (appViewModel.isAppStateEqual(AppViewModel.States.GPS)) {
                if (!isGpsOk) {
                    Toast.makeText(this,
                            "Waiting for GPS data...",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        appViewModel.getTracks().observe(this, tracks -> {
            // todo mapa
            if (tracks != null && tracks.size() > 0) {
                appViewModel.setLastTrack(tracks.get(tracks.size() - 1));
            }
            ChartManage.setData(tracks);
            MapManage.setData(tracks);

        });

        // last track from current exer
        appViewModel.getLastTrack().observe(this, track -> {
            // duration on bottomappbar
            String barText;
            long seconds = 0;
            long minutes = 0;
            long hours = 0;
            if (track != null) {
                seconds = TimeUnit.SECONDS.convert(track.getTime(), TimeUnit.NANOSECONDS) % 60;
                minutes = TimeUnit.MINUTES.convert(track.getTime(), TimeUnit.NANOSECONDS) % 60;
                hours = TimeUnit.HOURS.convert(track.getTime(), TimeUnit.NANOSECONDS);
            }
            barText = String.format(Locale.ENGLISH, "%2d s", seconds);
            if (hours > 0 || minutes > 0) {
                barText = String.format(Locale.ENGLISH, "%2d min ", minutes) + barText;
            } else {
                barText = String.format("%7s", "") + barText;
            }
            if (hours > 0) {
                barText = String.format(Locale.ENGLISH, "%2d h ", hours) + barText;
            } else {
                barText = String.format("%7s", "") + barText;
            }
            bottomAppBar.getMenu().findItem(R.id.actDur).setTitle(barText);
            // adds point to map and chart
            MapManage.addTrack(track);
            ChartManage.addTrack(track);

        });

        // the very last track
        appViewModel.getAbsoluteLastTrack().observe(this, track -> appViewModel.setLastTrack(track));

        // icon of startFab, data collection, data show
        appViewModel.getState().observe(this, state -> {
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
                        if (!requestSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION,
                                ACCESS_FINE_LOCATION_PERMISSION_CODE)) {
                            return;
                        }
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
        zoominFab.setOnClickListener(view -> {
            if (MapManage.map.canZoomIn())
                MapManage.map.getController().zoomIn();
        });

        FloatingActionButton zoomoutFab = findViewById(R.id.zoomOut);
        zoomoutFab.setOnClickListener(view -> MapManage.map.getController().zoomOut());

        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
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
        });

    }


    private boolean menuSwitch(MenuItem item) {
        MaterialCardView mcv2 = findViewById(R.id.historyView);
        MaterialCardView mcv = findViewById(R.id.cardView);
        MaterialCardView mcv1 = findViewById(R.id.settingsView);

        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        int height = bottomAppBar.getHeight();

        mcv.setTranslationY(-height);
        mcv1.setTranslationY(-height);
        mcv2.setTranslationY(-height);

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

                    lp.height = height3 - (int) (2.75 * height);
                    mcv2.setLayoutParams(lp);

                    mcv2.setVisibility(View.VISIBLE);

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

                    mcv.setVisibility(View.VISIBLE);
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
        // todo
    }

    @Override
    public void onPause() {
        super.onPause();
        // todo
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case (INTERNET_PERMISSION_CODE): {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                            "Map cannot load without Internet access!",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                return;
            }
            case (ACCESS_FINE_LOCATION_PERMISSION_CODE): {
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // if app just etnered gps mode, switch to NOTHING
                    if (appViewModel.isAppStateEqual(AppViewModel.States.GPS)) {
                        appViewModel.forceState(AppViewModel.States.NOTHING);
                    }
                    Toast.makeText(this,
                            "Cannot collect GPS data!",
                            Toast.LENGTH_SHORT)
                            .show();

                }
                return;
            }
        }
    }


    public boolean requestSinglePermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this,
                permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
        }
        return ContextCompat.checkSelfPermission(this,
                permission) == PackageManager.PERMISSION_GRANTED;
    }

}


package com.lovejoy777.rroandlayersmanager.activities;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import com.bitsyko.libicons.AppIcon;
import com.bitsyko.libicons.IconPack;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.utils.IconUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IconPackDetailActivity extends AppCompatActivity implements IconUtils.Callback {

    private CheckBox dontShowAgain;
    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private IconPack iconPack;
    private Switch installEverything;
    private FloatingActionButton installationFAB;
    private CoordinatorLayout cordLayout;
    private LoadDrawables imageLoader;
    private LoadLayerApks loadLayerApks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icon_pack_details);

        getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));

        cordLayout = (CoordinatorLayout) findViewById(R.id.main_content);

        receiveIntent();

        cordLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                loadBackdrop();
            }
        });


        createLayouts();

        // Log.d("Colors", String.valueOf(iconPack.getColors()));

    }

    private boolean isAnyCheckboxEnabled() {

        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                return true;
            }
        }
        return false;
    }


    private void refreshFab() {

        if (isAnyCheckboxEnabled()) {
            installationFAB.show();
        } else {
            installationFAB.hide();
        }

    }

    private void loadOverlayCardviews() {
        loadLayerApks = new LoadLayerApks();
        loadLayerApks.execute();
    }

    private void loadScreenshotCardview() {
        loadScreenshots();
    }

    private void receiveAndUseData() {

        TextView tv_description = (TextView) cordLayout.findViewById(R.id.HeX1);
        tv_description.setText(iconPack.getDescription());

        TextView tv_whatsNew = (TextView) cordLayout.findViewById(R.id.tv_whatsNew);
        tv_whatsNew.setText(iconPack.getWhatsNew());

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) cordLayout.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(iconPack.getName());

    }

    private void createLayouts() {
        //switch to select all Checkboxes
        installEverything = (Switch) cordLayout.findViewById(R.id.allswitch);


        //Hide the FAB
        installationFAB = (FloatingActionButton) cordLayout.findViewById(R.id.fab2);
        installationFAB.hide();

        //Initialize Layout
        Toolbar toolbar = (Toolbar) cordLayout.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);
        }

        installationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installDialog();
            }
        });

        installEverything.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkall();
                } else {
                    uncheckAllCheckBoxes();
                }
            }
        });

    }

    private void receiveIntent() {
        String layerPackageName = getIntent().getStringExtra("PackageName");
        iconPack = new IconPack(this, layerPackageName);
        Log.d("PackageName: ", layerPackageName);
    }

    private void loadBackdrop() {

        ImageView imageView = (ImageView) cordLayout.findViewById(R.id.backdrop);
        ImageView imageView2 = (ImageView) cordLayout.findViewById(R.id.backdropsmall);

        // Drawable promo = iconPack.getPromo();

        Drawable promo = getDrawable(R.drawable.background);
        Drawable promo2 = iconPack.getIcon();

        imageView.setImageDrawable(promo);

        imageView2.setImageDrawable(promo2);

        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) cordLayout.findViewById(R.id.collapsing_toolbar);

        Palette.from(((BitmapDrawable) promo2).getBitmap()).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch != null) {
                    collapsingToolbar.setContentScrimColor(vibrantSwatch.getRgb());
                    float[] hsv = new float[3];
                    Color.colorToHSV(vibrantSwatch.getRgb(), hsv);
                    hsv[2] *= 0.8f;
                    collapsingToolbar.setStatusBarScrimColor(Color.HSVToColor(hsv));
                }
            }
        });
        Animator reveal = ViewAnimationUtils.createCircularReveal(imageView,
                imageView.getWidth() / 2,
                imageView.getHeight() / 2,
                0,
                imageView.getHeight() * 2);
        reveal.setDuration(750);
        reveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                loadScreenshotCardview();
                receiveAndUseData();
                //  Animation fadeInAnimation = AnimationUtils.loadAnimation(OverlayDetailActivity.this, R.anim.fadein);
                LinearLayout WN = (LinearLayout) cordLayout.findViewById(R.id.lin2);
                WN.setVisibility(View.VISIBLE);
                // WN.startAnimation(fadeInAnimation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                loadOverlayCardviews();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        reveal.start();
    }

    private void loadBackdrop2() {
        final ImageView imageView = (ImageView) cordLayout.findViewById(R.id.backdrop);
        imageView.setBackgroundResource(R.drawable.no_heroimage);
    }

    private void loadScreenshots() {
        imageLoader = new LoadDrawables();
        imageLoader.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uncheckAllCheckBoxes() {

        for (CheckBox checkBox : checkBoxes) {

            if (checkBox.isChecked()) {
                checkBox.performClick();
            }

        }

        refreshFab();

    }

    private void checkall() {

        for (CheckBox checkBox : checkBoxes) {
            if (!checkBox.isChecked() && checkBox.isEnabled()) {
                checkBox.performClick();
            }
        }

        refreshFab();

    }


    ///////////
    //Snackbars

    private void installationFinishedSnackBar() {

        //show SnackBar after successful installation of the overlays
        final View coordinatorLayoutView = cordLayout.findViewById(R.id.main_content);
        Snackbar.make(coordinatorLayoutView, R.string.OverlaysInstalled, Snackbar.LENGTH_LONG)
                .setAction(R.string.Reboot, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Commands.reboot(IconPackDetailActivity.this);
                    }
                })
                .show();
    }


    /////////
    //Dialogs
    private void installDialog() {

        //if (showInstallationConfirmDialog()) {
        AlertDialog.Builder installdialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();
        View dontShowAgainLayout = View.inflate(this, R.layout.dialog_donotshowagain, null);
        dontShowAgain = (CheckBox) dontShowAgainLayout.findViewById(R.id.skip);

        installdialog.setView(dontShowAgainLayout);
        installdialog.setTitle(R.string.MoveOverlays);
        installdialog.setMessage(R.string.ApplyOverlays);
        installdialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (dontShowAgain.isChecked()) {
                    SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = myprefs.edit();
                    editor.putString("ConfirmInstallationDialog", "checked");
                    editor.apply();
                }

                //start async task to install the Overlays
                InstallAsyncOverlays();
            }
        });
        installdialog.setNegativeButton(android.R.string.cancel, null);

        SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String skipMessage = myprefs.getString("ConfirmInstallationDialog", "unchecked");
        if (!skipMessage.equals("checked")) {
            installdialog.show();
        } else {
            InstallAsyncOverlays();
        }
    }

    private void InstallAsyncOverlays() {

        List<AppIcon> iconsToInstall = new ArrayList<>();

        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                iconsToInstall.add((AppIcon) checkBox.getTag());
            }
        }

        IconUtils.installIcons(this, iconsToInstall, this);
    }

    @Override
    public void onInstallFinish() {
        installationFinishedSnackBar();
        uncheckAllCheckBoxes();
        installEverything.setChecked(false);
    }

    @Override
    public void onDestroy() {
        loadBackdrop2();
        if (imageLoader.getStatus() != AsyncTask.Status.FINISHED) {
            imageLoader.cancel(true);
        }

        if (loadLayerApks.getStatus() != AsyncTask.Status.FINISHED) {
            loadLayerApks.cancel(true);
        }

        super.onDestroy();
    }

    private class LoadDrawables extends AsyncTask<Void, Drawable, Void> {

        LinearLayout screenshotLayout;

        @Override
        protected void onPreExecute() {
            screenshotLayout = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutScreenshots);
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<Drawable> drawables = iconPack.getPreviewImages();

            if (drawables != null) {
                for (Drawable drawable : drawables) {
                    publishProgress(drawable);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Drawable... values) {

            for (Drawable screenshot : values) {
                if (screenshot == null) continue;

                ImageView screenshotImageView;

                try {
                    screenshotImageView = new ImageView(IconPackDetailActivity.this);
                } catch (NullPointerException e) {
                    continue;
                }

                Bitmap bitmap = ((BitmapDrawable) screenshot).getBitmap();

                //TODO: Rewrite
                if (bitmap.getHeight() > 1000) {
                    screenshotImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.4), (int) (bitmap.getHeight() * 0.4), true));
                } else {
                    screenshotImageView.setImageBitmap(bitmap);
                }

                LinearLayout linear = new LinearLayout(IconPackDetailActivity.this);

                linear.addView(screenshotImageView);
                screenshotLayout.addView(linear);
            }

        }


    }


    private class LoadLayerApks extends AsyncTask<Void, Void, List<AppIcon>> {

        LinearLayout screenshotLayout;

        @Override
        protected void onPreExecute() {
            screenshotLayout = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutScreenshots);
        }

        @Override
        protected List<AppIcon> doInBackground(Void... params) {
            List<AppIcon> apps = iconPack.getCompatibleApps();

            Collections.sort(apps, new Comparator<AppIcon>() {
                @Override
                public int compare(AppIcon lhs, AppIcon rhs) {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                }
            });

            return apps;

        }

        @Override
        protected void onPostExecute(List<AppIcon> list) {
            //   super.onPostExecute(list);

            LinearLayout linearLayout1 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory1);
            LinearLayout linearLayout2 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory2);

            for (AppIcon app : list) {

                TableRow row = new TableRow(IconPackDetailActivity.this);
                row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                CheckBox check = new CheckBox(IconPackDetailActivity.this);

                String text = (app.getName() + " (" + app.getPackageName() + ")");
                check.setText(text);
                check.setTag(app);

                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        refreshFab();
                    }
                });

                row.addView(check);

                checkBoxes.add(check);

                //if (app.isInPack()) {
                  //  linearLayout1.addView(row);
                //} else {
                  //  linearLayout2.addView(row);
                //}

            }

            linearLayout1.invalidate();
            linearLayout2.invalidate();

            //cordLayout.findViewById(R.id.CardViewCategory2).setVisibility(View.GONE);

        }
    }


}
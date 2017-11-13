package ru.ra66it.updaterforspotify.mvp.presenter;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.ra66it.updaterforspotify.QueryPreferneces;
import ru.ra66it.updaterforspotify.R;
import ru.ra66it.updaterforspotify.model.Spotify;
import ru.ra66it.updaterforspotify.mvp.view.BaseViewFragment;
import ru.ra66it.updaterforspotify.rest.SpotifyApi;
import ru.ra66it.updaterforspotify.utils.UtilsDownloadSpotify;
import ru.ra66it.updaterforspotify.utils.UtilsNetwork;
import ru.ra66it.updaterforspotify.utils.UtilsSpotify;


/**
 * Created by 2Rabbit on 11.11.2017.
 */

@InjectViewState
public class SpotifyOriginPresenter extends MvpPresenter<BaseViewFragment> {

    private String latestLink;
    private String latestVersionName;
    private String latestVersionNumber;
    private String installVersion;

    private boolean hasError = false;

    public SpotifyOriginPresenter() {

    }

    public void getLatestVersionSpotify(Context context) {
        if (QueryPreferneces.isSpotifyBeta(context)) {
            loadDataOrigin(context);
        } else {
            loadDataBeta(context);
        }
    }

    public void loadDataBeta(Context context) {
        if (UtilsNetwork.isNetworkAvailable(context)) {

            errorLayout(false);
            getViewState().showCardProgress();
            latestVersionNumber = "0.0.0.0";

            SpotifyApi.Factory.getInstance().getLatestOriginBeta().enqueue(new Callback<Spotify>() {
                @Override
                public void onResponse(Call<Spotify> call, Response<Spotify> response) {
                    try {
                        latestLink = response.body().getBody();
                        latestVersionNumber = response.body().getTagName();
                        latestVersionName = response.body().getName();
                        fillData(context);

                    } catch (NullPointerException e) {
                        errorLayout(true);
                        getViewState().showErrorSnackbar(R.string.error);
                    }


                    getViewState().hideCardProgress();
                }

                @Override
                public void onFailure(Call<Spotify> call, Throwable t) {
                    errorLayout(true);
                    getViewState().hideCardProgress();
                }
            });

        } else {
            errorLayout(true);
            getViewState().showErrorSnackbar(R.string.no_internet_connection);
        }
    }

    public void loadDataOrigin(Context context) {
        if (UtilsNetwork.isNetworkAvailable(context)) {

            errorLayout(false);
            getViewState().showCardProgress();
            latestVersionNumber = "0.0.0.0";

            SpotifyApi.Factory.getInstance().getLatestOrigin().enqueue(new Callback<Spotify>() {
                @Override
                public void onResponse(Call<Spotify> call, Response<Spotify> response) {
                    try {
                        latestLink = response.body().getBody();
                        latestVersionNumber = response.body().getTagName();
                        latestVersionName = response.body().getName();
                        fillData(context);

                    } catch (NullPointerException e) {
                        errorLayout(true);
                        getViewState().showErrorSnackbar(R.string.error);
                    }


                    getViewState().hideCardProgress();
                }

                @Override
                public void onFailure(Call<Spotify> call, Throwable t) {
                    errorLayout(true);
                    getViewState().hideCardProgress();
                }
            });

        } else {
            errorLayout(true);
            getViewState().showErrorSnackbar(R.string.no_internet_connection);
        }
    }


    public void downloadLatestVersion(Context context) {
        UtilsDownloadSpotify.downloadSpotify(context, latestLink, latestVersionName);
    }


    public void fillData(Context context) {
        if (!latestVersionNumber.equals("0.0.0.0")) {
            getViewState().setLatestVersionAvailable(latestVersionNumber);
            if (UtilsSpotify.isSpotifyInstalled(context) &&
                    UtilsSpotify.isSpotifyUpdateAvailable(installVersion, latestVersionNumber)) {
                // Install new version
                getViewState().showFAB();
                getViewState().showCardView();

            } else if (!UtilsSpotify.isSpotifyInstalled(context)) {
                // Install spotify now
                getViewState().showFAB();
                getViewState().showCardView();

            } else if (UtilsSpotify.isDogFoodInstalled(context)) {
                getViewState().showFAB();
                getViewState().showCardView();

            } else {
                //have latest version
                getViewState().hideFAB();
                getViewState().hideCardView();
                getViewState().setInstalledVersion(context.getString(R.string.up_to_date));
            }

        }

    }

    public void checkInstalledSpotifyVersion(Context context, FloatingActionButton fab) {
        if (UtilsSpotify.isSpotifyInstalled(context)) {
            installVersion = UtilsSpotify.getInstalledSpotifyVersion(context);
            getViewState().setInstalledVersion(installVersion);
            if (!UtilsSpotify.isDogFoodInstalled(context)) {
                fab.setImageResource(R.drawable.ic_autorenew_black_24dp);
            }
            fillData(context);
        } else {
            getViewState().showFAB();
            fab.setImageResource(R.drawable.ic_file_download_black_24dp);
            getViewState().setInstalledVersion(context.getString(R.string.spotify_not_installed));
            if (hasError) {
                getViewState().hideFAB();
            }
        }

    }

    public void errorLayout(boolean bool) {
        if (bool) {
            hasError = true;
            getViewState().hideLayoutCards();
            getViewState().hideFAB();
        } else {
            hasError = false;
            getViewState().showLayoutCards();
            getViewState().hideFAB();
        }

    }


}
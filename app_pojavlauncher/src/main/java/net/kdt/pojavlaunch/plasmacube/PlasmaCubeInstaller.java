package net.kdt.pojavlaunch.plasmacube;

import android.util.Log;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.progresskeeper.DownloaderProgressWrapper;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.ZipUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipFile;

/**
 * Installe automatiquement le modpack PlasmaCube au premier lancement :
 * telecharge le zip du pack depuis GitHub, l'extrait dans .minecraft,
 * puis cree/selectionne le profil Fabric correspondant.
 */
public final class PlasmaCubeInstaller {
    private static final String TAG = "PlasmaCubeInstaller";

    public static final String PACK_VERSION = "1.0";
    public static final String PACK_URL =
            "https://github.com/0xVenoth/PlasmaCube-modpack/releases/download/v1.0/PlasmaCube-modpack-1.0.zip";
    public static final String PROFILE_NAME = "PlasmaCube";
    public static final String VERSION_ID = "fabric-loader-0.19.3-1.21.1";

    private static final AtomicBoolean sRunning = new AtomicBoolean(false);

    private PlasmaCubeInstaller() {}

    /** A appeler une fois le stockage initialise (LauncherActivity.onCreate). */
    public static void installIfNeeded() {
        if (!sRunning.compareAndSet(false, true)) return;
        File marker = new File(Tools.DIR_GAME_NEW, "plasmacube_pack_version");
        String installed = null;
        if (marker.canRead()) {
            try {
                installed = Tools.read(marker).trim();
            } catch (IOException ignored) {}
        }
        if (PACK_VERSION.equals(installed)) {
            ensureProfile();
            sRunning.set(false);
            return;
        }
        ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.plasmacube_downloading, 0.0, 0.0);
        PojavApplication.sExecutorService.execute(() -> {
            File packZip = new File(Tools.DIR_CACHE, "plasmacube-pack.zip");
            try {
                DownloadUtils.downloadFileMonitored(PACK_URL, packZip, new byte[32768],
                        new DownloaderProgressWrapper(R.string.plasmacube_downloading,
                                ProgressLayout.INSTALL_MODPACK));
                ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.plasmacube_extracting);
                File gameDir = new File(Tools.DIR_GAME_NEW);
                try (ZipFile zipFile = new ZipFile(packZip)) {
                    ZipUtils.zipExtract(zipFile, "", gameDir);
                }
                Tools.write(marker.getAbsolutePath(), PACK_VERSION);
                ensureProfile();
                Log.i(TAG, "Modpack " + PACK_VERSION + " installe");
            } catch (IOException e) {
                Log.e(TAG, "Echec de l'installation du modpack, reessaiera au prochain lancement", e);
            } finally {
                //noinspection ResultOfMethodCallIgnored
                packZip.delete();
                ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
                sRunning.set(false);
            }
        });
    }

    /** Garantit l'existence du profil PlasmaCube et le selectionne. */
    private static void ensureProfile() {
        try {
            LauncherProfiles.load();
            String key = null;
            for (Map.Entry<String, MinecraftProfile> entry : LauncherProfiles.mainProfileJson.profiles.entrySet()) {
                MinecraftProfile profile = entry.getValue();
                if (profile != null && PROFILE_NAME.equals(profile.name)) {
                    profile.lastVersionId = VERSION_ID;
                    key = entry.getKey();
                    break;
                }
            }
            if (key == null) {
                MinecraftProfile profile = new MinecraftProfile();
                profile.name = PROFILE_NAME;
                profile.lastVersionId = VERSION_ID;
                key = LauncherProfiles.getFreeProfileKey();
                LauncherProfiles.mainProfileJson.profiles.put(key, profile);
            }
            LauncherProfiles.write();
            LauncherPreferences.DEFAULT_PREF.edit()
                    .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, key)
                    .apply();
        } catch (Throwable th) {
            Log.e(TAG, "Impossible de configurer le profil PlasmaCube", th);
        }
    }
}

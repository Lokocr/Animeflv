package knf.animeflv;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jordy on 16/08/2015.
 */
public class Conf_fragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    Context context;
    MediaPlayer mp;
    MediaPlayer r;
    MediaPlayer oni;
    MediaPlayer sam;
    MediaPlayer dango;
    private FragmentActivity myContext;
    MaterialDialog dialog;
    Login login = new Login();
    //Ringtone r;
    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);
        context=getActivity();
        Boolean activado= PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("notificaciones", true);
        if (!activado){
            getPreferenceScreen().findPreference("tiempo").setEnabled(false);
            getPreferenceScreen().findPreference("sonido").setEnabled(false);
        }else {
            getPreferenceScreen().findPreference("tiempo").setEnabled(true);
            getPreferenceScreen().findPreference("sonido").setEnabled(true);
        }
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        r = MediaPlayer.create(context, notification);
        mp = MediaPlayer.create(context, R.raw.sound);
        oni = MediaPlayer.create(context, R.raw.onii);
        sam = MediaPlayer.create(context, R.raw.sam);
        dango = MediaPlayer.create(context, R.raw.dango);
        final File file = new File(Environment.getExternalStorageDirectory()+"/Animeflv/download");
        long size = getcachesize();
        long dirsize=getFileSize(file);
        String tamano=formatSize(size);
        String vidsize=formatSize(dirsize);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("b_cache", tamano).commit();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("b_video", vidsize).commit();
        getPreferenceScreen().findPreference("b_cache").setSummary("Tamaño de cache: " + tamano);
        getPreferenceScreen().findPreference("b_cache").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clearApplicationData();
                String s = formatSize(getcachesize());
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("b_cache", s).commit();
                getPreferenceScreen().findPreference("b_cache").setSummary("Tamaño de cache: " + s);
                return false;
            }
        });
        getPreferenceScreen().findPreference("b_video").setSummary("Espacio usado: " + vidsize);
        getPreferenceScreen().findPreference("b_video").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new MaterialDialog.Builder(myContext)
                        .title("ELIMINAR")
                        .titleGravity(GravityEnum.CENTER)
                        .content("Desea eliminar TODOS los animes descargados?")
                        .positiveText("SI")
                        .negativeText("CANCELAR")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                deleteDownload(file);
                                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                                String[] eids = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("teids", "").split(":::");
                                for (String s : eids) {
                                    if (!s.trim().equals("")) {
                                        long l = Long.parseLong(context.getSharedPreferences("data", Context.MODE_PRIVATE).getString(s, "0"));
                                        manager.remove(l);
                                    }
                                }
                                String si = formatSize(getFileSize(file));
                                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("b_video", si).commit();
                                getPreferenceScreen().findPreference("b_video").setSummary("Espacio usado: " + si);
                            }
                        }).build().show();

                return false;
            }
        });
        com.lb.material_preferences_library.custom_preferences.SwitchPreference switchPreference = (com.lb.material_preferences_library.custom_preferences.SwitchPreference) findPreference("streaming");
        if (!isMXInstaled()) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("streaming", false).apply();
            switchPreference.setChecked(false);
        }
        getPreferenceScreen().findPreference("b_log").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ChangelogDialog.create()
                        .show(myContext.getSupportFragmentManager(), "changelog");

                return false;
            }
        });
        String login_email=PreferenceManager.getDefaultSharedPreferences(context).getString("login_email","null");
        if (!login_email.equals("null")){
            getPreferenceScreen().findPreference("login").setSummary(login_email);
        }
        getPreferenceScreen().findPreference("login").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Login.create().show(myContext.getSupportFragmentManager(),"Login");
                //Login.create().show(myContext.getSupportFragmentManager(), "login");
                if (isNetworkAvailable()) {
                    //login = Login.create();
                    login.show(myContext.getSupportFragmentManager(), "login");
                }else {
                    Toast.makeText(getActivity(),"Necesitas Internet!!!",Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        getPreferenceScreen().findPreference("b_vistos").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String vistos=context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("vistos","");
                String[] array=vistos.split(":::");
                for (String s:array){
                    context.getSharedPreferences("data",Context.MODE_PRIVATE).edit().putBoolean(s, false).apply();
                }
                context.getSharedPreferences("data", Context.MODE_PRIVATE).edit().putString("vistos","");
                Toast.makeText(getActivity(), "Historial Borrado!!", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        if (getSD1()==null){
            getPreferenceScreen().findPreference("b_move").setEnabled(false);
            getPreferenceScreen().findPreference("b_move").setSummary("Tarjeta SD no encontrada");
        }
        getPreferenceScreen().findPreference("b_move").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DownloadManager downloadManager=(DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor c = downloadManager.query(new DownloadManager.Query()
                        .setFilterByStatus(DownloadManager.STATUS_PAUSED
                                | DownloadManager.STATUS_RUNNING));
                    if (getSD1()!=null) {
                        //try {MoveFiles();} catch (IOException e) {Toast.makeText(context, "Error " + e.getMessage(), Toast.LENGTH_LONG).show();}
                        Toast.makeText(context, "No descargues ni reproduzcas anime hasta que la operacion se complete (No hay problema con Streaming))", Toast.LENGTH_LONG).show();
                        new MoveFiles(context).execute();
                        //context.startActivity(new Intent(context,Move.class));
                    } else {
                        Toast.makeText(context, "No se detecta tarjeta SD", Toast.LENGTH_LONG).show();
                    }
                return false;
            }
        });
    }
    public long getcachesize(){
        long size = 0;
        File[] files = context.getCacheDir().listFiles();
        File[] mediaStorage = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache").listFiles();
        for (File f:files) {
            size = size+f.length();
        }
        for (File f1:mediaStorage){
            size = size+f1.length();
        }
        return size;
    }
    public void deleteDownload(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                deleteDir(new File(dir, children[i]));
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        new Parser().saveBackup(context);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("Preference", key);
       switch (key){
           case "notificaciones":
               Boolean activado=sharedPreferences.getBoolean(key,true);
               if (!activado){
                   getPreferenceScreen().findPreference("tiempo").setEnabled(false);
                   getPreferenceScreen().findPreference("sonido").setEnabled(false);
                   new Alarm().CancelAlarm(context);
               }else {
                   getPreferenceScreen().findPreference("tiempo").setEnabled(true);
                   getPreferenceScreen().findPreference("sonido").setEnabled(true);
                   new Alarm().SetAlarm(context);
               }
               break;
           case "tiempo":
               int tiempo=Integer.parseInt(sharedPreferences.getString(key, "60000"));
               new Alarm().CancelAlarm(context);
               new Alarm().SetAlarm(context,tiempo);
               break;
           case "sonido":
               int not=Integer.parseInt(sharedPreferences.getString("sonido","0"));
               if (not==0){
                   if (mp.isPlaying()||oni.isPlaying()||sam.isPlaying()||dango.isPlaying()){
                       if (mp.isPlaying())mp.stop();
                       if (oni.isPlaying())oni.stop();
                       if (sam.isPlaying())sam.stop();
                       if (dango.isPlaying())dango.stop();
                   }
                   Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                   r = MediaPlayer.create(context, notification);
                   r.start();
               }
               if (not==1){
                   if (r.isPlaying()||oni.isPlaying()||sam.isPlaying()||dango.isPlaying()){
                       if (r.isPlaying())r.stop();
                       if (oni.isPlaying())oni.stop();
                       if (sam.isPlaying())sam.stop();
                       if (dango.isPlaying())dango.stop();
                   }
                   mp = MediaPlayer.create(context, R.raw.sound);
                   mp.start();
               }
               if (not==2){
                   if (r.isPlaying()||mp.isPlaying()||sam.isPlaying()||dango.isPlaying()){
                       if (r.isPlaying())r.stop();
                       if (mp.isPlaying())mp.stop();
                       if (sam.isPlaying())sam.stop();
                       if (dango.isPlaying())dango.stop();
                   }
                   oni = MediaPlayer.create(context, R.raw.onii);
                   oni.start();
               }
               if (not==3){
                   if (r.isPlaying()||mp.isPlaying()||oni.isPlaying()||dango.isPlaying()){
                       if (r.isPlaying())r.stop();
                       if (mp.isPlaying())mp.stop();
                       if (oni.isPlaying())oni.stop();
                       if (dango.isPlaying())dango.stop();
                   }
                   sam = MediaPlayer.create(context, R.raw.sam);
                   sam.start();
               }
               if (not==4){
                   if (r.isPlaying()||mp.isPlaying()||sam.isPlaying()||oni.isPlaying()){
                       if (r.isPlaying())r.stop();
                       if (mp.isPlaying())mp.stop();
                       if (oni.isPlaying())oni.stop();
                       if (sam.isPlaying())sam.stop();
                   }
                   dango = MediaPlayer.create(context, R.raw.dango);
                   dango.start();
               }
               break;
           case "b_cache":
               break;
           case "nCuenta_Status":
               String status=sharedPreferences.getString("nCuenta_Status","NULL");
               switch (status){
                   case "exito":
                       sharedPreferences.edit().putString("nCuenta_Status","NEUTRAL").apply();
                       login.dismiss();
                       Toast.makeText(getActivity(), "Usuario Creado!!", Toast.LENGTH_SHORT).show();
                       String login_email=PreferenceManager.getDefaultSharedPreferences(context).getString("login_email","null");
                       getPreferenceScreen().findPreference("login").setSummary(login_email);
                       break;
                   case "error":
                       sharedPreferences.edit().putString("nCuenta_Status","NEUTRAL").apply();
                       login.dismiss();
                       Toast.makeText(getActivity(),"Error!!",Toast.LENGTH_SHORT).show();
                       break;
                   case "existe":
                       sharedPreferences.edit().putString("nCuenta_Status","NEUTRAL").apply();
                       login.dismiss();
                       Toast.makeText(getActivity(),"Usuario ya existe!!",Toast.LENGTH_SHORT).show();
                       break;
               }
               break;
           case "GET_Status":
               String state=sharedPreferences.getString("GET_Status","NEUTRAL");
               Log.d("GET_STATUS",state);
               switch (state.trim()){
                   case "contraseña":
                       login.LoginErrors(1);
                       break;
                   case "noexiste":
                       login.LoginErrors(2);
                       break;
               }
               break;
           case "cCorreo_Status":
               String Cstate=sharedPreferences.getString("cCorreo_Status","NEUTRAL");
               Log.d("cCorreo_STATUS",Cstate);
               switch (Cstate.trim()){
                   case "contraseña":
                       login.cCorreoErrors(1);
                       break;
                   case "noexiste":
                       login.cCorreoErrors(2);
                       break;
               }
               break;
           case "cPass_Status":
               String CPstate=sharedPreferences.getString("cPass_Status","NEUTRAL");
               Log.d("cPass_STATUS",CPstate);
               switch (CPstate.trim()){
                   case "contraseña":
                       login.cPassErrors(1);
                       break;
                   case "noexiste":
                       login.cPassErrors(2);
                       break;
               }
               break;
           case "login_email":
               String login_email=PreferenceManager.getDefaultSharedPreferences(context).getString("login_email","null");
               if (!login_email.equals("null")) {
                   getPreferenceScreen().findPreference("login").setSummary(login_email);
               }else {
                   getPreferenceScreen().findPreference("login").setSummary("Iniciar Sesion");
               }
               break;
           case "streaming":
               com.lb.material_preferences_library.custom_preferences.SwitchPreference switchPreference=(com.lb.material_preferences_library.custom_preferences.SwitchPreference)findPreference("streaming");
               if (!isMXInstaled()&&sharedPreferences.getBoolean("streaming",false)){
                   sharedPreferences.edit().putBoolean("streaming",false).apply();
                   switchPreference.setChecked(false);
                   Toast.makeText(context,"MX Player no esta instalado",Toast.LENGTH_SHORT).show();
               }
       }
    }
    public Boolean isMXInstaled(){
        Boolean is=false;
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.mxtech.videoplayer.pro")) {
                is=true;
                break;
            }
            if (packageInfo.packageName.equals("com.mxtech.videoplayer.ad")) {
                is=true;
                break;
            }
        }
        return is;
    }
    private boolean isNetworkAvailable() {
        Boolean net=false;
        int Tcon=Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("t_conexion", "0"));
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        switch (Tcon){
            case 0:
                NetworkInfo Wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                net=Wifi.isConnected();
                break;
            case 1:
                NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                net=mobile.isConnected();
                break;
            case 2:
                NetworkInfo WifiA = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo mobileA = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                net=WifiA.isConnected()||mobileA.isConnected();
                break;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && net;
    }
    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
    public void clearApplicationData() {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                }
            }
        }
        File mediaStorage = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache");
        if (mediaStorage.isDirectory()) {
            String[] children = mediaStorage.list();
            for (int i = 0; i < children.length; i++) {
                if (!children[i].equals("directorio.txt") && !children[i].equals("data.save"))
                new File(mediaStorage, children[i]).delete();
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    public static long getFileSize(final File file) {
        if(file==null||!file.exists())
            return 0;
        if(!file.isDirectory())
            return file.length();
        final List<File> dirs=new LinkedList<File>();
        dirs.add(file);
        long result=0;
        while(!dirs.isEmpty()) {
            final File dir=dirs.remove(0);
            if(!dir.exists())
                continue;
            final File[] listFiles=dir.listFiles();
            if(listFiles==null||listFiles.length==0)
                continue;
            for(final File child : listFiles)
            {
                result+=child.length();
                if(child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }
    public String getSD(){
        String sd = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (android.os.Build.DEVICE.contains("samsung") || android.os.Build.MANUFACTURER.contains("samsung")) {
            File f = new File(Environment.getExternalStorageDirectory().getParent() + "/extSdCard");
            if (f.exists() && f.isDirectory()) {
                sd = Environment.getExternalStorageDirectory().getParent() + "/extSdCard";
            } else {
                f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/external_sd");
                if (f.exists() && f.isDirectory()) {
                    sd = Environment.getExternalStorageDirectory().getAbsolutePath() + "/external_sd";
                }else {
                    f = new File(Environment.getExternalStorageDirectory().getParent() + "/sdcard1");
                    if (f.exists() && f.isDirectory()) {
                        sd = Environment.getExternalStorageDirectory().getParent() + "/sdcard1";
                    }else {
                        sd="NoSD";
                    }
                }
            }
        }else {
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/external_sd");
            if (f.exists() && f.isDirectory()) {
                sd = Environment.getExternalStorageDirectory().getAbsolutePath() + "/external_sd";
            }else {
                f = new File(Environment.getExternalStorageDirectory().getParent() + "/sdcard1");
                if (f.exists() && f.isDirectory()) {
                    sd = Environment.getExternalStorageDirectory().getParent() + "/sdcard1";
                }else {
                    sd="NoSD";
                }
            }
        }
        Toast.makeText(context,sd,Toast.LENGTH_LONG).show();
        return sd;
    }
    public String getSD1(){
        String sSDpath = null;
        File   fileCur = null;
        for( String sPathCur : Arrays.asList("MicroSD", "external_SD", "sdcard1", "ext_card", "external_sd", "ext_sd", "external", "extSdCard", "externalSdCard")) {
            fileCur = new File( "/mnt/", sPathCur);
            if( fileCur.isDirectory() && fileCur.canWrite()) {
                sSDpath = fileCur.getAbsolutePath();
                break;
            }
            if( sSDpath == null)  {
                fileCur = new File( "/storage/", sPathCur);
                if( fileCur.isDirectory() && fileCur.canWrite())
                {
                    sSDpath = fileCur.getAbsolutePath();
                    break;
                }
            }
            if( sSDpath == null)  {
                fileCur = new File( "/storage/emulated", sPathCur);
                if( fileCur.isDirectory() && fileCur.canWrite())
                {
                    sSDpath = fileCur.getAbsolutePath();
                    Log.e("path",sSDpath);
                    break;
                }
            }
        }
        return sSDpath;
    }
    public void MoveFiles() throws IOException {
        File sourceLocation=new File(Environment.getExternalStorageDirectory() + "/Animeflv/download");
        File targetLocation=new File(getSD() + "/Animeflv/download");
        ProgressDialog dialog=ProgressDialog.show(context,"","Por favor espere...",true,false);
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }
            String[] children = sourceLocation.list();
            for (String i:children) {
                File scan=new File(sourceLocation,i);
                if (scan.isDirectory()) {
                    String[] archivos = scan.list();
                    for (String mp4:archivos){
                        File inSD=new File(targetLocation,i);
                        if (!inSD.exists()){
                            scan.mkdir();
                        }
                        InputStream in = new FileInputStream(new File(scan,mp4));
                        OutputStream out = new FileOutputStream(new File(inSD,mp4));
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        in.close();
                        out.close();
                        File file=new File(inSD,mp4);
                        if (file.exists()){
                            if (new File(scan,mp4).delete()){
                                Log.d("Move ok",mp4);
                            }
                        }
                    }

                }
            }
            dialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mp.isPlaying()){
            mp.stop();
            mp.release();
        }
        if (r.isPlaying()){
            r.stop();
            r.release();
        }
        if (oni.isPlaying()){
            oni.stop();
            oni.release();
        }
        if (sam.isPlaying()){
            sam.stop();
            sam.release();
        }
        if (dango.isPlaying()){
            dango.stop();
            dango.release();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }
}

package com.infinimesh;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.Locale;
import java.util.concurrent.*;
import io.github.gretchycat.infinemesh.*;
import io.github.gretchycat.infinemesh.android.MeshView;

public final class MeshActivity extends Activity {
    private static final int OPEN=100;
    private final ExecutorService loader=Executors.newSingleThreadExecutor();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private MeshView viewer;
    private TextView info,clock;
    private LinearLayout playback;
    private SeekBar seek;
    private Button play;
    private SharedPreferences prefs;
    private Future<?> pending;
    private int generation;
    private ModelDocument document;
    private String documentName="",location;
    private boolean playing,updatingSeek,destroyed;
    private double seconds,speed=10;
    private long lastTick;
    private final Runnable tick=new Runnable() {
        public void run() {
            if(!playing || document==null || document.toolpath==null) return;
            long now=SystemClock.elapsedRealtime();seconds+=(now-lastTick)/1000.0*speed;lastTick=now;
            if(seconds>=document.toolpath.durationSeconds()) { seconds=document.toolpath.durationSeconds();pause(); }
            updateTime();
            if(playing) handler.postDelayed(this,33);
        }
    };
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);prefs=getSharedPreferences("viewer",MODE_PRIVATE);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(6),0,dp(6),0);
        setContentView(root);
        // Keep controls clear of display cutouts, system bars and gesture navigation.
        root.setOnApplyWindowInsetsListener((v,insets)-> {
            v.setPadding(dp(6)+insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),dp(6)+insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;
        });
        LinearLayout top=row(root);
        Button openButton=button(top,"Open",this::open);
        openButton.setLayoutParams(new LinearLayout.LayoutParams(dp(72),dp(48)));
        Spinner shading=new Spinner(this);
        shading.setContentDescription("Model shading mode");
        shading.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Mesh / wireframe","Flat shading","Gouraud shading"}));
        top.addView(shading,new LinearLayout.LayoutParams(0,dp(48),1));
        Button menu=button(top,"⋮",this::options);
        menu.setLayoutParams(new LinearLayout.LayoutParams(dp(48),dp(48)));
        menu.setContentDescription("Viewer options and samples");
        LinearLayout resets=row(root);
        button(resets,"Reset rotation",()->viewer.resetRotation()).setLayoutParams(new LinearLayout.LayoutParams(0,dp(48),1.8f));
        button(resets,"Fit",()->viewer.resetZoom());
        button(resets,"−",()->viewer.zoomBy(0.8f)).setContentDescription("Zoom out");
        button(resets,"+",()->viewer.zoomBy(1.25f)).setContentDescription("Zoom in");
        viewer=new MeshView(this);root.addView(viewer,new LinearLayout.LayoutParams(-1,0,1));
        int mode=Math.max(0,Math.min(2,prefs.getInt("shading",2)));shading.setSelection(mode);
        viewer.setShading(MeshView.Shading.values()[mode]);
        shading.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) {}
            public void onItemSelected(AdapterView<?> parent,View view,int index,long id) {
                viewer.setShading(MeshView.Shading.values()[index]);prefs.edit().putInt("shading",index).apply();
            }
        });
        applyAppearance();
        info=new TextView(this);info.setTextSize(13);info.setPadding(dp(8),dp(6),dp(8),dp(6));
        info.setText("InfiniMesh · Open an STL or G-code file\nDrag to rotate · Pinch to zoom · Samples in ⋮");root.addView(info);
        playback=new LinearLayout(this);playback.setOrientation(LinearLayout.VERTICAL);root.addView(playback);
        clock=new TextView(this);clock.setTextSize(12);clock.setPadding(dp(8),0,0,0);playback.addView(clock);
        seek=new SeekBar(this);seek.setMax(10000);seek.setContentDescription("G-code simulation position");playback.addView(seek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar bar) { pause(); }
            public void onStopTrackingTouch(SeekBar bar) {}
            public void onProgressChanged(SeekBar bar,int progress,boolean fromUser) {
                if(fromUser && !updatingSeek && document!=null && document.toolpath!=null) {
                    seconds=document.toolpath.durationSeconds()*progress/10000;updateTime();
                }
            }
        });
        LinearLayout transport=row(playback);
        button(transport,"Restart",()->{pause();seconds=0;updateTime();});
        play=button(transport,"Play",()->{
            if(playing) pause(); else {
                if(document==null || document.toolpath==null) return;
                if(seconds>=document.toolpath.durationSeconds()) seconds=0;
                playing=true;play.setText(R.string.pause);lastTick=SystemClock.elapsedRealtime();handler.post(tick);
            }
        });
        Spinner rate=new Spinner(this);rate.setContentDescription("Simulation playback speed");
        rate.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"1×","10×","60×","300×"}));
        transport.addView(rate,new LinearLayout.LayoutParams(0,dp(48),1));rate.setSelection(1);
        rate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> p) {}
            public void onItemSelected(AdapterView<?> p,View v,int i,long id) { speed=new double[]{1,10,60,300}[i]; }
        });
        playback.setVisibility(View.GONE);
        if(saved!=null && saved.getString("location")!=null) load(saved.getString("location"),saved.getDouble("seconds",-1));
        else incoming(getIntent());
    }
    private LinearLayout row(LinearLayout parent) {
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);parent.addView(row,new LinearLayout.LayoutParams(-1,-2));return row;
    }
    private Button button(LinearLayout row,String text,Runnable action) {
        Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setTextSize(13);b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(dp(8),0,dp(8),0);
        row.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));b.setOnClickListener(v->action.run());return b;
    }
    private int dp(int value) { return Math.round(value*getResources().getDisplayMetrics().density); }
    private void open() {
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try { startActivityForResult(intent,OPEN); } catch(ActivityNotFoundException e) { error("No Android document picker is available."); }
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request==OPEN && result==RESULT_OK && data!=null && data.getData()!=null) {
            Uri uri=data.getData();
            if ((data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
                try { getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch(SecurityException ignored) {}
            }
            load(uri.toString(),-1);
        }
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent);setIntent(intent);incoming(intent); }
    private void incoming(Intent intent) {
        if(intent==null) return;
        Uri uri=intent.getData();
        if(Intent.ACTION_SEND.equals(intent.getAction())) uri=intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(uri==null && intent.getClipData()!=null && intent.getClipData().getItemCount()>0) uri=intent.getClipData().getItemAt(0).getUri();
        if(uri!=null) load(uri.toString(),-1);
    }
    private void load(String source,double resumeSeconds) {
        pause();int ticket=++generation;if(pending!=null) pending.cancel(true);
        info.setText(R.string.loading);
        pending=loader.submit(()-> {
            try {
                boolean sample=source.startsWith("sample:");Uri uri=sample?null:Uri.parse(source);
                String name=sample?source.substring(7):displayName(uri);
                String mime=sample?"":getContentResolver().getType(uri);
                ModelDocument loaded;
                try(InputStream input=sample?getAssets().open("samples/"+name):getContentResolver().openInputStream(uri)) {
                    if(input==null) throw new IOException("The document provider returned no data");
                    loaded=new ModelLoader().load(input,name,mime,DecodeLimits.DEFAULT);
                }
                if(Thread.currentThread().isInterrupted()) return;
                viewer.setDocument(loaded);
                runOnUiThread(()-> {
                    if(destroyed || generation!=ticket) return;
                    document=loaded;documentName=name;location=source;
                    seconds=loaded.toolpath==null?0:(resumeSeconds>=0?Math.min(resumeSeconds,loaded.toolpath.durationSeconds()):loaded.toolpath.durationSeconds());
                    playback.setVisibility(loaded.toolpath==null?View.GONE:View.VISIBLE);
                    info.setText(summary());updateTime();
                });
            } catch(Exception | OutOfMemoryError failure) {
                runOnUiThread(()-> {
                    if(destroyed || generation!=ticket) return;
                    info.setText(document==null?"Open an STL or G-code file to begin.":summary());
                    error(failure instanceof OutOfMemoryError?"This model is too large for available memory.":failure.getMessage());
                });
            }
        });
    }
    private String displayName(Uri uri) {
        try(Cursor cursor=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)) {
            if(cursor!=null && cursor.moveToFirst() && !cursor.isNull(0)) return cursor.getString(0);
        } catch(RuntimeException ignored) {}
        return uri.getLastPathSegment()==null?"model":uri.getLastPathSegment();
    }
    private String summary() {
        Bounds b=document.bounds();String unit=document.mesh==null?"mm":"source units";
        String count=document.mesh!=null?document.mesh.triangleCount()+" triangles":document.toolpath.segmentCount()+" segments · approximate timing";
        return documentName+" · "+document.format+"\n"+count+String.format(Locale.US," · %.2f × %.2f × %.2f %s",(double)b.maxX()-b.minX(),(double)b.maxY()-b.minY(),(double)b.maxZ()-b.minZ(),unit)+
            (document.warnings.isEmpty()?"":"\nPreview assumptions apply — see Details in ⋮");
    }
    private void updateTime() {
        if(document==null || document.toolpath==null) return;
        double duration=document.toolpath.durationSeconds();viewer.setPreviewSeconds(seconds);
        updatingSeek=true;seek.setProgress(duration==0?10000:(int)(seconds/duration*10000));updatingSeek=false;
        clock.setText(getString(R.string.preview_time,time(seconds),time(duration)));
    }
    private static String time(double value) { long s=(long)value;return String.format(Locale.US,"%d:%02d:%02d",s/3600,(s/60)%60,s%60); }
    private void pause() { playing=false;handler.removeCallbacks(tick);if(play!=null) play.setText(R.string.play); }
    private void options() {
        String[] choices={"Default background color","Default model color","Ground grid: "+(prefs.getBoolean("grid",true)?"on":"off"),"Travel moves: "+(prefs.getBoolean("travel",true)?"on":"off"),"Open sample STL","Open sample G-code","Details","Restore default colors"};
        new AlertDialog.Builder(this).setTitle("InfiniMesh").setItems(choices,(d,index)-> {
            switch(index) {
                case 0 -> chooseColor("background",0xff17212b);
                case 1 -> chooseColor("model",0xff65c9bd);
                case 2 -> { prefs.edit().putBoolean("grid",!prefs.getBoolean("grid",true)).apply();applyAppearance(); }
                case 3 -> { prefs.edit().putBoolean("travel",!prefs.getBoolean("travel",true)).apply();applyAppearance(); }
                case 4 -> load("sample:cube.stl",-1);
                case 5 -> load("sample:spiral.gcode",-1);
                case 6 -> new AlertDialog.Builder(this).setTitle("Model details").setMessage(document==null?"STL and G-code viewer. Additional model formats and texture mapping are planned. This app only views files.":summary()+"\n\n"+String.join("\n\n",document.warnings)).setPositiveButton("OK",null).show();
                case 7 -> { prefs.edit().remove("background").remove("model").apply();applyAppearance(); }
            }
        }).show();
    }
    private void chooseColor(String key,int fallback) {
        EditText field=new EditText(this);field.setSingleLine();field.setHint("#RRGGBB");
        field.setText(String.format(Locale.US,"#%06X",prefs.getInt(key,fallback)&0xffffff));field.setSelectAllOnFocus(true);
        field.setContentDescription("Color as six hexadecimal RGB digits");
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(key.equals("model")?"Default untextured model color":"Default background color")
            .setView(field).setPositiveButton("Apply",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v-> {
            String value=field.getText().toString().trim();
            if(!value.matches("#?[0-9a-fA-F]{6}")) { field.setError("Use six RGB hex digits, for example #65C9BD");return; }
            prefs.edit().putInt(key,Color.parseColor(value.startsWith("#")?value:"#"+value)).apply();applyAppearance();dialog.dismiss();
        }));dialog.show();
    }
    private void applyAppearance() {
        viewer.setColors(prefs.getInt("background",0xff17212b),prefs.getInt("model",0xff65c9bd));
        viewer.setGridVisible(prefs.getBoolean("grid",true));viewer.setTravelVisible(prefs.getBoolean("travel",true));
    }
    private void error(String message) { if(!isFinishing()&&!destroyed) new AlertDialog.Builder(this).setTitle("Unable to open model").setMessage(message==null?"The model could not be loaded.":message).setPositiveButton("OK",null).show(); }
    @Override protected void onResume() { super.onResume();viewer.onResume(); }
    @Override protected void onPause() { pause();viewer.onPause();super.onPause(); }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out);out.putString("location",location);out.putDouble("seconds",seconds); }
    @Override protected void onDestroy() { destroyed=true;++generation;if(pending!=null) pending.cancel(true);loader.shutdownNow();handler.removeCallbacksAndMessages(null);super.onDestroy(); }
}

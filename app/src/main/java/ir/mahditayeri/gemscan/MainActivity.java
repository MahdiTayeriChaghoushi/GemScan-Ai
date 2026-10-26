package ir.mahditayeri.gemscan;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends ComponentActivity {
    private FrameLayout root;
    private TextView status;
    private PreviewView preview;
    private FrameLayout markers;
    private int marked = 0;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = new FrameLayout(this);
        preview = new PreviewView(this);
        root.addView(preview, new FrameLayout.LayoutParams(-1, -1));
        markers = new FrameLayout(this);
        root.addView(markers, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(20, 25, 20, 25);
        controls.setBackgroundColor(0xCC101820);
        TextView title = new TextView(this);
        title.setText("GemScan AI | Field Preview");
        title.setTextColor(Color.WHITE); title.setTextSize(19);
        controls.addView(title);
        status = new TextView(this);
        status.setText("Tap a visible rock to mark it. Automatic gemstone detection is NOT enabled.");
        status.setTextColor(Color.WHITE);
        controls.addView(status);
        Button clear = new Button(this);
        clear.setText("Clear markers");
        clear.setOnClickListener(v -> { markers.removeAllViews(); marked = 0; status.setText("Markers cleared. Tap a rock to mark it."); });
        controls.addView(clear);
        TextView author = new TextView(this);
        author.setText("Mahdi Tayeri Chaghoushi | AI Researcher | 09051580027");
        author.setTextColor(Color.WHITE);
        controls.addView(author);
        FrameLayout.LayoutParams top = new FrameLayout.LayoutParams(-1, -2, Gravity.TOP);
        root.addView(controls, top);
        markers.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) return true;
            TextView pin = new TextView(this);
            pin.setText("◎"); pin.setTextSize(40); pin.setTextColor(Color.GREEN);
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(100, 100);
            p.leftMargin = Math.max(0, (int)event.getX()-50);
            p.topMargin = Math.max(0, (int)event.getY()-50);
            markers.addView(pin, p);
            status.setText("Manually marked rocks: " + (++marked) + " | Not a mineral identification");
            return true;
        });
        setContentView(root);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera();
        else requestPermissions(new String[]{Manifest.permission.CAMERA}, 10);
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) startCamera();
        else status.setText("Camera permission is required.");
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview p = new Preview.Builder().build();
                p.setSurfaceProvider(preview.getSurfaceProvider());
                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, p);
            } catch (Exception e) { status.setText("Camera error: " + e.getMessage()); }
        }, ContextCompat.getMainExecutor(this));
    }
}

package app.maironstudios.com.trasbankintegration;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.ingenico.pclutilities.PclUtilities;

import lombok.Getter;

@Getter
public class PosBluetooth {
    private String name;
    private String address;
    private boolean activated;

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isActivated() {
        return activated;
    }

    public PosBluetooth(PclUtilities.BluetoothCompanion btCompanion) {


        this.name = btCompanion.getBluetoothDevice().getName();
        this.address = btCompanion.getBluetoothDevice().getAddress();
        this.activated = btCompanion.isActivated();
    }
}
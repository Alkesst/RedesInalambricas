package bluetooth;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.NoSuchElementException;

public class BluetoothSocket {
    private static final Object INQUIRY_COMPLETED_EVENT = new Object();
    public static void main(String[] args) throws IOException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String bluetoothURL = searchDevices(br, agent);
        StreamConnection service = (StreamConnection) Connector.open(bluetoothURL);
        System.out.println("connected to server!");
    }

    private static String searchDevices(BufferedReader br, DiscoveryAgent agent) {
        String serverBluetoothAddress = null;
        List<RemoteDevice> cachedDevices;
        try {
            DiscoveryListener discoveryListener;
            discoveryListener = new DeviceAndServiceDiscoverer(INQUIRY_COMPLETED_EVENT);
            System.out.println("============= \tSearching near devices\t =============");
            agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
            synchronized (INQUIRY_COMPLETED_EVENT) {
                INQUIRY_COMPLETED_EVENT.wait();
            }
            ((DeviceAndServiceDiscoverer) discoveryListener).printAllDevices();
            System.out.println("Tell me the device to connect to: ");
            int devicePos = Integer.parseInt(br.readLine()) - 1;
            cachedDevices = ((DeviceAndServiceDiscoverer) discoveryListener).getCachedDevices();
            RemoteDevice device = cachedDevices.get(devicePos);
            agent.searchServices(new int[]{0x0100}, new UUID[] { new UUID(0x1002)}, device,
                    discoveryListener);
            synchronized (INQUIRY_COMPLETED_EVENT) {
                INQUIRY_COMPLETED_EVENT.wait();
            }
            System.out.println();
            try {
                serverBluetoothAddress = ((DeviceAndServiceDiscoverer) discoveryListener).allUrls().get(0).get("chat");
            } catch(NoSuchElementException a) {
                System.err.println("This device has no server....");
                System.exit(-1);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return serverBluetoothAddress;
    }


}

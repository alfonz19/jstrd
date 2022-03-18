package strd.lib.hid;

@SuppressWarnings("UnusedReturnValue")
public interface StreamDeckHandle {
    boolean isClosed();

    void close();

    void setInputReportListener(InputReportListener buttonListener);

    void setDeviceRemovalListener(DeviceRemovalListener deviceRemovalListener);

    int getFeatureReport(byte[] getSerialNumberRequest, int length);

    int setFeatureReport(byte b, byte[] resetPayload, int length);

    int setOutputReport(byte b, byte[] finalPayload, int length);

    interface InputReportListener {
        void onInputReport(byte[] reportData, int reportLength);
    }

    interface DeviceRemovalListener {
        void onDeviceRemoved();
    }
}

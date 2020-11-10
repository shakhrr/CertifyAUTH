
package com.certifyglobal.authenticator.barcodescanning;


import com.certifyglobal.authenticator.GraphicOverlay;
import com.certifyglobal.callback.Communicator;
import com.certifyglobal.pojo.FrameMetadata;
import com.certifyglobal.utils.Logger;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.IOException;
import java.util.List;

public class BarcodeScanningProcessor extends VisionProcessorBase<List<FirebaseVisionBarcode>> {

    private static final String TAG = "BarcodeScanningProcessor";
    private final FirebaseVisionBarcodeDetector detector;
    private Communicator communicator;
    private String type;

    public BarcodeScanningProcessor(Communicator communicator, String type) {
        // Note that if you know which format of barcode your app is dealing with, detection will be
        // faster to specify the supported barcode formats one by one, e.g.
         new FirebaseVisionBarcodeDetectorOptions.Builder()
             .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
             .build();
        this.communicator = communicator;
        this.type = type;
        detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Logger.error(TAG, "Exception thrown while trying to close Barcode Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionBarcode>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            List<FirebaseVisionBarcode> barcodes,
             FrameMetadata frameMetadata,
             GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        try {
            for (int i = 0; i < barcodes.size(); ++i) {
                FirebaseVisionBarcode barcode = barcodes.get(i);
                BarcodeGraphic barcodeGraphic = new BarcodeGraphic(graphicOverlay, barcode);
                graphicOverlay.add(barcodeGraphic);
              //  Logger.debug();
                if (type.equals("BioSign") || barcode.getDisplayValue() != null && barcode.getDisplayValue().contains("/ActivateMobile")) {
                    if (communicator != null)
                        communicator.setAction(barcode.getDisplayValue(), 0);
                }
            }
        } catch (Exception e) {
            Logger.error(TAG, "onSuccess " + e);
        }
    }

    @Override
    protected void onFailure(Exception e) {
        Logger.error(TAG, "Barcode detection failed " + e);
    }
}

package com.vulpesnovis.StftFilter;

import com.android.sdklib.util.SparseIntArray;

public interface FFTDataListener {
    void onDataComputed(int[] timeValues, int[] freqValues, SparseIntArray fftDataset, int magMin, int magMax);
}

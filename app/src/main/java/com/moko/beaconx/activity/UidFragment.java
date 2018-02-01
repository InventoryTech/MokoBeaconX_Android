package com.moko.beaconx.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.beaconx.R;
import com.moko.beaconx.able.ISlotDataAction;
import com.moko.beaconx.utils.ToastUtils;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.entity.TxPowerEnum;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UidFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "UidFragment";
    @Bind(R.id.et_namespace)
    EditText etNamespace;
    @Bind(R.id.et_instance_id)
    EditText etInstanceId;
    @Bind(R.id.sb_adv_interval)
    SeekBar sbAdvInterval;
    @Bind(R.id.sb_adv_tx_power)
    SeekBar sbAdvTxPower;
    @Bind(R.id.sb_tx_power)
    SeekBar sbTxPower;
    @Bind(R.id.tv_adv_interval)
    TextView tvAdvInterval;
    @Bind(R.id.tv_adv_tx_power)
    TextView tvAdvTxPower;
    @Bind(R.id.tv_tx_power)
    TextView tvTxPower;

    private SlotDataActivity activity;

    public UidFragment() {
    }

    public static UidFragment newInstance() {
        UidFragment fragment = new UidFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_uid, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        sbAdvInterval.setOnSeekBarChangeListener(this);
        sbAdvTxPower.setOnSeekBarChangeListener(this);
        sbTxPower.setOnSeekBarChangeListener(this);
        setValue();
        return view;
    }

    private void setValue() {
        if (activity.slotData.frameTypeEnum != SlotFrameTypeEnum.UID) {
            sbAdvInterval.setProgress(9);
            sbAdvTxPower.setProgress(127);
            sbTxPower.setProgress(6);
            return;
        }
        etNamespace.setText(activity.slotData.namespace);
        etInstanceId.setText(activity.slotData.instanceId);
        etNamespace.setSelection(etNamespace.getText().toString().length());
        etInstanceId.setSelection(etInstanceId.getText().toString().length());

        int advIntervalProgress = activity.slotData.advInterval / 100 - 1;
        sbAdvInterval.setProgress(advIntervalProgress);
        advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);
        tvAdvInterval.setText(String.format("%dms", activity.slotData.advInterval));

        int advTxPowerProgress = activity.slotData.rssi_0m + 127;
        sbAdvTxPower.setProgress(advTxPowerProgress);
        advTxPowerBytes = MokoUtils.toByteArray(activity.slotData.rssi_0m, 1);
        tvAdvTxPower.setText(String.format("%ddBm", activity.slotData.rssi_0m));

        int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
        sbTxPower.setProgress(txPowerProgress);
        txPowerBytes = MokoUtils.toByteArray(activity.slotData.txPower, 1);
        tvTxPower.setText(String.format("%ddBm", activity.slotData.txPower));
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    private byte[] advIntervalBytes;
    private byte[] advTxPowerBytes;
    private byte[] txPowerBytes;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_adv_interval:
                int advInterval = (progress + 1) * 100;
//                LogModule.i("advInterval:" + advInterval);
                tvAdvInterval.setText(String.format("%dms", advInterval));
                advIntervalBytes = MokoUtils.toByteArray(advInterval, 2);
                break;
            case R.id.sb_adv_tx_power:
                int advTxPower = progress - 127;
//                LogModule.i("advTxPower:" + advTxPower);
                tvAdvTxPower.setText(String.format("%ddBm", advTxPower));
                advTxPowerBytes = MokoUtils.toByteArray(advTxPower, 1);
                break;
            case R.id.sb_tx_power:
                TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
                int txPower = txPowerEnum.getTxPower();
//                LogModule.i("txPower:" + txPower);
                tvTxPower.setText(String.format("%ddBm", txPower));
                txPowerBytes = MokoUtils.toByteArray(txPower, 1);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private byte[] uidParamsBytes;

    @Override
    public boolean isValid() {
        String namespace = etNamespace.getText().toString();
        String instanceId = etInstanceId.getText().toString();
        if (TextUtils.isEmpty(namespace) || TextUtils.isEmpty(instanceId)) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (namespace.length() != 20 || instanceId.length() != 12) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        String uidParamsStr = activity.slotData.frameTypeEnum.getFrameType() + namespace + instanceId;
        uidParamsBytes = MokoUtils.hex2bytes(uidParamsStr);
        return true;
    }

    @Override
    public void sendData() {
        activity.mMokoService.sendOrder(
                // 切换通道，保证通道是在当前设置通道里
                activity.mMokoService.setSlot(activity.slotData.slotEnum),
                activity.mMokoService.setSlotData(uidParamsBytes),
                activity.mMokoService.setRadioTxPower(txPowerBytes),
                activity.mMokoService.setAdvTxPower(advTxPowerBytes),
                activity.mMokoService.setAdvInterval(advIntervalBytes)
        );
    }
}
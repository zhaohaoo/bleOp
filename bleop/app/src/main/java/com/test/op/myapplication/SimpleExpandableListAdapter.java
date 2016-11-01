/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.op.myapplication;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class SimpleExpandableListAdapter extends BaseExpandableListAdapter {
    private List<? extends Map<String, ?>> mGroupData;
    private int mExpandedGroupLayout;
    private int mCollapsedGroupLayout;
    private String[] mGroupFrom;
    private int[] mGroupTo;
    private Context context;

    private List<? extends List<? extends Map<String, ?>>> mChildData;
    private int mChildLayout;
    private int mLastChildLayout;
    private String[] mChildFrom;
    private int[] mChildTo;
    private int charaProp = 0;
    private final String LIST_CHARAPROP = "CHARAPROP";
    private Object mSendThreadLock = new Object();
    private LayoutInflater mInflater;
    public BluetoothGatt mBluetoothGatt;

    public SimpleExpandableListAdapter(Context context,
                                       List<? extends Map<String, ?>> groupData, int groupLayout,
                                       String[] groupFrom, int[] groupTo,
                                       List<? extends List<? extends Map<String, ?>>> childData,
                                       int childLayout, String[] childFrom, int[] childTo) {

        this(context, groupData, groupLayout, groupLayout, groupFrom, groupTo,
                childData, childLayout, childLayout, childFrom, childTo);
    }

    public SimpleExpandableListAdapter(Context context,
                                       List<? extends Map<String, ?>> groupData, int expandedGroupLayout,
                                       int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
                                       List<? extends List<? extends Map<String, ?>>> childData,
                                       int childLayout, String[] childFrom, int[] childTo) {
        this(context, groupData, expandedGroupLayout, collapsedGroupLayout,
                groupFrom, groupTo, childData, childLayout, childLayout,
                childFrom, childTo);
    }

    public SimpleExpandableListAdapter(Context context,
                                       List<? extends Map<String, ?>> groupData, int expandedGroupLayout,
                                       int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
                                       List<? extends List<? extends Map<String, ?>>> childData,
                                       int childLayout, int lastChildLayout, String[] childFrom,
                                       int[] childTo) {
        this.context = context;
        mGroupData = groupData;
        mExpandedGroupLayout = expandedGroupLayout;
        mCollapsedGroupLayout = collapsedGroupLayout;
        mGroupFrom = groupFrom;
        mGroupTo = groupTo;

        mChildData = childData;
        mChildLayout = childLayout;
        mLastChildLayout = lastChildLayout;
        mChildFrom = childFrom;
        mChildTo = childTo;

        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Object getChild(int groupPosition, int childPosition) {
        return mChildData.get(groupPosition).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        View v;
        ViewHolder viewHolder = null;

        if (convertView == null) {
            v = newChildView(isLastChild, parent);

        } else {
            v = convertView;
            viewHolder = (ViewHolder) v.getTag();
        }
        viewHolder = new ViewHolder();
        viewHolder.btn_r = (Button) v.findViewById(R.id.button_r);
        viewHolder.btn_w = (Button) v.findViewById(R.id.button_w);
        viewHolder.btn_n = (Button) v.findViewById(R.id.button_n);
        viewHolder.editTex_w = (EditText) v.findViewById(R.id.editTex_w);
        viewHolder.btn_w
                .setOnClickListener(new myOnclickListener(
                        ((DeviceControlActivity) context).mGattCharacteristics
                                .get(groupPosition).get(childPosition),
                        viewHolder.editTex_w));
        viewHolder.btn_r.setOnClickListener(new myOnclickListener(
                ((DeviceControlActivity) context).mGattCharacteristics.get(
                        groupPosition).get(childPosition), null));

        viewHolder.editTex_w.setKeyListener(new DigitsKeyListener() {

            @Override
            public int getInputType() {
                // TODO Auto-generated method stub
                return android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
            }

            @Override
            protected char[] getAcceptedChars()

            {

                char numberChars[] = { '1', '2', '3', '4', '5', '6', '7', '8',
                        '9', '0', 'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c',
                        'd', 'e', 'f' };

                return numberChars;

            }

        });

        bindView(v, mChildData.get(groupPosition).get(childPosition),
                mChildFrom, mChildTo);

        int charaProp = Integer.parseInt(mChildData.get(groupPosition)
                .get(childPosition).get(LIST_CHARAPROP).toString());
        Log.e("nihao", "charaProp===============: " + charaProp);

        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            Log.e("nihao",
                    "(charaProp | BluetoothGattCharacteristic.PROPERTY_READ)===============: "
                            + (charaProp | BluetoothGattCharacteristic.PROPERTY_READ));

            viewHolder.btn_r.setVisibility(View.VISIBLE);
            Log.e("guojia", "gattCharacteristic的属性为:  可读");
        } else {
            viewHolder.btn_r.setVisibility(View.GONE);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            Log.e("guojia",
                    "(charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE)===============: "
                            + (charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE));
            viewHolder.btn_w.setVisibility(View.VISIBLE);
            viewHolder.editTex_w.setVisibility(View.VISIBLE);
            Log.e("guojia", "gattCharacteristic的属性为:  可写");
        } else {
            viewHolder.btn_w.setVisibility(View.GONE);
            viewHolder.editTex_w.setVisibility(View.GONE);
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            Log.e("guojia",
                    "(charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY)===============: "
                            + (charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY));
            viewHolder.btn_n.setVisibility(View.VISIBLE);
            Log.e("guojia", "gattCharacteristic的属性为:  具备通知属性");
        } else {
            viewHolder.btn_n.setVisibility(View.GONE);
        }
        return v;
    }

    public View newChildView(boolean isLastChild, ViewGroup parent) {
        return mInflater.inflate((isLastChild) ? mLastChildLayout
                : mChildLayout, parent, false);
    }

    private void bindView(View view, Map<String, ?> data, String[] from,
                          int[] to) {
        int len = to.length;

        for (int i = 0; i < len; i++) {
            TextView v = (TextView) view.findViewById(to[i]);
            if (v != null) {
                v.setText((String) data.get(from[i]));
            }
        }
    }

    public int getChildrenCount(int groupPosition) {
        return mChildData.get(groupPosition).size();
    }

    public Object getGroup(int groupPosition) {
        return mGroupData.get(groupPosition);
    }

    public int getGroupCount() {
        return mGroupData.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newGroupView(isExpanded, parent);
        } else {
            v = convertView;
        }
        bindView(v, mGroupData.get(groupPosition), mGroupFrom, mGroupTo);
        return v;
    }

    public View newGroupView(boolean isExpanded, ViewGroup parent) {
        return mInflater.inflate((isExpanded) ? mExpandedGroupLayout
                : mCollapsedGroupLayout, parent, false);
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }

    public class myOnclickListener implements OnClickListener {
        private BluetoothGattCharacteristic characteristic;
        private BluetoothGattCharacteristic mNotifyCharacteristic;
        private EditText editText;

        /**
         *
         */
        public myOnclickListener(BluetoothGattCharacteristic characteristic,
                                 EditText editText) {
            // TODO Auto-generated constructor stub
            this.characteristic = characteristic;
            this.editText = editText;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.view.View.OnClickListener#onClick(android.view.View)
         */
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.button_r:
                    final int charaProp = characteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

                        if (mNotifyCharacteristic != null) {
                            ((DeviceControlActivity) context).mBluetoothLeService
                                    .setCharacteristicNotification(
                                            mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        ((DeviceControlActivity) context).mBluetoothLeService
                                .readCharacteristic(characteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = characteristic;
                        ((DeviceControlActivity) context).mBluetoothLeService
                                .setCharacteristicNotification(characteristic, true);
                    }
                    break;
                case R.id.button_w:
                    if (editText.getText().toString() != null) {
                        mBluetoothGatt = ((DeviceControlActivity) context).mBluetoothLeService.mBluetoothGatt;
                        sendCmd2Ble(characteristic, editText);
                    }

                    break;
                case R.id.button_n:
                    Log.e("wwwwwwwwwwww", mNotifyCharacteristic.WRITE_TYPE_DEFAULT+"");
                    break;

                default:
                    break;
            }

        }

    }

    final static class ViewHolder {

        Button btn_r = null;
        Button btn_w = null;
        Button btn_n = null;
        EditText editTex_w = null;

    }

    private void sendCmd2Ble(BluetoothGattCharacteristic characteristic,
                             final EditText editText) {
        synchronized (mSendThreadLock) {

            characteristic
                    .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            characteristic.setValue(getDataBytes(editText));
            writeCharacteristic(characteristic);

        }
    }

    /**
     *
     * @param characteristic
     *
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.writeCharacteristic(characteristic);
        }

    }

    public byte[] getDataBytes(EditText editText) {
        if (editText == null) {
            return null;
        }
        String str = editText.getText().toString();
        byte[] dataBytes = new byte[] {};

        dataBytes = HexString2Bytes(str);
        // int value = Integer.parseInt(editText.getText().toString(), 16);
        // macBytes = (byte) value;
//        Log.e("ssssssssssssssssssssssssss",
//                DataTypeUtils.bytesToHexString(dataBytes));
        return dataBytes;
    }

    /**
     * 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF,
     * 0xD9}
     *
     * @param src
     *            String
     * @return byte[]
     */
    public static byte[] HexString2Bytes(String src) {
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    /**
     * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF
     *
     * @param src0
     *            byte
     * @param src1
     *            byte
     * @return byte
     */
    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }
}

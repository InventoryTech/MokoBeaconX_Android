#MokoBeaconX Android SDK Instruction DOC（English）

----

## 1. Import project

**1.1 Import "Module mokosupport" to root directory**

**1.2 Edit "settings.gradle" file**

```
include ':app', ':mokosupport'
```

**1.3 Edit "build.gradle" file under the APP project**


	dependencies {
		...
		compile project(path: ':mokosupport')
	}


----

## 2. How to use

**Initialize sdk at project initialization**

```
MokoSupport.getInstance().init(getApplicationContext());
```

**SDK provides three main functions:**

* Scan the device;
* Connect to the device;
* Send and receive data.

### 2.1 Scan the device

 **Start scanning**

```
MokoSupport.getInstance().startScanDevice(callback);
```

 **End scanning**

```
MokoSupport.getInstance().stopScanDevice();
```
 **Implement the scanning callback interface**

```java
/**
 * @ClassPath com.moko.support.callback.MokoScanDeviceCallback
 */
public interface MokoScanDeviceCallback {
    void onStartScan();

    void onScanDevice(DeviceInfo device);

    void onStopScan();
}
```
* **Analysis `DeviceInfo` ; inferred `BeaconInfo`**

```
BeaconInfo beaconInfo = new BeaconXInfoParseableImpl().parseDeviceInfo(device);
```

Please refer to "Demo Project" to use `BeaconInfoParseableImpl` class. You can get some basic information from `BeaconInfo`, such as "Device Name", "MAC address", "RSSI" .

You can filter devices according to the 5th and 6th bytes in the field of broadcasting ServiceID---0xAAFE stands for Eddystone; 0x20FF stands for iBeacon; 0x10FF stands for custom device information witch includs MAC address, Device Name, Battery information etc.


```
if (((int) scanRecord[5] & 0xff) == 0xAA && ((int) scanRecord[6] & 0xff) == 0xFE) {
            length = (int) scanRecord[7];
            isEddystone = true;
        }
        if (((int) scanRecord[5] & 0xff) == 0x20 && ((int) scanRecord[6] & 0xff) == 0xFF) {
            length = (int) scanRecord[3];
            isBeacon = true;
        }
        if (((int) scanRecord[5] & 0xff) == 0x10 && ((int) scanRecord[6] & 0xff) == 0xFF) {
            length = (int) scanRecord[3];
            isDeviceInfo = true;
        }
```

### 2.2 Connect to the device


```
MokoSupport.getInstance().connDevice(context, address, mokoConnStateCallback);
```

When connecting to the device, context, MAC address and callback interface of connection status (`MokoConnStateCallback`) should be transferred in.


```java
public interface MokoConnStateCallback {

    /**
     * @Description  Connecting succeeds
     */
    void onConnectSuccess();

    /**
     * @Description  Disconnect
     */
    void onDisConnected();
}
```

"Demo Project" implements callback interface in Service. It broadcasts status to Activity after receiving the status, and could send and receive data after connecting to the device.

### 2.3 Send and receive data.

All the request data is encapsulated into **TASK**, and sent to the device in a **QUEUE** way.
SDK gets task status from task callback (`MokoOrderTaskCallback`) after sending tasks successfully.

* **Task**

At present, all the tasks sent from the SDK can be divided into 4 types:

> 1.  READ：Readable
> 2.  WRITE：Writable
> 3.  NOTIFY：Can be listened( Need to enable the notification property of the relevant characteristic values)
> 4.  WRITE_NO_RESPONSE：After enabling the notification property, send data to the device and listen to the data returned by device.

Encapsulated tasks are as follows:

<table>
<thead>
<tr>
<th>Task Class</th>
<th>Task Type</th>
<th>Function</th>
</tr>
</thead>

<tbody>
<tr>
<td>NotifyConfigTask</td>
<td>NOTIFY</td>
<td>Enable notification property</td>
</tr>
</tbody>
</table>


Custom device information
--

<table>
<thead>
<tr>
<th>Task Class</th>
<th>Task Type</th>
<th>Function</th>
</tr>
</thead>

<tbody>
<tr>
<td><code>LockStateTask</code></td>
<td>READ</td>
<td>Get Lock State; <strong>0x00</strong> stands for LOCKED and needs to be unlocked; <strong>0x01</strong> stands for UNLOCKED; <strong>0x02</strong> stands for Uulocked and automatic relock disabled.</td>
</tr>
<tr>
<td><code>LockStateTask</code></td>
<td>WRITE</td>
<td>Set new password; AES encryption of 16 byte new password with 16 byte old password ( To prevent the new password from being broadcast in the clear, the client shall AES-128-ECB encrypt the new password with the existing password. The BeaconX shall perform the decryption with its existing password and set that value as the new password. ).</td>
</tr>
<tr>
<td><code>UnLockTask</code></td>
<td>READ</td>
<td>Get a 128-bit challenge token. This token is for one-time use and cannot be replayed.To securely unlock the BeaconX, the host must write a one-time use unlock_token into the characteristic. To create the unlock_token, it first reads the randomly generated 16-byte challenge and generates it using AES-128-ECB.encrypt (key=password[16], text=challenge[16]).</td>
</tr>
<tr>
<td><code>UnLockTask</code></td>
<td>WRITE</td>
<td>Unlock，If the result of this calculation matches the unlock_token written to the characteristic, the beacon is unlocked. Sets the LOCK STATE to 0x01 on success.</td>
</tr>
<tr>
<td><code>ManufacturerTask</code></td>
<td>READ</td>
<td>Get manufacturer.</td>
</tr>
<tr>
<td><code>DeviceModelTask</code></td>
<td>READ</td>
<td>Get product model.</td>
</tr>
<tr>
<td><code>ProductDateTask</code></td>
<td>READ</td>
<td>Get production date.</td>
</tr>
<tr>
<td><code>HardwareVersionTask</code></td>
<td>READ</td>
<td>Get hardware version.</td>
</tr>
<tr>
<td><code>FirmwareVersionTask</code></td>
<td>READ</td>
<td>Get firmware version.</td>
</tr>
<tr>
<td><code>SoftwareVersionTask</code></td>
<td>READ</td>
<td>Get software version.</td>
</tr>
<tr>
<td><code>BatteryTask</code></td>
<td>READ</td>
<td>Get battery capacity.</td>
</tr>
<tr>
<td><code>WriteConfigTask</code></td>
<td>WRITE_NO_RESPONSE</td>
<td>Write <code>ConfigKeyEnum.GET_DEVICE_MAC</code>，get MAC address.</td>
</tr>
<tr>
<td><code>WriteConfigTask</code></td>
<td>WRITE_NO_RESPONSE</td>
<td>Write<code>ConfigKeyEnum.GET_DEVICE_NAME</code>，get device name.</td>
</tr>
<tr>
<td><code>WriteConfigTask</code></td>
<td>WRITE_NO_RESPONSE</td>
<td>Call<code>setDeviceName(String deviceName)</code>，set device name（The length of the name cannot be more than 8）.</td>
</tr>
<tr>
<td><code>WriteConfigTask</code></td>
<td>WRITE_NO_RESPONSE</td>
<td>Write<code>ConfigKeyEnum.GET_CONNECTABLE</code>，get evice connection status; 01:Connectable; 00：Unconnectable.</td>
</tr>
<tr>
<td><code>WriteConfigTask</code></td>
<td>WRITE_NO_RESPONSE</td>
<td>Call<code>setConnectable(boolean isConnectable)</code>，Set the connection status.</td>
</tr>
<tr>
<td><code>ResetDeviceTask</code></td>
<td>WRITE</td>
<td>Reset</td>
</tr>
</tbody>
</table>



iBeacon information
--

<table>
<thead>
<tr>
<th>Task Class</th>
<th>Task Type</th>
<th>Function</th>
</tr>
</thead>

<tbody>
<tr>
<td><code>WriteConfigTask</code></td>
<td>WRITE_NO_RESPONSE</td>
<td>Write <code>ConfigKeyEnum.GET_IBEACON_UUID</code>，get iBeacon UUID.</td>
</tr>
<tr>
<td><code>WriteConfigTask</code></td>
<td>WRITE_NO_RESPONSE</td>
<td>Call<code>setiBeaconUUID(String uuidHex)</code>，set iBeacon UUID(16bytes).</td>
</tr>
<tr>
<td><code>WriteConfigTask</code></td>
<td>WRITE_NO_RESPONSE</td>
<td>Write<code>ConfigKeyEnum.GET_IBEACON_INFO</code>，get iBeacon Major、Minor and advTxPower(RSSI@1m).</td>
</tr>
<tr>
<td><code>WriteConfigTask</code></td>
<td>WRITE_NO_RESPONSE</td>
<td>Call <code>setiBeaconData(int major, int minor, int advTxPower)</code>，set iBeacon Major(2bytes)、Minor(2bytes) and advTxPower(RSSI@1m, 1bytes).</td>
</tr>
</tbody>
</table>


Eddystone information（URL,UID,TLM）
---

<table>
<thead>
<tr>
<th>Task Class</th>
<th>Task Type</th>
<th>Function</th>
</tr>
</thead>

<tbody>
<tr>
<td><code>AdvSlotTask</code></td>
<td>WRITE</td>
<td>Switch SLOT. Please take <code>SlotEnum</code> as reference</td>
</tr>
<tr>
<td><code>AdvSlotDataTask</code></td>
<td>READ</td>
<td>After switching the SLOT, get the current SLOT data and parse the returned data according to the SLOT type.</td>
</tr>
</tbody>
</table>


```
public void setSlotData(byte[] value) {
        int frameType = value[0];
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum != null) {
            switch (slotFrameTypeEnum) {
                case URL:
                    // URL：10cf014c6f766500
                    BeaconXParser.parseUrlData(slotData, value);
                    break;
                case TLM:
                    break;
                case UID:
                    BeaconXParser.parseUidData(slotData, value);
                    break;
            }
        }
    }
```

<table>
<thead>
<tr>
<th>Task Class</th>
<th>Task Type</th>
<th>Function</th>
</tr>
</thead>

<tbody>
<tr>
<td><code>AdvSlotDataTask</code></td>
<td>WRITE</td>
<td>After switching the SLOT, set the current SLOT data</td>
</tr>
</tbody>
</table>

	UID data composition：SLOT type(0x00) + Namespace(10bytes) + Instance ID(6bytes)
	URL data composition：SLOT type(0x10) + URLScheme(1bytes) + URLContent(Max 17bytes)
	TLM data composition：SLOT type(0x20)
	NO_DATA data composition：0

<table>
<thead>
<tr>
<th>Task Class</th>
<th>Task Type</th>
<th>Function</th>
</tr>
</thead>

<tbody>
<tr>
<td><code>RadioTxPowerTask</code></td>
<td>READ</td>
<td>Get current SLOT Tx Power.</td>
</tr>
<tr>
<td><code>RadioTxPowerTask</code></td>
<td>WRITE</td>
<td>Set current SLOT Tx Power(1bytes). Please take <code>TxPowerEnum</code> as reference</td>
</tr>
<tr>
<td><code>AdvIntervalTask</code></td>
<td>READ</td>
<td>Get current SLOT broadcasting Interval.</td>
</tr>
<tr>
<td><code>AdvIntervalTask</code></td>
<td>WRITE</td>
<td>Set current SLOT broadcasting Interval(2bytes). Range：100ms- 4000ms. Example：0x03E8=1000 (Unit:ms).</td>
</tr>
<tr>
<td><code>AdvTxPowerTask</code></td>
<td>WRITE</td>
<td>Set currnent SLOT advTxPower(RSSI@0m, 1bytes). Range：-127dBm—0dBm. Example：0xED=-19dBm.</td>
</tr>
<tr>
<td><code>WriteConfigTask</code></td>
<td>WRITE_NO_RESPONSE</td>
<td>Write<code>ConfigKeyEnum.GET_SLOT_TYPE</code>，get the SLOT type of the five SLOTs. Please take <code>SlotFrameTypeEnum</code> as reference.</td>
</tr>
</tbody>
</table>


* **Create tasks**

The task callback (`MokoOrderTaskCallback`) and task type need to be passed when creating a task. Some tasks also need corresponding parameters to be passed.

Examples of creating tasks are as follows:

```
    /**
     * @Description   get LOCK STATE
     */
    public OrderTask getLockState() {
        LockStateTask lockStateTask = new LockStateTask(this, OrderTask.RESPONSE_TYPE_READ);
        return lockStateTask;
    }
	...
    /**
     * @Description  Set Device Name
     */
    public OrderTask setDeviceName(String deviceName) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        writeConfigTask.setDeviceName(deviceName);
        return writeConfigTask;
    }
	...
    /**
     * @Description   Switch SLOT
     */
    public OrderTask setSlot(SlotEnum slot) {
        AdvSlotTask advSlotTask = new AdvSlotTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        advSlotTask.setData(slot);
        return advSlotTask;
    }
    }
```

* **Send tasks**

```
MokoSupport.getInstance().sendOrder(OrderTask... orderTasks);
```

The task can be one or more.

* **Task callback**

```java
/**
 * @ClassPath com.moko.support.callback.OrderCallback
 */
public interface MokoOrderTaskCallback {

    void onOrderResult(OrderTaskResponse response);

    void onOrderTimeout(OrderTaskResponse response);

    void onOrderFinish();
}
```
`void onOrderResult(OrderTaskResponse response);`

	After the task is sent to the device, the data returned by the device can be obtained by using the `onOrderResult` function, and you can determine witch class the task is according to the `response.orderType` function. The `response.responseValue` is the returned data.

`void onOrderTimeout(OrderTaskResponse response);`

	Every task has a default timeout of 3 seconds to prevent the device from failing to return data due to a fault and the fail will cause other tasks in the queue can not execute normally. After the timeout, the `onOrderTimeout` will be called back. You can determine witch class the task is according to the `response.orderType` function and then the next task continues.

`void onOrderFinish();`

	When the task in the queue is empty, `onOrderFinish` will be called back.

* **Listening task**

If the task belongs to `NOTIFY` and ` WRITE_NO_RESPONSE` task has been sent, the task is in listening state. When there is data returned from the device, the data will be sent in the form of broadcast, and the action of receiving broadcast is `MokoConstants.ACTION_RESPONSE_NOTIFY`.

```
String action = intent.getAction();
...
if (MokoConstants.ACTION_RESPONSE_NOTIFY.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    byte[] value = intent.getByteArrayExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE);
                    ...
                }
```

Get `OrderTaskResponse` from the **intent** of `onReceive`, and the corresponding **key** value is `MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK`.

## 4. Special instructions

> 1. AndroidManifest.xml of SDK has declared to access SD card and get Bluetooth permissions.
> 2. The SDK comes with logging, and if you want to view the log in the SD card, please to use "LogModule". The log path is : root directory of SD card/mokoBeaconX/mokoLog. It only records the log of the day and the day before.
> 3. Just connecting to the device successfully, it needs to delay 1 second before sending data, otherwise the device can not return data normally.
> 4. We suggest that sending and receiving data should be executed in the "Service". There will be a certain delay when the device returns data, and you can broadcast data to the "Activity" after receiving in the "Service". Please refer to the "Demo Project".
















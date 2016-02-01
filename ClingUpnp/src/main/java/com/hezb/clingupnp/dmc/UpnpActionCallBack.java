package com.hezb.clingupnp.dmc;

/**
 * upnp回调封装
 */
public interface UpnpActionCallBack {
	
	/**
	 * 回调成功
	 */
	void onSuccess(int type);

	/**
	 * 回调出错
     */
	void onFailure(int type, String error);

	/**
	 * 获取音量成功
	 * 返回音量值
	 */
	void getVolumeReceived(int volume);

	/**
	 * 获取当前视频状态成功
	 * 返回是否播放
     */
	void getTransportReceived(boolean isPlaying);

	/**
	 * 获取当前视频信息成功
	 * 返回当前进度 时长
	 */
	void getPositionInfoReceived(int currentPosition, int duration);
	
}

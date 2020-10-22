package com.viettel.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * @author quanns2
 */
@Service(value = "iconUtils")
@Scope("session")
public class IconUtils implements Serializable {
	public String statusIcon(Integer status) {
		if (status == null)
			return "";
		String icon ="icon-mind Green Fs40";
		switch (status) {
			case Constant.STAND_BY_STATUS:
				icon = "fa fa-hourglass-1 Blue Fs40";
				break;
			case Constant.RUNNING_STATUS:
				icon = "icon-refresh47 Yellow Fs40";
				break;
			case Constant.FINISH_SUCCESS_STATUS:
				icon = "fa fa-check-circle Green Fs40";
				break;
			case Constant.FINISH_FAIL_STATUS:
				icon = "fa fa-times-circle-o RedN Fs40";
				break;
			case Constant.CANCEL_STATUS:
				icon = "fa fa-stop-circle-o RedN Fs40";
				break;
			case Constant.NOT_ALLOW_STATUS:
				icon = "fa fa-hand-stop-o Green Fs40";
				break;
			case Constant.WARNING_STATUS:
				icon = "fa fa-warning RedN Fs40";
				break;
			case Constant.FINISH_SUCCESS_WITH_WARNING:
				icon = "fa fa-warning Yellow Fs40";
				break;
			case AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER:
				icon = "fa fa-medkit Yellow Fs40";
				break;
			default:
				break;
		}
		return icon;
	}

	public String monitorIcon(Integer status) {
		if (status == null)
			return "";
//		String icon ="icon-mind Green Fs40";
		String icon = "";
		switch (status.intValue()) {
			case AamConstants.RUNNING_STATUS.SUCCESS:
				icon = "fa fa-check Green Fs18";
				break;
			case AamConstants.RUNNING_STATUS.RUNNING:
				icon = "fa fa-play Yellow Fs18";
				break;
			case AamConstants.RUNNING_STATUS.FAIL:
				icon = "fa fa-warning RedN Fs18";
				break;
			case AamConstants.RUNNING_STATUS.WAIT:
				icon = "fa fa-clock-o RedN Fs18";
				break;
			default:
				break;
		}
		return icon;
	}

	public String statusIconSmall(Integer status) {
		String fa = "";

		if (status == null)
			return "";

		switch (status) {
			case Constant.STAND_BY_STATUS:
				break;
			case Constant.RUNNING_STATUS:
				fa = " fa-forward Yellow";
				break;
			case AamConstants.RUN_STATUS.FINISH_SUCCESS_STATUS_STOP:
			case Constant.FINISH_SUCCESS_STATUS:
				fa = " fa-check Green";
				break;
			case Constant.FINISH_FAIL_STATUS:
				fa = " fa-close RedN";
				break;
			case Constant.FINISH_SUCCESS_WITH_WARNING:
				fa = " fa-warning Yellow";
				break;
			default:
				break;

		}

		return fa;
	}
}

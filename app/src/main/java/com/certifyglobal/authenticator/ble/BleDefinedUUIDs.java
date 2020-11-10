package com.certifyglobal.authenticator.ble;

import java.util.UUID;

public class BleDefinedUUIDs {

	public static class Service {
		final static public UUID HEART_RATE = UUID.fromString("98CCA859-91E7-7586-7747-83E44EF482E2");

		final static public UUID AUTHX = UUID.fromString("E282F44E-E483-4777-8675-E79159A8CC98");

	}

	;

	public static class Characteristic {
		final static public UUID HEART_RATE_MEASUREMENT = UUID.fromString("98CCA859-91E7-7586-7747-83E44EF482E2");
		final static public UUID MANUFACTURER_STRING = UUID.fromString("98CCA859-91E7-7586-7747-83E44EF482E2");
		final static public UUID MODEL_NUMBER_STRING = UUID.fromString("98CCA859-91E7-7586-7747-83E44EF482E2");
		final static public UUID FIRMWARE_REVISION_STRING = UUID.fromString("98CCA859-91E7-7586-7747-83E44EF482E2");
		final static public UUID APPEARANCE = UUID.fromString("98CCA859-91E7-7586-7747-83E44EF482E2");
		final static public UUID BODY_SENSOR_LOCATION = UUID.fromString("98CCA859-91E7-7586-7747-83E44EF482E2");
		final static public UUID BATTERY_LEVEL = UUID.fromString("98CCA859-91E7-7586-7747-83E44EF482E2");

		final static public UUID AUTHX_OTP = UUID.fromString("C81EF508-0DC4-4701-913A-2D3B5D3743AC");
	}

	public static class Descriptor {
		final static public UUID CHAR_CLIENT_CONFIG = UUID.fromString("98CCA859-91E7-7586-7747-83E44EF482E2");
	}

}

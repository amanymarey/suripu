package com.hello.suripu.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

public class DeviceData {

    public static final float FLOAT_2_INT_MULTIPLIER = 100;
    private static final float MAX_DUST_ANALOG_VALUE = 4096;
    private static final float DUST_FLOAT_TO_INT_MULTIPLIER = 1000000;


    @JsonProperty("account_id")
    public final Long accountId;

    @JsonProperty("device_id")
    public final Long deviceId;

    @JsonProperty("ambient_temperature")
    public final int ambientTemperature;

    @JsonProperty("ambient_humidity")
    public final int ambientHumidity;

    @JsonProperty("ambient_air_quality")
    public final int ambientAirQuality;

    @JsonProperty("ambient_light")
    public final int ambientLight;

    @JsonProperty("ambient_light_variance")
    public final int ambientLightVariance;

    @JsonProperty("ambient_light_peakiness")
    public final int ambientLightPeakiness;

    @JsonProperty("timestamp_utc")
    public final DateTime dateTimeUTC;

    @JsonProperty("offset_millis")
    public final Integer offsetMillis;

    public DeviceData(
            final Long accountId,
            final Long deviceId,
            final int ambientTemperature,
            final int ambientHumidity,
            final int ambientAirQuality,
            final int ambientLight,
            final int ambientLightVariance,
            final int ambientLightPeakiness,
            final DateTime dateTimeUTC,
            final Integer offsetMillis) {
        this.accountId = accountId;
        this.deviceId = deviceId;
        this.ambientTemperature = ambientTemperature;
        this.ambientHumidity = ambientHumidity;
        this.ambientAirQuality = ambientAirQuality;
        this.ambientLight = ambientLight;
        this.dateTimeUTC = dateTimeUTC;
        this.ambientLightVariance = ambientLightVariance;
        this.ambientLightPeakiness = ambientLightPeakiness;
        this.offsetMillis = offsetMillis;

        checkNotNull(this.accountId);
        checkNotNull(this.deviceId);
        checkNotNull(this.dateTimeUTC);
        checkNotNull(this.offsetMillis);
    }

    public static int floatToDBInt(final float value){
        return (int)(value * FLOAT_2_INT_MULTIPLIER);
    }

    public static float dbIntToFloat(final int valueFromDB){
        return valueFromDB / FLOAT_2_INT_MULTIPLIER;
    }

    public static int convertDustAnalogToPPM(final int AnalogValue, final int firmwareVersion) {
        // convert raw counts to ppm for dust sensor
        float voltage = (float) AnalogValue / MAX_DUST_ANALOG_VALUE * 4.0f;

        // TODO: add checks for firmware version when we switch sensor
        // SHARP GP2Y1010AU0F  PM2.5(see Fig. 3 of spec sheet)
        final float coeff = 0.5f/2.9f;
        final float intercept = 0.6f * coeff;
        final float maxVoltage = 3.2f;

        voltage = Math.min(voltage, maxVoltage);
        final float dustDensity = coeff * voltage - intercept; // micro-gram per m^3
        return (int) (dustDensity * DUST_FLOAT_TO_INT_MULTIPLIER);
    }

    public static class Builder{
        private Long accountId;
        private Long deviceId;
        private int ambientTemperature;
        private int ambientHumidity;
        private int ambientAirQuality;
        private int ambientLight;
        private int ambientLightVariance;
        private int ambientLightPeakiness;
        private DateTime dateTimeUTC;
        private Integer offsetMillis;

        public Builder withAccountId(final Long accountId){
            this.accountId = accountId;
            return this;
        }

        public Builder withDeviceId(final Long deviceId){
            this.deviceId = deviceId;
            return this;
        }

        public Builder withAmbientTemperature(final int ambientTemperature){
            this.ambientTemperature = ambientTemperature;
            return this;
        }

        public Builder withAmbientHumidity(final int ambientHumidity){
            this.ambientHumidity = ambientHumidity;
            return this;
        }

        public Builder withAmbientAirQuality(final int ambientAirQuality, final int firmwareVersion){
            this.ambientAirQuality = convertDustAnalogToPPM(ambientAirQuality, firmwareVersion);
            return this;
        }

        public Builder withAmbientLight(final int ambientLight){
            this.ambientLight = ambientLight;
            return this;
        }

        public Builder withAmbientLightVariance(final int ambientLightVariance){
            this.ambientLightVariance = ambientLightVariance;
            return this;
        }

        public Builder withAmbientLightPeakiness(final int ambientLightPeakiness){
            this.ambientLightPeakiness = ambientLightPeakiness;
            return this;
        }

        public Builder withDateTimeUTC(final DateTime dateTimeUTC){
            this.dateTimeUTC = dateTimeUTC;
            return this;
        }

        public Builder withOffsetMillis(final Integer offsetMillis){
            this.offsetMillis = offsetMillis;
            return this;
        }

        public DeviceData build(){
            return new DeviceData(this.accountId, this.deviceId, this.ambientTemperature, this.ambientHumidity, this.ambientAirQuality, this.ambientLight, this.ambientLightVariance, this.ambientLightPeakiness, this.dateTimeUTC, this.offsetMillis);
        }


    }

    @Override
    public String toString() {
        return Objects.toStringHelper(DeviceData.class)
                .add("account_id", accountId)
                .add("device_id", deviceId)
                .add("ambient_temperature", ambientTemperature)
                .add("ambient_humidity", ambientHumidity)
                .add("ambient_light", ambientLight)
                .add("ambient_light_variance", ambientLightVariance)
                .add("ambient_light_peakiness", ambientLightPeakiness)
                .add("dateTimeUTC", dateTimeUTC)
                .add("offset_millis", offsetMillis)
                .toString();
    }
}

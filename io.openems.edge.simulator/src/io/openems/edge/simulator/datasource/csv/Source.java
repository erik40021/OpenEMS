package io.openems.edge.simulator.datasource.csv;

public enum Source {
	ZERO("zero.csv"), //
	H0_HOUSEHOLD_SUMMER_WEEKDAY_STANDARD_LOAD_PROFILE("h0-summer-weekday-standard-load-profile.csv"), //
	H0_HOUSEHOLD_SUMMER_WEEKDAY_PV_PRODUCTION("h0-summer-weekday-pv-production.csv"), //
	H0_HOUSEHOLD_SUMMER_WEEKDAY_NON_REGULATED_CONSUMPTION("h0-summer-weekday-non-regulated-consumption.csv"), //
	H0_HOUSEHOLD_SUMMER_WEEKDAY_PV_PRODUCTION2("h0-summer-weekday-pv-production2.csv"),
	APT1_2014_MAINSTRIP_LOADS("Apt1_2014_mainstrip_loads.csv"),
	APT1_2014_MAINSTRIP_TIMESTAMPS("Apt1_2014_mainstrip_timestamps.csv");

	public final String filename;

	private Source(String filename) {
		this.filename = filename;
	}
}

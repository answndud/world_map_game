package com.worldmap.game.location.application;

import com.worldmap.country.domain.Country;
import org.springframework.stereotype.Component;

@Component
public class LocationGameDistanceHintPolicy {

	private static final double EARTH_RADIUS_KM = 6371.0;

	public LocationGameDistanceHint buildHint(Country selectedCountry, Country targetCountry) {
		double selectedLat = selectedCountry.getReferenceLatitude().doubleValue();
		double selectedLng = selectedCountry.getReferenceLongitude().doubleValue();
		double targetLat = targetCountry.getReferenceLatitude().doubleValue();
		double targetLng = targetCountry.getReferenceLongitude().doubleValue();

		int distanceKm = (int) Math.round(haversineKm(selectedLat, selectedLng, targetLat, targetLng));
		String directionHint = directionFrom(selectedLat, selectedLng, targetLat, targetLng, distanceKm);
		return new LocationGameDistanceHint(distanceKm, directionHint);
	}

	private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
		double latitudeDelta = Math.toRadians(lat2 - lat1);
		double longitudeDelta = Math.toRadians(normalizeLongitudeDelta(lng2 - lng1));
		double latitude1 = Math.toRadians(lat1);
		double latitude2 = Math.toRadians(lat2);

		double haversine = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
			+ Math.cos(latitude1) * Math.cos(latitude2)
			* Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);

		double arc = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
		return EARTH_RADIUS_KM * arc;
	}

	private String directionFrom(double lat1, double lng1, double lat2, double lng2, int distanceKm) {
		if (distanceKm <= 150) {
			return "바로 근처";
		}

		double latitude1 = Math.toRadians(lat1);
		double latitude2 = Math.toRadians(lat2);
		double longitudeDelta = Math.toRadians(normalizeLongitudeDelta(lng2 - lng1));

		double y = Math.sin(longitudeDelta) * Math.cos(latitude2);
		double x = Math.cos(latitude1) * Math.sin(latitude2)
			- Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longitudeDelta);
		double bearing = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;

		if (bearing < 22.5 || bearing >= 337.5) {
			return "북쪽";
		}
		if (bearing < 67.5) {
			return "북동쪽";
		}
		if (bearing < 112.5) {
			return "동쪽";
		}
		if (bearing < 157.5) {
			return "남동쪽";
		}
		if (bearing < 202.5) {
			return "남쪽";
		}
		if (bearing < 247.5) {
			return "남서쪽";
		}
		if (bearing < 292.5) {
			return "서쪽";
		}
		return "북서쪽";
	}

	private double normalizeLongitudeDelta(double delta) {
		double normalized = delta;
		while (normalized > 180) {
			normalized -= 360;
		}
		while (normalized < -180) {
			normalized += 360;
		}
		return normalized;
	}
}

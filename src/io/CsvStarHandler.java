package io;

import static app.Settings.STAR_HEADERS;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import app.Settings;
import domain.galaxy.Coordinate;
import domain.galaxy.LuminosityClass;
import domain.galaxy.Star;
import domain.galaxy.StarType;

public class CsvStarHandler  {
	private static CSVRecord test;
	
	public static Set<Star> readStarsfromCsv(String path) throws IOException {
		Set<Star> stars = new HashSet<>();
		boolean firstLine = true;
		Reader reader = null;
		CSVParser csvParser = null;
		try {
			reader = Files.newBufferedReader(Paths.get(path));
			csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader(STAR_HEADERS));
			for (CSVRecord record : csvParser) {
				test = record;
				if (firstLine) {
					firstLine = false;
				}
				else {
					String designation = record.get(0);
					String name = record.get(1);
					String starType = record.get(2);
					String sequence = record.get(3);
					String luminosity = record.get(4);
					String magnitude = record.get(5);
					String distance = record.get(6);
					String longitude = record.get(7);
					String latitude = record.get(8);
					String goldilockStart = record.get(9);
					String goldilockEnd = record.get(10);
					String sisterDesignation = record.get(11);
					
					Star dummySister = new Star();
					if ((sisterDesignation != null) && (sisterDesignation.length() == 0)) {
						dummySister.setDesignation(null);
					}
					else {
						dummySister.setDesignation(sisterDesignation);
					}
					
					Star star = new Star(designation, name, StarType.parse(starType), Integer.valueOf(sequence), LuminosityClass.parse(luminosity), 
											Float.valueOf(magnitude), Float.valueOf(distance), Float.valueOf(longitude), Float.valueOf(latitude), 
											Double.valueOf(goldilockStart), Double.valueOf(goldilockEnd), dummySister);
					
					stars.add(star);
				}
			}
		}
		catch (IndexOutOfBoundsException e) {
			System.out.println("Bad record found: " + test);
		} 
		finally {
			if (csvParser != null) {
				try {
					csvParser.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		return stars;
	}
	
	public static Set<Star> readInitStarList() {
		Set<Star> stars = new HashSet<>();
		File existingStars = Settings.instance().getExistingStarsFile();
		Reader reader = null;
		CSVParser csvParser = null;
		try {
			reader = Files.newBufferedReader(existingStars.toPath());
			csvParser = new CSVParser(reader, CSVFormat.newFormat(';'));
			for (CSVRecord csvRecord : csvParser) {
				try {
					String name = null;
					Double longitude = Double.valueOf(csvRecord.get(2).replace(',','.')); // This is counted from where we are, need to correct it
					Double latitude = Double.valueOf(csvRecord.get(3).replace(',','.')); // This is counted from where we are, need to correct it
					Double distance = Double.valueOf(csvRecord.get(4).replace(',','.')); // This is counted from where we are, need to correct it
					Float magnitude = Float.valueOf(csvRecord.get(8).replace(',','.'));
					Coordinate coordinate = correctCoordinateFromSolToBlackHole(new Coordinate(distance, longitude, latitude));
					
					String spectrum = csvRecord.get(6);
					Star result = null;
					// First check for binary systems. If so, this is a Wolf-Rayet star with a twin.
					if ((spectrum.contains(":") || 
						(spectrum.contains("+")) ||
						(spectrum.contains("/")))) {
						if (distance <= Settings.instance().getGalaxySize()) {
							String designation = Star.randomDesignation(StarType.W, coordinate);
							result = new Star(designation, name, StarType.W, 0, LuminosityClass.O, magnitude, coordinate);
						}
						// Create the other star in the twin system, if it can be decided
						String[] elements = spectrum.split(":");
						if (elements.length < 2) {
							elements = spectrum.split("/");
							if (elements.length < 2) {
								elements = spectrum.split("\\+");
							}
						}
						StarType starType = StarType.parse(elements[1]);
						if (starType != null) {
							int temperatureSequence = -1;
							if (elements[1].length() > 1) {
								temperatureSequence = Integer.valueOf(elements[1].substring(1,2));
							}
							else {
								temperatureSequence = 0;
							}
							LuminosityClass luminosityClass = LuminosityClass.parse(elements[1]);
							if (distance <= Settings.instance().getGalaxySize()) {
								String designation = Star.randomDesignation(starType, coordinate);
								Star twin = new Star(designation, null, starType, temperatureSequence, luminosityClass, magnitude, coordinate);
								if (twin != null) {
									Coordinate sc = result.getCoordinate();
									double variation = ((Settings.instance().getRandom().nextFloat() * 30f) + 15f) * (float) Math.pow((double) ((result.getType().getMinMass() + result.getType().getMaxMass()) / 2f), (1d/3d));
									double newDistance = sc.getDistance() * 206264d + variation;
									Coordinate newCoordinate = new Coordinate((newDistance / 206264f), sc.getLongitude(), sc.getLatitude());
									twin.setCoordinate(newCoordinate);
									StringBuilder sisterDesignation = new StringBuilder(result.getDesignation().substring(0, 5));
									sisterDesignation.append(Long.valueOf(result.getDesignation().substring(5)) + 1);
									twin.setDesignation(new String(sisterDesignation));
									result.setSister(twin);
									twin.setSister(result);
									stars.add(twin);
								}
								stars.add(result);
							}
						}
					}
					else {
						StarType starType = StarType.parse(spectrum);
						int temperatureSequence = -1;
						if ((spectrum.length() > 2) && spectrum.substring(1,2).equals(" ")) {
							// Regular star
							temperatureSequence = Integer.valueOf(spectrum.substring(2,3));
						}
						else {
							// Wolf-Rayet star with no known temperatureSequence
							temperatureSequence = 0;
						}
						String luminosity = csvRecord.get(7);
						LuminosityClass luminosityClass = null;
						if ((luminosity == null) || (luminosity.equals(""))) {
							luminosityClass = LuminosityClass.I;
						}
						else {
							luminosityClass = LuminosityClass.parse(luminosity);
						}
						if (distance <= Settings.instance().getGalaxySize()) {
							String designation = Star.randomDesignation(starType, coordinate);
							result = new Star(designation, null, starType, temperatureSequence, luminosityClass, magnitude, coordinate);
							stars.add(result);
						}
						
					}
				}
				catch (NumberFormatException e) {

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		finally {
			if (csvParser != null) {
				try {
					csvParser.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
	return stars;
	}
	
	public static Set<Star> readCustomStars(Set<Star> initStars) throws IOException {
		Set<Star> stars = new HashSet<>();
		File customStars = Settings.instance().getCustomStarsFile();
		Reader reader = null;
		CSVParser csvParser = null;
		try {
			reader = Files.newBufferedReader(customStars.toPath());
			csvParser = new CSVParser(reader, CSVFormat.newFormat(';'));
			for (CSVRecord csvRecord : csvParser) {
				try {
					String name = csvRecord.get(0);
					StarType type = StarType.parse(csvRecord.get(1));
					int temperatureSequence = Integer.valueOf(csvRecord.get(2));
					LuminosityClass luminosity = LuminosityClass.parse(csvRecord.get(3));
					
					Double distance = Double.valueOf(csvRecord.get(6).replace(',','.')); // This is counted from where we are, need to correct it
					Double longitude = Double.valueOf(csvRecord.get(4).replace(',','.')); // This is counted from where we are, need to correct it
					Double latitude = (Double.valueOf(csvRecord.get(5).replace(',','.'))) * 360d / 24d; // This is counted from where we are, need to correct it
					Coordinate coordinate = correctCoordinateFromSolToBlackHole(new Coordinate(distance, longitude, latitude));
					
					float magnitude = 0;
					Set<Star> temp = new HashSet<>();
					for (Star s: initStars) {
						if (s.getType() == type) {
							temp.add(s);
						}
					}
					Star target = null;
					double afstand = 1E+12;
					for (Star s: temp) {
						double x = s.getCoordinate().calculateDistance(coordinate);
						if (x < afstand) {
							afstand = x;
							target = s;
						}
					}
					
					if (target != null) {
						magnitude = target.getAbsoluteMagnitude();
					}
					
					String designation = Star.randomDesignation(type, coordinate);
					Star result = new Star(designation, name, type, temperatureSequence, luminosity, magnitude, coordinate);
					stars.add(result);
				}
				catch (NumberFormatException e) {
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		finally {
			if (csvParser != null) {
				try {
					csvParser.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		stars.addAll(readBaseStars());
		return stars;
	}
	
	private static Coordinate correctCoordinateFromSolToBlackHole(Coordinate coordinate) {
		double distance = coordinate.getDistance();
		double longitude = coordinate.getLongitude();
		double latitude = coordinate.getLatitude();
		
		latitude = latitude * (1d-((11000 - distance) / 11000));
		
		if ((longitude > 90) && (longitude < 270)) {
			// Between Sol and BH1
			longitude = longitude + (Math.asin(Math.sin((180-longitude) * Math.PI / 180) * ((distance - 11000) / 11000))) * 180 / Math.PI;
		}
		else {
			// Sol is between object and BH1
			if (distance != 11000) {
				longitude = (Math.asin((Math.sin(longitude * Math.PI / 180) / (distance - 11000)))) * 180 / Math.PI;
			}
		}
		distance = Math.abs(11000 - distance);
		return new Coordinate(distance, longitude, latitude);
	}
	
	public static Set<Star> readBaseStars() throws IOException {
		Set<Star> result = new HashSet<>();
		
		Star newStar = new Star("SAGI-1000000000000000001", "Sagittarius A*", StarType.H, 5, LuminosityClass.VIII, 1000.0f, 0d, 0d, 0d);
		result.add(newStar);
		
		newStar = new Star("SAGI-1000000000000000002", "S1", StarType.B, 0, LuminosityClass.V, -1.1f, 0.0006d,  246.62607015d, 0d);
		result.add(newStar);
		
		newStar = new Star("SAGI-1000000000000000003", "S2", StarType.B, 0, LuminosityClass.V, -1.1f, 0.0006d, 3.14159265d, 0d);
		result.add(newStar);
		
		newStar = new Star("SAGI-1000000000000000004", "S3", StarType.B, 0, LuminosityClass.V, -1.1f, 0.0006d, 122.71828182, 0d);
		result.add(newStar);	
		
		newStar = new Star("SOLA-6592602058205295101", "Sol", StarType.G, 5, LuminosityClass.I, 5.0f, 10981.917d, 0.314159265d, 0.000712d);
		result.add(newStar);

		return result;
	}
}

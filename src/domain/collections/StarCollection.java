package domain.collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import app.Settings;
import domain.galaxy.Body;
import domain.galaxy.Star;
import domain.galaxy.StarType;
import io.CsvStarHandler;

public class StarCollection implements Set<Star> {
	private Set<Star> stars;
	private Map<StarType, List<Star>> typeMap;
	private static volatile StarCollection internalObject = null;
	private Star centralBlackHole;
	private Star councilCenter;
	private Map<Integer, Set<Star>> distanceMap;
	private Set<String> usedDesignations;

	public StarCollection() throws IOException {
		super();
		initPerformanceMaps();
		this.stars = new HashSet<>();
		this.usedDesignations = new HashSet<>();
		this.addAll(CsvStarHandler.readBaseStars());
		this.centralBlackHole = this.find("SAGI-1000000000000000001");
		this.councilCenter = this.find("COUN-1000000000000000001");
	}
	
	public static StarCollection instance() throws IOException {
		if (internalObject == null) {
			internalObject = new StarCollection();
		}
		return internalObject;
	}
	
	private void initPerformanceMaps() {
		this.distanceMap = new TreeMap<>();
		for (int i=0; i<=Settings.instance().getGalaxySize(); i++) {
			distanceMap.put(i, new HashSet<>());
		}
		StarType[] types = StarType.values();
		this.typeMap = new HashMap<>();
		for (StarType type: types) {
			typeMap.put(type,  new ArrayList<Star>());
		}
	}

	@Override
	public int size() {
		return stars.size();
	}

	@Override
	public boolean isEmpty() {
		return stars.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return stars.contains(o);
	}
	
	public boolean contains(String designation) {
		return usedDesignations.contains(designation);
	}

	@Override
	public Iterator<Star> iterator() {
		return stars.iterator();
	}

	@Override
	public Object[] toArray() {
		return stars.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return stars.toArray(a);
	}

	public Star getCentralBlackHole() {
		return centralBlackHole;
	}

	public Star getCouncilCenter() {
		return councilCenter;
	}

	@Override
	public boolean add(Star e) {
		if (usedDesignations.contains(e.getDesignation())) {
			return false;
		}
		// If the distance is too nice, it is an initStar
		if ((e.getCoordinate().getDistance() - (double) ((int) e.getCoordinate().getDistance())) > 0.00001) {
			if (checkIllegalDistance(e)) {
				return false;
			}
		}
		if (stars.add(e)) {
			typeMap.get(e.getType()).add(e);
			int distanceSlice = (int) e.getCoordinate().getDistance();
			Set<Star> distanceStars = distanceMap.get(distanceSlice);
			distanceStars.add(e);
			distanceMap.put(distanceSlice, distanceStars);
			
			//distanceMap.get((int) e.getCoordinate().getDistance()).add(e);
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Star) {
			typeMap.get(((Star) o).getType()).remove(o);
			if (stars.remove((Star) o)) {
				for (Star s: stars) {
					if (s.getSister() == o) {
						s.setSister(null);
						break;
					}
				}
				distanceMap.get((int) ((Star) o).getCoordinate().getDistance()).remove(o);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if (stars.remove(o)) {
				for (Star s: stars) {
					if (s.getSister() == o) {
						s.setSister(null);
						break;
					}
				}
				distanceMap.get((int) ((Star) o).getCoordinate().getDistance()).remove(o);
				return true;
			}
			else {
				return false;
			}
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return stars.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Star> c) {
		if (c == null) {
			return false;
		}
		boolean result = false;
		for (Object o: c) {
			boolean r = this.add((Star) o);
			result = result | r;
		}
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Iterator<Star> i = stars.iterator();
		boolean result = false;
		while (i.hasNext()) {
			Body s = i.next();
			if (!c.contains(s)) {
				i.remove();
				result = true;
			}
		}
		return result;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for (Object o: c) {
			boolean temp = this.remove(o);
			result = result || temp;
		}
		
		return result;
	}

	@Override
	public void clear() {
		stars.clear();
		typeMap.clear();
		distanceMap.clear();
		usedDesignations.clear();
		initPerformanceMaps();
	}
	
	public Body getCentral() {
		return centralBlackHole;
	}

	public List<Star> get(StarType type) {
		List<Star> result = new ArrayList<>();
		result.addAll(typeMap.get(type));
		return result;
	}
	
	public Star find(String designation) {
		Iterator<Star> i = stars.iterator();
		while (i.hasNext()) {
			Star result = i.next();
			if (result.getDesignation().equals(designation)) {
				return result;
			}
		}
		return null;
	}
	
	private boolean checkIllegalDistance(Star check) {
		for (Star s: distanceMap.get((int) check.getCoordinate().getDistance())) {
			double distance = s.calculateDistance(check);
			if (distance < Settings.instance().getMinDistance() && (s.getSister() != null) && (!s.getSister().equals(check))) {
				// Too close and the other is not the sister star
				return true;
			}
			else {
				return false;
			}
		}
		return false;
	}
}

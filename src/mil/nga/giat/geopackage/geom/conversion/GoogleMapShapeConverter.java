package mil.nga.giat.geopackage.geom.conversion;

import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.geom.CircularString;
import mil.nga.giat.geopackage.geom.CompoundCurve;
import mil.nga.giat.geopackage.geom.Geometry;
import mil.nga.giat.geopackage.geom.GeometryCollection;
import mil.nga.giat.geopackage.geom.GeometryType;
import mil.nga.giat.geopackage.geom.LineString;
import mil.nga.giat.geopackage.geom.MultiLineString;
import mil.nga.giat.geopackage.geom.MultiPoint;
import mil.nga.giat.geopackage.geom.MultiPolygon;
import mil.nga.giat.geopackage.geom.Point;
import mil.nga.giat.geopackage.geom.Polygon;
import mil.nga.giat.geopackage.geom.PolyhedralSurface;
import mil.nga.giat.geopackage.geom.TIN;
import mil.nga.giat.geopackage.geom.Triangle;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Provides conversions methods between geometry object and Google Maps Android
 * API v2 Shapes
 * 
 * @author osbornb
 */
public class GoogleMapShapeConverter {

	/**
	 * Unit converter
	 */
	private final UnitConverter unitConverter;

	/**
	 * Constructor, uses default {@link DegreesConverter}
	 */
	public GoogleMapShapeConverter() {
		this(new DegreesConverter());
	}

	/**
	 * Constructor with specified unit converter
	 * 
	 * @param unitConverter
	 */
	public GoogleMapShapeConverter(UnitConverter unitConverter) {
		this.unitConverter = unitConverter;
	}

	/**
	 * Convert a {@link Point} to a {@link LatLng}
	 * 
	 * @param point
	 * @return
	 */
	public LatLng toLatLng(Point point) {
		double latitude = unitConverter.toDegrees(point.getY());
		double longitude = unitConverter.toDegrees(point.getX());
		LatLng latLng = new LatLng(latitude, longitude);
		return latLng;
	}

	/**
	 * Convert a {@link LatLng} to a {@link Point}
	 * 
	 * @param latLng
	 * @return
	 */
	public Point toPoint(LatLng latLng) {
		return toPoint(latLng, false, false);
	}

	/**
	 * Convert a {@link LatLng} to a {@link Point}
	 * 
	 * @param latLng
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public Point toPoint(LatLng latLng, boolean hasZ, boolean hasM) {
		double y = unitConverter.degreesToUnits(latLng.latitude);
		double x = unitConverter.degreesToUnits(latLng.longitude);
		Point point = new Point(hasZ, hasM, x, y);
		return point;
	}

	/**
	 * Convert a {@link LineString} to a {@link PolylineOptions}
	 * 
	 * @param lineString
	 * @return
	 */
	public PolylineOptions toPolyline(LineString lineString) {

		PolylineOptions polylineOptions = new PolylineOptions();

		for (Point point : lineString.getPoints()) {
			LatLng latLng = toLatLng(point);
			polylineOptions.add(latLng);
		}

		return polylineOptions;
	}

	/**
	 * Convert a {@link Polyline} to a {@link LineString}
	 * 
	 * @param polyline
	 * @return
	 */
	public LineString toLineString(Polyline polyline) {
		return toLineString(polyline, false, false);
	}

	/**
	 * Convert a {@link Polyline} to a {@link LineString}
	 * 
	 * @param polyline
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public LineString toLineString(Polyline polyline, boolean hasZ, boolean hasM) {
		return toLineString(polyline.getPoints(), hasZ, hasM);
	}

	/**
	 * Convert a {@link PolylineOptions} to a {@link LineString}
	 * 
	 * @param polyline
	 * @return
	 */
	public LineString toLineString(PolylineOptions polyline) {
		return toLineString(polyline, false, false);
	}

	/**
	 * Convert a {@link PolylineOptions} to a {@link LineString}
	 * 
	 * @param polyline
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public LineString toLineString(PolylineOptions polyline, boolean hasZ,
			boolean hasM) {
		return toLineString(polyline.getPoints(), hasZ, hasM);
	}

	/**
	 * Convert a list of {@link LatLng} to a {@link LineString}
	 * 
	 * @param latLngs
	 * @return
	 */
	public LineString toLineString(List<LatLng> latLngs) {
		return toLineString(latLngs, false, false);
	}

	/**
	 * Convert a list of {@link LatLng} to a {@link LineString}
	 * 
	 * @param latLngs
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public LineString toLineString(List<LatLng> latLngs, boolean hasZ,
			boolean hasM) {

		LineString lineString = new LineString(hasZ, hasM);

		for (LatLng latLng : latLngs) {
			Point point = toPoint(latLng, hasZ, hasM);
			lineString.addPoint(point);
		}

		return lineString;
	}

	/**
	 * Convert a {@link Polygon} to a {@link PolygonOptions}
	 * 
	 * @param lineString
	 * @return
	 */
	public PolygonOptions toPolygon(Polygon polygon) {

		PolygonOptions polygonOptions = new PolygonOptions();

		List<LineString> rings = polygon.getRings();

		if (!rings.isEmpty()) {

			// Add the polygon points
			LineString polygonLineString = rings.get(0);
			for (Point point : polygonLineString.getPoints()) {
				LatLng latLng = toLatLng(point);
				polygonOptions.add(latLng);
			}

			// Add the holes
			for (int i = 1; i < rings.size(); i++) {
				LineString hole = rings.get(i);
				List<LatLng> holeLatLngs = new ArrayList<LatLng>();
				for (Point point : hole.getPoints()) {
					LatLng latLng = toLatLng(point);
					holeLatLngs.add(latLng);
				}
				polygonOptions.addHole(holeLatLngs);
			}
		}

		return polygonOptions;
	}

	/**
	 * Convert a {@link com.google.android.gms.maps.model.Polygon} to a
	 * {@link Polygon}
	 * 
	 * @param mapPolygon
	 * @return
	 */
	public Polygon toPolygon(com.google.android.gms.maps.model.Polygon polygon) {
		return toPolygon(polygon, false, false);
	}

	/**
	 * Convert a {@link com.google.android.gms.maps.model.Polygon} to a
	 * {@link Polygon}
	 * 
	 * @param polygon
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public Polygon toPolygon(com.google.android.gms.maps.model.Polygon polygon,
			boolean hasZ, boolean hasM) {
		return toPolygon(polygon.getPoints(), polygon.getHoles(), hasZ, hasM);
	}

	/**
	 * Convert a {@link com.google.android.gms.maps.model.Polygon} to a
	 * {@link Polygon}
	 * 
	 * @param polygon
	 * @return
	 */
	public Polygon toPolygon(PolygonOptions polygon) {
		return toPolygon(polygon, false, false);
	}

	/**
	 * Convert a {@link com.google.android.gms.maps.model.Polygon} to a
	 * {@link Polygon}
	 * 
	 * @param polygon
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public Polygon toPolygon(PolygonOptions polygon, boolean hasZ, boolean hasM) {
		return toPolygon(polygon.getPoints(), polygon.getHoles(), hasZ, hasM);
	}

	/**
	 * Convert a list of {@link LatLng} and list of hole list {@link LatLng} to
	 * a {@link Polygon}
	 * 
	 * @param latLngs
	 * @param holes
	 * @return
	 */
	public Polygon toPolygon(List<LatLng> latLngs, List<List<LatLng>> holes) {
		return toPolygon(latLngs, holes, false, false);
	}

	/**
	 * Convert a list of {@link LatLng} and list of hole list {@link LatLng} to
	 * a {@link Polygon}
	 * 
	 * @param latLngs
	 * @param holes
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public Polygon toPolygon(List<LatLng> latLngs, List<List<LatLng>> holes,
			boolean hasZ, boolean hasM) {

		Polygon polygon = new Polygon(hasZ, hasM);

		// Add the polygon points
		LineString polygonLineString = new LineString(hasZ, hasM);
		for (LatLng latLng : latLngs) {
			Point point = toPoint(latLng);
			polygonLineString.addPoint(point);
		}
		polygon.addRing(polygonLineString);

		// Add the holes
		for (List<LatLng> hole : holes) {

			LineString holeLineString = new LineString(hasZ, hasM);
			for (LatLng latLng : hole) {
				Point point = toPoint(latLng);
				holeLineString.addPoint(point);
			}
			polygon.addRing(holeLineString);
		}

		return polygon;
	}

	/**
	 * Convert a {@link MultiPoint} to a list of {@link LatLng}
	 * 
	 * @param multiPoint
	 * @return
	 */
	public List<LatLng> toLatLngs(MultiPoint multiPoint) {

		List<LatLng> points = new ArrayList<LatLng>();

		for (Point point : multiPoint.getPoints()) {
			LatLng latLng = toLatLng(point);
			points.add(latLng);
		}

		return points;
	}

	/**
	 * Convert a list of {@link LatLng} to a {@link MultiPoint}
	 * 
	 * @param latLngList
	 * @return
	 */
	public MultiPoint toMultiPoint(List<LatLng> latLngList) {
		return toMultiPoint(latLngList, false, false);
	}

	/**
	 * Convert a list of {@link LatLng} to a {@link MultiPoint}
	 * 
	 * @param latLngList
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public MultiPoint toMultiPoint(List<LatLng> latLngList, boolean hasZ,
			boolean hasM) {

		MultiPoint multiPoint = new MultiPoint(hasZ, hasM);

		for (LatLng latLng : latLngList) {
			Point point = toPoint(latLng);
			multiPoint.addPoint(point);
		}

		return multiPoint;
	}

	/**
	 * Convert a {@link MultiLineString} to a list of {@link PolylineOptions}
	 * 
	 * @param multiLineString
	 * @return
	 */
	public List<PolylineOptions> toPolylines(MultiLineString multiLineString) {

		List<PolylineOptions> polylines = new ArrayList<PolylineOptions>();

		for (LineString lineString : multiLineString.getLineStrings()) {
			PolylineOptions polyline = toPolyline(lineString);
			polylines.add(polyline);
		}

		return polylines;
	}

	/**
	 * Convert a list of {@link Polyline} to a {@link MultiLineString}
	 * 
	 * @param polylineList
	 * @return
	 */
	public MultiLineString toMultiLineString(List<Polyline> polylineList) {
		return toMultiLineString(polylineList, false, false);
	}

	/**
	 * Convert a list of {@link Polyline} to a {@link MultiLineString}
	 * 
	 * @param polylineList
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public MultiLineString toMultiLineString(List<Polyline> polylineList,
			boolean hasZ, boolean hasM) {

		MultiLineString multiLineString = new MultiLineString(hasZ, hasM);

		for (Polyline polyline : polylineList) {
			LineString lineString = toLineString(polyline);
			multiLineString.addLineString(lineString);
		}

		return multiLineString;
	}

	/**
	 * Convert a list of {@link PolylineOptions} to a {@link MultiLineString}
	 * 
	 * @param polylineList
	 * @return
	 */
	public MultiLineString toMultiLineStringFromOptions(
			List<PolylineOptions> polylineList) {
		return toMultiLineStringFromOptions(polylineList, false, false);
	}

	/**
	 * Convert a list of {@link PolylineOptions} to a {@link MultiLineString}
	 * 
	 * @param polylineList
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public MultiLineString toMultiLineStringFromOptions(
			List<PolylineOptions> polylineList, boolean hasZ, boolean hasM) {

		MultiLineString multiLineString = new MultiLineString(hasZ, hasM);

		for (PolylineOptions polyline : polylineList) {
			LineString lineString = toLineString(polyline);
			multiLineString.addLineString(lineString);
		}

		return multiLineString;
	}

	/**
	 * Convert a {@link MultiPolygon} to a list of {@link PolygonOptions}
	 * 
	 * @param multiPolygon
	 * @return
	 */
	public List<PolygonOptions> toPolygons(MultiPolygon multiPolygon) {

		List<PolygonOptions> polygons = new ArrayList<PolygonOptions>();

		for (Polygon polygon : multiPolygon.getPolygons()) {
			PolygonOptions polygonOptions = toPolygon(polygon);
			polygons.add(polygonOptions);
		}

		return polygons;
	}

	/**
	 * Convert a list of {@link Polygon} to a {@link MultiPolygon}
	 * 
	 * @param polygonList
	 * @return
	 */
	public MultiPolygon toMultiPolygon(
			List<com.google.android.gms.maps.model.Polygon> polygonList) {
		return toMultiPolygon(polygonList, false, false);
	}

	/**
	 * Convert a list of {@link Polygon} to a {@link MultiPolygon}
	 * 
	 * @param polygonList
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public MultiPolygon toMultiPolygon(
			List<com.google.android.gms.maps.model.Polygon> polygonList,
			boolean hasZ, boolean hasM) {

		MultiPolygon multiPolygon = new MultiPolygon(hasZ, hasM);

		for (com.google.android.gms.maps.model.Polygon mapPolygon : polygonList) {
			Polygon polygon = toPolygon(mapPolygon);
			multiPolygon.addPolygon(polygon);
		}

		return multiPolygon;
	}

	/**
	 * Convert a list of {@link PolygonOptions} to a {@link MultiPolygon}
	 * 
	 * @param polygonList
	 * @return
	 */
	public MultiPolygon toMultiPolygonFromOptions(
			List<PolygonOptions> polygonList) {
		return toMultiPolygonFromOptions(polygonList, false, false);
	}

	/**
	 * Convert a list of {@link PolygonOptions} to a {@link MultiPolygon}
	 * 
	 * @param polygonList
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public MultiPolygon toMultiPolygonFromOptions(
			List<PolygonOptions> polygonList, boolean hasZ, boolean hasM) {

		MultiPolygon multiPolygon = new MultiPolygon(hasZ, hasM);

		for (PolygonOptions mapPolygon : polygonList) {
			Polygon polygon = toPolygon(mapPolygon);
			multiPolygon.addPolygon(polygon);
		}

		return multiPolygon;
	}

	/**
	 * Convert a {@link CompoundCurve} to a list of {@link PolylineOptions}
	 * 
	 * @param compoundCurve
	 * @return
	 */
	public List<PolylineOptions> toPolylines(CompoundCurve compoundCurve) {

		List<PolylineOptions> polylines = new ArrayList<PolylineOptions>();

		for (LineString lineString : compoundCurve.getLineStrings()) {
			PolylineOptions polyline = toPolyline(lineString);
			polylines.add(polyline);
		}

		return polylines;
	}

	/**
	 * Convert a list of {@link Polyline} to a {@link CompoundCurve}
	 * 
	 * @param polylineList
	 * @return
	 */
	public CompoundCurve toCompoundCurve(List<Polyline> polylineList) {
		return toCompoundCurve(polylineList, false, false);
	}

	/**
	 * Convert a list of {@link Polyline} to a {@link CompoundCurve}
	 * 
	 * @param polylineList
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public CompoundCurve toCompoundCurve(List<Polyline> polylineList,
			boolean hasZ, boolean hasM) {

		CompoundCurve compoundCurve = new CompoundCurve(hasZ, hasM);

		for (Polyline polyline : polylineList) {
			LineString lineString = toLineString(polyline);
			compoundCurve.addLineString(lineString);
		}

		return compoundCurve;
	}

	/**
	 * Convert a list of {@link PolylineOptions} to a {@link CompoundCurve}
	 * 
	 * @param polylineList
	 * @return
	 */
	public CompoundCurve toCompoundCurveWithOptions(
			List<PolylineOptions> polylineList) {
		return toCompoundCurveWithOptions(polylineList, false, false);
	}

	/**
	 * Convert a list of {@link PolylineOptions} to a {@link CompoundCurve}
	 * 
	 * @param polylineList
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public CompoundCurve toCompoundCurveWithOptions(
			List<PolylineOptions> polylineList, boolean hasZ, boolean hasM) {

		CompoundCurve compoundCurve = new CompoundCurve(hasZ, hasM);

		for (PolylineOptions polyline : polylineList) {
			LineString lineString = toLineString(polyline);
			compoundCurve.addLineString(lineString);
		}

		return compoundCurve;
	}

	/**
	 * Convert a {@link PolyhedralSurface} to a list of {@link PolygonOptions}
	 * 
	 * @param polyhedralSurface
	 * @return
	 */
	public List<PolygonOptions> toPolygons(PolyhedralSurface polyhedralSurface) {

		List<PolygonOptions> polygons = new ArrayList<PolygonOptions>();

		for (Polygon polygon : polyhedralSurface.getPolygons()) {
			PolygonOptions polygonOptions = toPolygon(polygon);
			polygons.add(polygonOptions);
		}

		return polygons;
	}

	/**
	 * Convert a list of {@link Polygon} to a {@link PolyhedralSurface}
	 * 
	 * @param polygonList
	 * @return
	 */
	public PolyhedralSurface toPolyhedralSurface(
			List<com.google.android.gms.maps.model.Polygon> polygonList) {
		return toPolyhedralSurface(polygonList, false, false);
	}

	/**
	 * Convert a list of {@link Polygon} to a {@link PolyhedralSurface}
	 * 
	 * @param polygonList
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public PolyhedralSurface toPolyhedralSurface(
			List<com.google.android.gms.maps.model.Polygon> polygonList,
			boolean hasZ, boolean hasM) {

		PolyhedralSurface polyhedralSurface = new PolyhedralSurface(hasZ, hasM);

		for (com.google.android.gms.maps.model.Polygon mapPolygon : polygonList) {
			Polygon polygon = toPolygon(mapPolygon);
			polyhedralSurface.addPolygon(polygon);
		}

		return polyhedralSurface;
	}

	/**
	 * Convert a list of {@link PolygonOptions} to a {@link PolyhedralSurface}
	 * 
	 * @param polygonList
	 * @return
	 */
	public PolyhedralSurface toPolyhedralSurfaceWithOptions(
			List<PolygonOptions> polygonList) {
		return toPolyhedralSurfaceWithOptions(polygonList, false, false);
	}

	/**
	 * Convert a list of {@link PolygonOptions} to a {@link PolyhedralSurface}
	 * 
	 * @param polygonList
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public PolyhedralSurface toPolyhedralSurfaceWithOptions(
			List<PolygonOptions> polygonList, boolean hasZ, boolean hasM) {

		PolyhedralSurface polyhedralSurface = new PolyhedralSurface(hasZ, hasM);

		for (PolygonOptions mapPolygon : polygonList) {
			Polygon polygon = toPolygon(mapPolygon);
			polyhedralSurface.addPolygon(polygon);
		}

		return polyhedralSurface;
	}

	/**
	 * Convert a {@link Geometry} to a Map shape
	 * 
	 * @param geometry
	 * @return
	 */
	public Object toShape(Geometry geometry) {

		Object shape = null;

		GeometryType geometryType = geometry.getGeometryType();
		switch (geometryType) {
		case POINT:
			shape = toLatLng((Point) geometry);
			break;
		case LINESTRING:
			shape = toPolyline((LineString) geometry);
			break;
		case POLYGON:
			shape = toPolygon((Polygon) geometry);
			break;
		case MULTIPOINT:
			shape = toLatLngs((MultiPoint) geometry);
			break;
		case MULTILINESTRING:
			shape = toPolylines((MultiLineString) geometry);
			break;
		case MULTIPOLYGON:
			shape = toPolygons((MultiPolygon) geometry);
			break;
		case CIRCULARSTRING:
			shape = toPolyline((CircularString) geometry);
			break;
		case COMPOUNDCURVE:
			shape = toPolylines((CompoundCurve) geometry);
			break;
		case POLYHEDRALSURFACE:
			shape = toPolygons((PolyhedralSurface) geometry);
			break;
		case TIN:
			shape = toPolygons((TIN) geometry);
			break;
		case TRIANGLE:
			shape = toPolygon((Triangle) geometry);
			break;
		default:
			throw new GeoPackageException("Unsupported Geometry Type: "
					+ geometryType.getName());
		}

		return shape;
	}

	/**
	 * Convert a {@link GeometryCollection} to a list of Map shapes
	 * 
	 * @param geometryCollection
	 * @return
	 */
	public List<Object> toShapes(GeometryCollection<Geometry> geometryCollection) {

		List<Object> shapes = new ArrayList<Object>();

		for (Geometry geometry : geometryCollection.getGeometries()) {
			Object shape = toShape(geometry);
			shapes.add(shape);
		}

		return shapes;
	}
}

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
import mil.nga.giat.geopackage.geom.unit.CoordinateConverter;
import mil.nga.giat.geopackage.geom.unit.DegreeConverter;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
	 * Coordinate converter
	 */
	private final CoordinateConverter coordinateConverter;

	/**
	 * Constructor, uses default {@link DegreeConverter}
	 */
	public GoogleMapShapeConverter() {
		this(new DegreeConverter());
	}

	/**
	 * Constructor with specified coordinate converter
	 * 
	 * @param coordinateConverter
	 */
	public GoogleMapShapeConverter(CoordinateConverter coordinateConverter) {
		this.coordinateConverter = coordinateConverter;
	}

	/**
	 * Convert a {@link Point} to a {@link LatLng}
	 * 
	 * @param point
	 * @return
	 */
	public LatLng toLatLng(Point point) {
		double latitude = coordinateConverter.toDegrees(point.getY());
		double longitude = coordinateConverter.toDegrees(point.getX());
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
		double y = coordinateConverter.degreesToUnits(latLng.latitude);
		double x = coordinateConverter.degreesToUnits(latLng.longitude);
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
		Double z = null;

		for (Point point : lineString.getPoints()) {
			LatLng latLng = toLatLng(point);
			polylineOptions.add(latLng);
			if (point.hasZ()) {
				z = (z == null) ? point.getZ() : Math.max(z, point.getZ());
			}
		}

		if (lineString.hasZ() && z != null) {
			polylineOptions.zIndex(z.floatValue());
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

			Double z = null;

			// Add the polygon points
			LineString polygonLineString = rings.get(0);
			for (Point point : polygonLineString.getPoints()) {
				LatLng latLng = toLatLng(point);
				polygonOptions.add(latLng);
				if (point.hasZ()) {
					z = (z == null) ? point.getZ() : Math.max(z, point.getZ());
				}
			}

			// Add the holes
			for (int i = 1; i < rings.size(); i++) {
				LineString hole = rings.get(i);
				List<LatLng> holeLatLngs = new ArrayList<LatLng>();
				for (Point point : hole.getPoints()) {
					LatLng latLng = toLatLng(point);
					holeLatLngs.add(latLng);
					if (point.hasZ()) {
						z = (z == null) ? point.getZ() : Math.max(z,
								point.getZ());
					}
				}
				polygonOptions.addHole(holeLatLngs);
			}

			if (polygon.hasZ() && z != null) {
				polygonOptions.zIndex(z.floatValue());
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
	 * Convert a {@link MultiPoint} to a {@link MultiLatLng}
	 * 
	 * @param multiPoint
	 * @return
	 */
	public MultiLatLng toLatLngs(MultiPoint multiPoint) {

		MultiLatLng multiLatLng = new MultiLatLng();

		for (Point point : multiPoint.getPoints()) {
			LatLng latLng = toLatLng(point);
			multiLatLng.add(latLng);
		}

		return multiLatLng;
	}

	/**
	 * Convert a {@link MultiLatLng} to a {@link MultiPoint}
	 * 
	 * @param latLngs
	 * @return
	 */
	public MultiPoint toMultiPoint(MultiLatLng latLngs) {
		return toMultiPoint(latLngs, false, false);
	}

	/**
	 * Convert a {@link MultiLatLng} to a {@link MultiPoint}
	 * 
	 * @param latLngs
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public MultiPoint toMultiPoint(MultiLatLng latLngs, boolean hasZ,
			boolean hasM) {

		MultiPoint multiPoint = new MultiPoint(hasZ, hasM);

		for (LatLng latLng : latLngs.getLatLngs()) {
			Point point = toPoint(latLng);
			multiPoint.addPoint(point);
		}

		return multiPoint;
	}

	/**
	 * Convert a {@link MultiLineString} to a {@link MultiPolylineOptions}
	 * 
	 * @param multiLineString
	 * @return
	 */
	public MultiPolylineOptions toPolylines(MultiLineString multiLineString) {

		MultiPolylineOptions polylines = new MultiPolylineOptions();

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
	 * Convert a {@link MultiPolylineOptions} to a {@link MultiLineString}
	 * 
	 * @param multiPolylineOptions
	 * @return
	 */
	public MultiLineString toMultiLineStringFromOptions(
			MultiPolylineOptions multiPolylineOptions) {
		return toMultiLineStringFromOptions(multiPolylineOptions, false, false);
	}

	/**
	 * Convert a {@link MultiPolylineOptions} to a {@link MultiLineString}
	 * 
	 * @param multiPolylineOptions
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public MultiLineString toMultiLineStringFromOptions(
			MultiPolylineOptions multiPolylineOptions, boolean hasZ,
			boolean hasM) {

		MultiLineString multiLineString = new MultiLineString(hasZ, hasM);

		for (PolylineOptions polyline : multiPolylineOptions
				.getPolylineOptions()) {
			LineString lineString = toLineString(polyline);
			multiLineString.addLineString(lineString);
		}

		return multiLineString;
	}

	/**
	 * Convert a {@link MultiPolygon} to a {@link MultiPolygonOptions}
	 * 
	 * @param multiPolygon
	 * @return
	 */
	public MultiPolygonOptions toPolygons(MultiPolygon multiPolygon) {

		MultiPolygonOptions polygons = new MultiPolygonOptions();

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
	 * Convert a {@link MultiPolygonOptions} to a {@link MultiPolygon}
	 * 
	 * @param multiPolygonOptions
	 * @return
	 */
	public MultiPolygon toMultiPolygonFromOptions(
			MultiPolygonOptions multiPolygonOptions) {
		return toMultiPolygonFromOptions(multiPolygonOptions, false, false);
	}

	/**
	 * Convert a list of {@link PolygonOptions} to a {@link MultiPolygon}
	 * 
	 * @param multiPolygonOptions
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public MultiPolygon toMultiPolygonFromOptions(
			MultiPolygonOptions multiPolygonOptions, boolean hasZ, boolean hasM) {

		MultiPolygon multiPolygon = new MultiPolygon(hasZ, hasM);

		for (PolygonOptions mapPolygon : multiPolygonOptions
				.getPolygonOptions()) {
			Polygon polygon = toPolygon(mapPolygon);
			multiPolygon.addPolygon(polygon);
		}

		return multiPolygon;
	}

	/**
	 * Convert a {@link CompoundCurve} to a {@link MultiPolylineOptions}
	 * 
	 * @param compoundCurve
	 * @return
	 */
	public MultiPolylineOptions toPolylines(CompoundCurve compoundCurve) {

		MultiPolylineOptions polylines = new MultiPolylineOptions();

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
	 * Convert a {@link MultiPolylineOptions} to a {@link CompoundCurve}
	 * 
	 * @param multiPolylineOptions
	 * @return
	 */
	public CompoundCurve toCompoundCurveWithOptions(
			MultiPolylineOptions multiPolylineOptions) {
		return toCompoundCurveWithOptions(multiPolylineOptions, false, false);
	}

	/**
	 * Convert a {@link MultiPolylineOptions} to a {@link CompoundCurve}
	 * 
	 * @param multiPolylineOptions
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public CompoundCurve toCompoundCurveWithOptions(
			MultiPolylineOptions multiPolylineOptions, boolean hasZ,
			boolean hasM) {

		CompoundCurve compoundCurve = new CompoundCurve(hasZ, hasM);

		for (PolylineOptions polyline : multiPolylineOptions
				.getPolylineOptions()) {
			LineString lineString = toLineString(polyline);
			compoundCurve.addLineString(lineString);
		}

		return compoundCurve;
	}

	/**
	 * Convert a {@link PolyhedralSurface} to a {@link MultiPolygonOptions}
	 * 
	 * @param polyhedralSurface
	 * @return
	 */
	public MultiPolygonOptions toPolygons(PolyhedralSurface polyhedralSurface) {

		MultiPolygonOptions polygons = new MultiPolygonOptions();

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
	 * Convert a {@link MultiPolygonOptions} to a {@link PolyhedralSurface}
	 * 
	 * @param multiPolygonOptions
	 * @return
	 */
	public PolyhedralSurface toPolyhedralSurfaceWithOptions(
			MultiPolygonOptions multiPolygonOptions) {
		return toPolyhedralSurfaceWithOptions(multiPolygonOptions, false, false);
	}

	/**
	 * Convert a {@link MultiPolygonOptions} to a {@link PolyhedralSurface}
	 * 
	 * @param multiPolygonOptions
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public PolyhedralSurface toPolyhedralSurfaceWithOptions(
			MultiPolygonOptions multiPolygonOptions, boolean hasZ, boolean hasM) {

		PolyhedralSurface polyhedralSurface = new PolyhedralSurface(hasZ, hasM);

		for (PolygonOptions mapPolygon : multiPolygonOptions
				.getPolygonOptions()) {
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
	@SuppressWarnings("unchecked")
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
		case GEOMETRYCOLLECTION:
			shape = toShapes((GeometryCollection<Geometry>) geometry);
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

	/**
	 * Convert a {@link Geometry} to a Map shape and add it
	 * 
	 * @param map
	 * @param geometry
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object addToMap(GoogleMap map, Geometry geometry) {

		Object shape = null;

		GeometryType geometryType = geometry.getGeometryType();
		switch (geometryType) {
		case POINT:
			shape = addLatLngToMap(map, toLatLng((Point) geometry));
			break;
		case LINESTRING:
			shape = addPolylineToMap(map, toPolyline((LineString) geometry));
			break;
		case POLYGON:
			shape = addPolygonToMap(map, toPolygon((Polygon) geometry));
			break;
		case MULTIPOINT:
			shape = addLatLngsToMap(map, toLatLngs((MultiPoint) geometry));
			break;
		case MULTILINESTRING:
			shape = addPolylinesToMap(map,
					toPolylines((MultiLineString) geometry));
			break;
		case MULTIPOLYGON:
			shape = addPolygonsToMap(map, toPolygons((MultiPolygon) geometry));
			break;
		case CIRCULARSTRING:
			shape = addPolylineToMap(map, toPolyline((CircularString) geometry));
			break;
		case COMPOUNDCURVE:
			shape = addPolylinesToMap(map,
					toPolylines((CompoundCurve) geometry));
			break;
		case POLYHEDRALSURFACE:
			shape = addPolygonsToMap(map,
					toPolygons((PolyhedralSurface) geometry));
			break;
		case TIN:
			shape = addPolygonsToMap(map, toPolygons((TIN) geometry));
			break;
		case TRIANGLE:
			shape = addPolygonToMap(map, toPolygon((Triangle) geometry));
			break;
		case GEOMETRYCOLLECTION:
			shape = addToMap(map, (GeometryCollection<Geometry>) geometry);
			break;
		default:
			throw new GeoPackageException("Unsupported Geometry Type: "
					+ geometryType.getName());
		}

		return shape;
	}

	/**
	 * Add a shape to the map
	 * 
	 * @param map
	 * @param shape
	 * @return
	 */
	public Object addShapeToMap(GoogleMap map, Object shape) {

		Object addedShape = null;

		if (shape instanceof LatLng) {
			addedShape = addLatLngToMap(map, (LatLng) shape);
		} else if (shape instanceof PolylineOptions) {
			addedShape = addPolylineToMap(map, (PolylineOptions) shape);
		} else if (shape instanceof PolygonOptions) {
			addedShape = addPolygonToMap(map, (PolygonOptions) shape);
		} else if (shape instanceof MultiLatLng) {
			addedShape = addLatLngsToMap(map, (MultiLatLng) shape);
		} else if (shape instanceof MultiPolylineOptions) {
			addedShape = addPolylinesToMap(map, (MultiPolylineOptions) shape);
		} else if (shape instanceof MultiPolygonOptions) {
			addedShape = addPolygonsToMap(map, (MultiPolygonOptions) shape);
		} else if (shape instanceof List<?>) {
			List<Object> addedShapeList = new ArrayList<Object>();
			List<?> shapeList = (List<?>) shape;
			for (Object shapeListItem : shapeList) {
				addedShapeList.add(addShapeToMap(map, shapeListItem));
			}
			addedShape = addedShapeList;
		} else {
			throw new GeoPackageException("Unsupported Shape Type: "
					+ shape.getClass().toString());
		}

		return addedShape;
	}

	/**
	 * Add a LatLng to the map
	 * 
	 * @param map
	 * @param latLng
	 * @return
	 */
	public Marker addLatLngToMap(GoogleMap map, LatLng latLng) {
		return addLatLngToMap(map, latLng, new MarkerOptions());
	}

	/**
	 * Add a LatLng to the map
	 * 
	 * @param map
	 * @param latLng
	 * @param options
	 * @return
	 */
	public Marker addLatLngToMap(GoogleMap map, LatLng latLng,
			MarkerOptions options) {
		return map.addMarker(options.position(latLng));
	}

	/**
	 * Add a Polyline to the map
	 * 
	 * @param map
	 * @param polyline
	 * @return
	 */
	public Polyline addPolylineToMap(GoogleMap map, PolylineOptions polyline) {
		return map.addPolyline(polyline);
	}

	/**
	 * Add a Polygon to the map
	 * 
	 * @param map
	 * @param polygon
	 * @return
	 */
	public com.google.android.gms.maps.model.Polygon addPolygonToMap(
			GoogleMap map, PolygonOptions polygon) {
		return map.addPolygon(polygon);
	}

	/**
	 * Add a list of LatLngs to the map
	 * 
	 * @param map
	 * @param latLngs
	 * @return
	 */
	public List<Marker> addLatLngsToMap(GoogleMap map, MultiLatLng latLngs) {
		List<Marker> markers = new ArrayList<Marker>();
		for (LatLng latLng : latLngs.getLatLngs()) {
			Marker marker = addLatLngToMap(map, latLng);
			markers.add(marker);
		}
		return markers;
	}

	/**
	 * Add a list of Polylines to the map
	 * 
	 * @param map
	 * @param polylines
	 * @return
	 */
	public List<Polyline> addPolylinesToMap(GoogleMap map,
			MultiPolylineOptions polylines) {
		List<Polyline> polylineList = new ArrayList<Polyline>();
		for (PolylineOptions polylineOption : polylines.getPolylineOptions()) {
			Polyline polyline = addPolylineToMap(map, polylineOption);
			polylineList.add(polyline);
		}
		return polylineList;
	}

	/**
	 * Add a list of Polygons to the map
	 * 
	 * @param map
	 * @param polygons
	 * @return
	 */
	public List<com.google.android.gms.maps.model.Polygon> addPolygonsToMap(
			GoogleMap map, MultiPolygonOptions polygons) {
		List<com.google.android.gms.maps.model.Polygon> polygonList = new ArrayList<com.google.android.gms.maps.model.Polygon>();
		for (PolygonOptions polygonOption : polygons.getPolygonOptions()) {
			com.google.android.gms.maps.model.Polygon polygon = addPolygonToMap(
					map, polygonOption);
			polygonList.add(polygon);
		}
		return polygonList;
	}

	/**
	 * Convert a {@link GeometryCollection} to a list of Map shapes and add to
	 * the map
	 * 
	 * @param map
	 * @param geometryCollection
	 * @return
	 */
	public List<Object> addToMap(GoogleMap map,
			GeometryCollection<Geometry> geometryCollection) {

		List<Object> shapes = new ArrayList<Object>();

		for (Geometry geometry : geometryCollection.getGeometries()) {
			Object shape = addToMap(map, geometry);
			shapes.add(shape);
		}

		return shapes;
	}

}

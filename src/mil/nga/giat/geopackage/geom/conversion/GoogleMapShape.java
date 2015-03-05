package mil.nga.giat.geopackage.geom.conversion;

import java.util.List;

import mil.nga.giat.geopackage.geom.GeometryType;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;

/**
 * Google Map Shape
 * 
 * @author osbornb
 */
public class GoogleMapShape {

	/**
	 * Geometry type
	 */
	private GeometryType geometryType;

	/**
	 * Shape type
	 */
	private GoogleMapShapeType shapeType;

	/**
	 * Shape objects
	 */
	private Object shape;

	/**
	 * Constructor
	 * 
	 * @param geometryType
	 * @param shapeType
	 * @param shape
	 */
	public GoogleMapShape(GeometryType geometryType,
			GoogleMapShapeType shapeType, Object shape) {
		this.geometryType = geometryType;
		this.shapeType = shapeType;
		this.shape = shape;
	}

	public GeometryType getGeometryType() {
		return geometryType;
	}

	public void setGeometryType(GeometryType geometryType) {
		this.geometryType = geometryType;
	}

	public GoogleMapShapeType getShapeType() {
		return shapeType;
	}

	public void setShapeType(GoogleMapShapeType shapeType) {
		this.shapeType = shapeType;
	}

	public Object getShape() {
		return shape;
	}

	public void setShape(Object shape) {
		this.shape = shape;
	}

	/**
	 * Removes all objects added to the map
	 */
	public void remove() {

		switch (shapeType) {

		case MARKER:
			((Marker) shape).remove();
			break;
		case POLYGON:
			((Polygon) shape).remove();
			break;
		case POLYLINE:
			((Polyline) shape).remove();
			break;
		case MULTI_MARKER:
			((MultiMarker) shape).remove();
			break;
		case MULTI_POLYLINE:
			((MultiPolyline) shape).remove();
			break;
		case MULTI_POLYGON:
			((MultiPolygon) shape).remove();
			break;
		case POLYLINE_MARKERS:
			((PolylineMarkers) shape).remove();
			break;
		case POLYGON_MARKERS:
			((PolygonMarkers) shape).remove();
			break;
		case MULTI_POLYLINE_MARKERS:
			((MultiPolylineMarkers) shape).remove();
			break;
		case MULTI_POLYGON_MARKERS:
			((MultiPolygonMarkers) shape).remove();
			break;
		case COLLECTION:
			@SuppressWarnings("unchecked")
			List<GoogleMapShape> shapeList = (List<GoogleMapShape>) shape;
			for (GoogleMapShape shapeListItem : shapeList) {
				shapeListItem.remove();
			}
			break;
		default:
		}

	}

	/**
	 * Updates all objects that could have changed from moved markers
	 */
	public void update() {

		switch (shapeType) {

		case POLYLINE_MARKERS:
			((PolylineMarkers) shape).update();
			break;
		case POLYGON_MARKERS:
			((PolygonMarkers) shape).update();
			break;
		case MULTI_POLYLINE_MARKERS:
			((MultiPolylineMarkers) shape).update();
			break;
		case MULTI_POLYGON_MARKERS:
			((MultiPolygonMarkers) shape).update();
			break;
		case COLLECTION:
			@SuppressWarnings("unchecked")
			List<GoogleMapShape> shapeList = (List<GoogleMapShape>) shape;
			for (GoogleMapShape shapeListItem : shapeList) {
				shapeListItem.update();
			}
			break;
		default:
		}

	}

}

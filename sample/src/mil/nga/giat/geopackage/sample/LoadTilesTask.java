package mil.nga.giat.geopackage.sample;

import mil.nga.giat.geopackage.BoundingBox;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.factory.GeoPackageFactory;
import mil.nga.giat.geopackage.io.GeoPackageProgress;
import mil.nga.giat.geopackage.tiles.TileGenerator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;

/**
 * Load tiles task
 * 
 * @author osbornb
 */
public class LoadTilesTask extends AsyncTask<String, Integer, String> implements
		GeoPackageProgress {

	/**
	 * Load tiles from a URL
	 * 
	 * @param database
	 * @param tableName
	 * @param tileUrl
	 * @param minZoom
	 * @param maxZoom
	 * @param compressFormat
	 * @param compressQuality
	 * @param boundingBox
	 */
	public static void loadTiles(Activity activity, ILoadTilesTask callback,
			GeoPackageDatabases active, String database, String tableName,
			String tileUrl, int minZoom, int maxZoom,
			CompressFormat compressFormat, Integer compressQuality,
			BoundingBox boundingBox) {

		if (minZoom > maxZoom) {
			throw new GeoPackageException(
					activity.getString(R.string.load_tiles_min_zoom_label)
							+ " can not be larger than "
							+ activity
									.getString(R.string.load_tiles_max_zoom_label));
		}

		ProgressDialog progressDialog = new ProgressDialog(activity);
		final LoadTilesTask loadTilesTask = new LoadTilesTask(callback,
				progressDialog, active);

		GeoPackageManager manager = GeoPackageFactory.getManager(activity);
		GeoPackage geoPackage = manager.open(database);
		TileGenerator tileGenerator = new TileGenerator(activity, geoPackage,
				tableName, tileUrl, minZoom, maxZoom);
		tileGenerator.setCompressFormat(compressFormat);
		tileGenerator.setCompressQuality(compressQuality);
		tileGenerator.setTileBoundingBox(boundingBox);
		tileGenerator.setProgress(loadTilesTask);

		loadTilesTask.setTileGenerator(tileGenerator);

		progressDialog.setMessage(activity
				.getString(R.string.geopackage_create_tiles_label)
				+ ": "
				+ database + " - " + tableName);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setIndeterminate(false);
		progressDialog.setMax(tileGenerator.getTileCount());
		progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
				activity.getString(R.string.button_cancel_label),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						loadTilesTask.cancel(true);
					}
				});

		loadTilesTask.execute();
	}

	private Integer max = null;
	private int progress = 0;
	private TileGenerator tileGenerator;
	private ILoadTilesTask callback;
	private ProgressDialog progressDialog;
	private GeoPackageDatabases active;

	/**
	 * Constructor
	 * 
	 * @param callback
	 * @param progressDialog
	 * @param active
	 */
	public LoadTilesTask(ILoadTilesTask callback,
			ProgressDialog progressDialog, GeoPackageDatabases active) {
		this.callback = callback;
		this.progressDialog = progressDialog;
		this.active = active;
	}

	/**
	 * Set the tile generator
	 * 
	 * @param tileGenerator
	 */
	public void setTileGenerator(TileGenerator tileGenerator) {
		this.tileGenerator = tileGenerator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addProgress(int progress) {
		this.progress += progress;
		publishProgress(this.progress);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() {
		return !isCancelled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cleanupOnCancel() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog.show();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		progressDialog.setProgress(progress[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCancelled(String result) {
		tileGenerator.close();
		progressDialog.dismiss();
		callback.onLoadTilesCancelled(result);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostExecute(String result) {
		tileGenerator.close();
		progressDialog.dismiss();
		callback.onLoadTilesPostExecute(result);
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			int count = tileGenerator.generateTiles();
			if (count > 0) {
				active.setModified(true);
			}
			if (count < max) {
				return "Fewer tiles were generated than expected. Expected: "
						+ max + ", Actual: " + count;
			}
		} catch (final Exception e) {
			return e.toString();
		}
		return null;
	}

}

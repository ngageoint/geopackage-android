package mil.nga.giat.geopackage;

import java.sql.SQLException;
import java.util.List;

import mil.nga.giat.geopackage.data.c1.SfSqlSpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.datastore.GeopackageSQLiteOpenHelper;
import android.app.Activity;
import android.os.Bundle;

import com.j256.ormlite.dao.BaseDaoImpl;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// force the database to init.
		GeopackageSQLiteOpenHelper dbOpener = new GeopackageSQLiteOpenHelper(
				getApplicationContext(), "geopackage.db", null, 1);

		GeoPackageManager manager = new GeoPackageManager(this);

		List<String> dbs = manager.databaseList();

		boolean exists = manager.exists(TempTests.exampleDb);

		TempTests.copySampleToInternalStorage(this);

		String path = TempTests.getExampleFilePath(this);
		boolean imported = false;
		try {
			imported = manager.load(path);
		} catch (GeoPackageException e) {
			e.printStackTrace();
		}

		exists = manager.exists(TempTests.exampleDb);

		GeoPackage geopackage = manager.open(TempTests.exampleDb);

		BaseDaoImpl<SpatialReferenceSystem, Integer> dao = null;
		List<SpatialReferenceSystem> results = null;
		try {
			dao = geopackage.spatialReferenceSystemDao();

			results = dao.queryForAll();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

//		BaseDaoImpl<SfSqlSpatialReferenceSystem, Integer> sfSqlSpatialRefSysDao = null;
//		List<SfSqlSpatialReferenceSystem> results2 = null;
//		try {
//			sfSqlSpatialRefSysDao = geopackage.sfSqlSpatialReferenceSystemDao();
//
//			results2 = sfSqlSpatialRefSysDao.queryForAll();
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		geopackage.close();

		imported = false;
		try {
			imported = manager.load(path);
		} catch (GeoPackageException e) {
			e.printStackTrace();
		}

		boolean deleted = manager.delete(TempTests.exampleDb);

		exists = manager.exists(TempTests.exampleDb);

		String newDb = "tester";
		
		boolean created = manager.create(newDb);

		exists = manager.exists(newDb);

		geopackage = manager.open(newDb);

		dao = null;
		results = null;
		try {
			dao = geopackage.spatialReferenceSystemDao();

			results = dao.queryForAll();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		BaseDaoImpl<SfSqlSpatialReferenceSystem, Integer> sfSqlSpatialRefSysDao = null;
		List<SfSqlSpatialReferenceSystem> results2 = null;
		try {
			sfSqlSpatialRefSysDao = geopackage.sfSqlSpatialReferenceSystemDao();

			results2 = sfSqlSpatialRefSysDao.queryForAll();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		geopackage.close();
		
		deleted = manager.delete("tester");

		exists = manager.exists("tester");

		System.out.println("DONE");
	}

}

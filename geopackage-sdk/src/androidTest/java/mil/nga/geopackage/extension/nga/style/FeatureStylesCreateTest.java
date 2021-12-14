package mil.nga.geopackage.extension.nga.style;

import android.content.pm.PackageManager.NameNotFoundException;

import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.CreateGeoPackageTestCase;

/**
 * Test Feature Styles from a created database
 *
 * @author osbornb
 */
public class FeatureStylesCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureStylesCreateTest() {

    }

    /**
     * Test feature styles
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testFeatureStyles() throws SQLException, IOException, NameNotFoundException {

        FeatureStylesUtils.testFeatureStyles(geoPackage);

    }

}

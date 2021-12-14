package mil.nga.geopackage.extension.nga.style;

import android.content.pm.PackageManager.NameNotFoundException;

import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.ImportGeoPackageTestCase;

/**
 * Test Feature Styles from an imported database
 *
 * @author osbornb
 */
public class FeatureStylesImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureStylesImportTest() {

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

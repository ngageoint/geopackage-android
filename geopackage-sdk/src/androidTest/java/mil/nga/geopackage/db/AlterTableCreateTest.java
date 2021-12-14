package mil.nga.geopackage.db;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.CreateGeoPackageTestCase;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomTable;

/**
 * Alter Table Create Test
 *
 * @author osbornb
 */
public class AlterTableCreateTest extends CreateGeoPackageTestCase {

    /**
     * Test column alters
     *
     * @throws SQLException upon error
     */
    @Test
    public void testColumns() throws SQLException {
        AlterTableUtils.testColumns(activity, geoPackage);
    }

    /**
     * Test copy feature table
     *
     * @throws SQLException upon error
     */
    @Test
    public void testCopyFeatureTable() throws SQLException {
        AlterTableUtils.testCopyFeatureTable(activity, geoPackage);
    }

    /**
     * Test copy tile table
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    @Test
    public void testCopyTileTable() throws SQLException, IOException {
        AlterTableUtils.testCopyTileTable(activity, geoPackage);
    }

    /**
     * Test copy attributes table
     *
     * @throws SQLException upon error
     */
    @Test
    public void testCopyAttributesTable() throws SQLException {
        AlterTableUtils.testCopyAttributesTable(activity, geoPackage);
    }

    /**
     * Test copy user table
     *
     * @throws SQLException upon error
     */
    @Test
    public void testCopyUserTable() throws SQLException {
        AlterTableUtils.testCopyUserTable(activity, geoPackage);
    }

    /**
     * Test alter column
     *
     * @throws SQLException upon error
     */
    @Test
    public void testAlterColumn() throws SQLException {

        String tableName = "user_test_table";
        String columnName = "column";
        int countCount = 0;

        List<UserCustomColumn> columns = new ArrayList<>();
        columns.add(UserCustomColumn
                .createPrimaryKeyColumn(columnName + ++countCount));
        UserCustomColumn column2 = UserCustomColumn.createColumn(
                columnName + ++countCount, GeoPackageDataType.TEXT, true);
        column2.addUniqueConstraint();
        columns.add(column2);
        columns.add(UserCustomColumn.createColumn(columnName + ++countCount,
                GeoPackageDataType.TEXT, true, "default_value"));
        columns.add(UserCustomColumn.createColumn(columnName + ++countCount,
                GeoPackageDataType.BOOLEAN));
        columns.add(UserCustomColumn.createColumn(columnName + ++countCount,
                GeoPackageDataType.DOUBLE));
        UserCustomColumn column6 = UserCustomColumn.createColumn(
                columnName + ++countCount, GeoPackageDataType.INTEGER, true);
        column6.addConstraint("CONSTRAINT check_constraint CHECK ("
                + (columnName + countCount) + " >= 0)");
        columns.add(column6);
        UserCustomColumn column7 = UserCustomColumn.createColumn(
                columnName + ++countCount, GeoPackageDataType.INTEGER);
        column7.addConstraint("CONSTRAINT another_check_constraint_13 CHECK ("
                + (columnName + countCount) + " >= 0)");
        columns.add(column7);

        UserCustomTable table = new UserCustomTable(tableName, columns);

        geoPackage.createUserTable(table);

        // Double column not null constraint
        UserCustomColumn doubleColumn = geoPackage.getUserCustomDao(tableName)
                .getTable().getColumn(4);
        TestCase.assertEquals(GeoPackageDataType.DOUBLE,
                doubleColumn.getDataType());
        TestCase.assertFalse(doubleColumn.isNotNull());

        // Double not null
        doubleColumn.addNotNullConstraint();
        AlterTable.alterColumn(geoPackage.getDatabase(), tableName,
                doubleColumn);
        doubleColumn = geoPackage.getUserCustomDao(tableName).getTable()
                .getColumn(4);
        TestCase.assertTrue(doubleColumn.isNotNull());

        // Double not null removed
        doubleColumn.setNotNull(false);
        AlterTable.alterColumn(geoPackage.getDatabase(), tableName,
                doubleColumn);
        doubleColumn = geoPackage.getUserCustomDao(tableName).getTable()
                .getColumn(4);
        TestCase.assertFalse(doubleColumn.isNotNull());

        // Boolean default value constraint
        UserCustomColumn booleanColumn = geoPackage.getUserCustomDao(tableName)
                .getTable().getColumn(3);
        TestCase.assertEquals(GeoPackageDataType.BOOLEAN,
                booleanColumn.getDataType());
        TestCase.assertFalse(booleanColumn.hasDefaultValue());

        // Default boolean value of true
        booleanColumn.setDefaultValue(true);
        AlterTable.alterColumn(geoPackage.getDatabase(), tableName,
                booleanColumn);
        booleanColumn = geoPackage.getUserCustomDao(tableName).getTable()
                .getColumn(3);
        TestCase.assertTrue(booleanColumn.hasDefaultValue());
        TestCase.assertTrue((Boolean) booleanColumn.getDefaultValue());

        // Default boolean value of false
        booleanColumn.setDefaultValue(false);
        AlterTable.alterColumn(geoPackage.getDatabase(), tableName,
                booleanColumn);
        booleanColumn = geoPackage.getUserCustomDao(tableName).getTable()
                .getColumn(3);
        TestCase.assertTrue(booleanColumn.hasDefaultValue());
        TestCase.assertFalse((Boolean) booleanColumn.getDefaultValue());

        // Default boolean value removed
        booleanColumn.setDefaultValue(null);
        AlterTable.alterColumn(geoPackage.getDatabase(), tableName,
                booleanColumn);
        booleanColumn = geoPackage.getUserCustomDao(tableName).getTable()
                .getColumn(3);
        TestCase.assertFalse(booleanColumn.hasDefaultValue());
        TestCase.assertNull(booleanColumn.getDefaultValue());

        // Primary key
        UserCustomColumn pkColumn = geoPackage.getUserCustomDao(tableName)
                .getTable().getColumn(0);
        TestCase.assertEquals(GeoPackageDataType.INTEGER,
                pkColumn.getDataType());
        TestCase.assertTrue(pkColumn.isPrimaryKey());
        TestCase.assertTrue(pkColumn.isAutoincrement());

        // Autoincrement false
        pkColumn.setAutoincrement(false);
        AlterTable.alterColumn(geoPackage.getDatabase(), tableName, pkColumn);
        pkColumn = geoPackage.getUserCustomDao(tableName).getTable()
                .getColumn(0);
        TestCase.assertTrue(pkColumn.isPrimaryKey());
        TestCase.assertFalse(pkColumn.isAutoincrement());

        // Autoincrement true
        pkColumn.setAutoincrement(true);
        AlterTable.alterColumn(geoPackage.getDatabase(), tableName, pkColumn);
        pkColumn = geoPackage.getUserCustomDao(tableName).getTable()
                .getColumn(0);
        TestCase.assertTrue(pkColumn.isPrimaryKey());
        TestCase.assertTrue(pkColumn.isAutoincrement());

        // Primary key false
        pkColumn.setPrimaryKey(false);
        AlterTable.alterColumn(geoPackage.getDatabase(), tableName, pkColumn);
        pkColumn = geoPackage.getUserCustomDao(tableName).getTable()
                .getColumn(0);
        TestCase.assertFalse(pkColumn.isPrimaryKey());
        TestCase.assertFalse(pkColumn.isAutoincrement());

        // Text unique constraint
        UserCustomColumn textColumn = geoPackage.getUserCustomDao(tableName)
                .getTable().getColumn(1);
        TestCase.assertEquals(GeoPackageDataType.TEXT,
                textColumn.getDataType());
        TestCase.assertTrue(textColumn.isUnique());

        // Unique text removed
        textColumn.setUnique(false);
        AlterTable.alterColumn(geoPackage.getDatabase(), tableName, textColumn);
        textColumn = geoPackage.getUserCustomDao(tableName).getTable()
                .getColumn(1);
        TestCase.assertFalse(textColumn.isUnique());

        // Unique text
        textColumn.setUnique(true);
        AlterTable.alterColumn(geoPackage.getDatabase(), tableName, textColumn);
        textColumn = geoPackage.getUserCustomDao(tableName).getTable()
                .getColumn(1);
        TestCase.assertTrue(textColumn.isUnique());

    }

}

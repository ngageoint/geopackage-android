package mil.nga.geopackage.test.attributes;

import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.attributes.AttributesColumn;
import mil.nga.geopackage.attributes.AttributesCursor;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesRow;
import mil.nga.geopackage.attributes.AttributesTable;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.db.DateConverter;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.metadata.Metadata;
import mil.nga.geopackage.metadata.MetadataScopeType;
import mil.nga.geopackage.metadata.reference.MetadataReference;
import mil.nga.geopackage.metadata.reference.MetadataReferenceDao;
import mil.nga.geopackage.metadata.reference.ReferenceScopeType;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.test.geom.GeoPackageGeometryDataUtils;
import mil.nga.geopackage.user.ColumnValue;
import mil.nga.geopackage.user.UserCoreResultUtils;

/**
 * Attributes Utility test methods
 * 
 * @author osbornb
 */
public class AttributesUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage) throws SQLException {

		List<String> tables = geoPackage.getAttributesTables();

		if (!tables.isEmpty()) {

			for (String tableName : tables) {

				// Test the get attributes DAO methods
				ContentsDao contentsDao = geoPackage.getContentsDao();
				Contents contents = contentsDao.queryForId(tableName);
				AttributesDao dao = geoPackage.getAttributesDao(contents);
				TestCase.assertNotNull(dao);
				dao = geoPackage.getAttributesDao(tableName);
				TestCase.assertNotNull(dao);

				TestCase.assertNotNull(dao.getDb());
				TestCase.assertEquals(tableName, dao.getTableName());

				AttributesTable attributesTable = dao.getTable();
				String[] columns = attributesTable.getColumnNames();

				// Query for all
				AttributesCursor cursor = dao.queryForAll();
				int count = cursor.getCount();
				int manualCount = 0;
				while (cursor.moveToNext()) {

					AttributesRow attributesRow = cursor.getRow();
					validateAttributesRow(columns, attributesRow);

					manualCount++;
				}
				TestCase.assertEquals(count, manualCount);
				cursor.close();

				// Manually query for all and compare
				cursor = (AttributesCursor) dao.getDatabaseConnection().query(dao.getTableName(),
						null, null, null, null, null, null);
				count = cursor.getCount();
				manualCount = 0;
				while (cursor.moveToNext()) {
					manualCount++;
				}
				TestCase.assertEquals(count, manualCount);

				TestCase.assertTrue("No attributes to test", count > 0);

				// Choose random attribute
				int random = (int) (Math.random() * count);
				cursor.moveToPosition(random);
				AttributesRow attributesRow = cursor.getRow();

				cursor.close();

				// Query by id
				AttributesRow queryAttributesRow = dao
						.queryForIdRow(attributesRow.getId());
				TestCase.assertNotNull(queryAttributesRow);
				TestCase.assertEquals(attributesRow.getId(),
						queryAttributesRow.getId());

				// Find two non id columns
				AttributesColumn column1 = null;
				AttributesColumn column2 = null;
				for (AttributesColumn column : attributesRow.getTable()
						.getColumns()) {
					if (!column.isPrimaryKey()) {
						if (column1 == null) {
							column1 = column;
						} else {
							column2 = column;
							break;
						}
					}
				}

				// Query for equal
				if (column1 != null) {

					Object column1Value = attributesRow.getValue(column1
							.getName());
					Class<?> column1ClassType = column1.getDataType()
							.getClassType();
					boolean column1Decimal = column1ClassType == Double.class
							|| column1ClassType == Float.class;
					ColumnValue column1AttributesValue;
					if (column1Decimal) {
						column1AttributesValue = new ColumnValue(column1Value,
								.000001);
					} else {
						column1AttributesValue = new ColumnValue(column1Value);
					}
					cursor = dao.queryForEq(column1.getName(),
							column1AttributesValue);
					TestCase.assertTrue(cursor.getCount() > 0);
					boolean found = false;
					while (cursor.moveToNext()) {
						queryAttributesRow = cursor.getRow();
						TestCase.assertEquals(column1Value,
								queryAttributesRow.getValue(column1.getName()));
						if (!found) {
							found = attributesRow.getId() == queryAttributesRow
									.getId();
						}
					}
					TestCase.assertTrue(found);
					cursor.close();

					// Query for field values
					Map<String, ColumnValue> fieldValues = new HashMap<String, ColumnValue>();
					fieldValues.put(column1.getName(), column1AttributesValue);
					Object column2Value = null;
					ColumnValue column2AttributesValue;
					if (column2 != null) {
						column2Value = attributesRow
								.getValue(column2.getName());
						Class<?> column2ClassType = column2.getDataType()
								.getClassType();
						boolean column2Decimal = column2ClassType == Double.class
								|| column2ClassType == Float.class;
						if (column2Decimal) {
							column2AttributesValue = new ColumnValue(
									column2Value, .000001);
						} else {
							column2AttributesValue = new ColumnValue(
									column2Value);
						}
						fieldValues.put(column2.getName(),
								column2AttributesValue);
					}
					cursor = dao.queryForValueFieldValues(fieldValues);
					TestCase.assertTrue(cursor.getCount() > 0);
					found = false;
					while (cursor.moveToNext()) {
						queryAttributesRow = cursor.getRow();
						TestCase.assertEquals(column1Value,
								queryAttributesRow.getValue(column1.getName()));
						if (column2 != null) {
							TestCase.assertEquals(column2Value,
									queryAttributesRow.getValue(column2
											.getName()));
						}
						if (!found) {
							found = attributesRow.getId() == queryAttributesRow
									.getId();
						}
					}
					TestCase.assertTrue(found);
					cursor.close();
				}

				MetadataReferenceDao referenceDao = geoPackage
						.getMetadataReferenceDao();
				List<MetadataReference> references = referenceDao.queryForEq(
						MetadataReference.COLUMN_TABLE_NAME,
						attributesTable.getTableName());
				if (references != null && !references.isEmpty()) {
					Metadata metadata = references.get(0).getMetadata();
					TestCase.assertEquals(MetadataScopeType.ATTRIBUTE_TYPE,
							metadata.getMetadataScope());
					for (MetadataReference reference : references) {
						TestCase.assertTrue(reference.getReferenceScope() == ReferenceScopeType.ROW
								|| reference.getReferenceScope() == ReferenceScopeType.ROW_COL);
						Long rowId = reference.getRowIdValue();
						TestCase.assertNotNull(rowId);

						AttributesRow queryRow = dao.queryForIdRow(rowId);
						TestCase.assertNotNull(queryRow);
						TestCase.assertNotNull(queryRow.getTable());
						TestCase.assertEquals(attributesTable.getTableName(),
								queryRow.getTable().getTableName());
					}
				}
			}
		}

	}

	/**
	 * Validate an attributes row
	 * 
	 * @param columns
	 * @param attributesRow
	 */
	private static void validateAttributesRow(String[] columns,
			AttributesRow attributesRow) {
		TestCase.assertEquals(columns.length, attributesRow.columnCount());

		for (int i = 0; i < attributesRow.columnCount(); i++) {
			AttributesColumn column = attributesRow.getTable().getColumns()
					.get(i);
            GeoPackageDataType dataType = column.getDataType();
			TestCase.assertEquals(i, column.getIndex());
			TestCase.assertEquals(columns[i], attributesRow.getColumnName(i));
			TestCase.assertEquals(i, attributesRow.getColumnIndex(columns[i]));
			int rowType = attributesRow.getRowColumnType(i);
			Object value = attributesRow.getValue(i);

			switch (rowType) {

			case UserCoreResultUtils.FIELD_TYPE_INTEGER:
				TestUtils.validateIntegerValue(value, column.getDataType());
				break;

			case UserCoreResultUtils.FIELD_TYPE_FLOAT:
				TestUtils.validateFloatValue(value, column.getDataType());
				break;

			case UserCoreResultUtils.FIELD_TYPE_STRING:
                if(dataType == GeoPackageDataType.DATE || dataType == GeoPackageDataType.DATETIME){
                    TestCase.assertTrue(value instanceof Date);
                    Date date = (Date) value;
                    DateConverter converter = DateConverter.converter(dataType);
                    String dateString = converter.stringValue(date);
                    TestCase.assertEquals(date.getTime(), converter.dateValue(dateString).getTime());
                }else{
                    TestCase.assertTrue(value instanceof String);
                }
				break;

			case UserCoreResultUtils.FIELD_TYPE_BLOB:
				TestCase.assertTrue(value instanceof byte[]);
				break;

			case UserCoreResultUtils.FIELD_TYPE_NULL:
				TestCase.assertNull(value);
				break;

			}
		}

		TestCase.assertTrue(attributesRow.getId() >= 0);
	}

	/**
	 * Test update
	 *
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testUpdate(GeoPackage geoPackage) throws SQLException {

		List<String> tables = geoPackage.getAttributesTables();

		if (!tables.isEmpty()) {

			for (String tableName : tables) {

				AttributesDao dao = geoPackage.getAttributesDao(tableName);
				TestCase.assertNotNull(dao);

				// Query for all
				AttributesCursor cursor = dao.queryForAll();
				int count = cursor.getCount();
				if (count > 0) {

					// // Choose random attribute
					// int random = (int) (Math.random() * count);
					// cursor.moveToPosition(random);
					cursor.moveToFirst();

					String updatedString = null;
					String updatedLimitedString = null;
                    Date updatedDate = null;
					Boolean updatedBoolean = null;
					Byte updatedByte = null;
					Short updatedShort = null;
					Integer updatedInteger = null;
					Long updatedLong = null;
					Float updatedFloat = null;
					Double updatedDouble = null;
					byte[] updatedBytes = null;
					byte[] updatedLimitedBytes = null;

					AttributesRow originalRow = cursor.getRow();
					AttributesRow attributesRow = cursor.getRow();

					try {
						attributesRow.setValue(
								attributesRow.getPkColumnIndex(), 9);
						TestCase.fail("Updated the primary key value");
					} catch (GeoPackageException e) {
						// expected
					}

					for (AttributesColumn attributesColumn : dao.getTable()
							.getColumns()) {
						if (!attributesColumn.isPrimaryKey()) {

                            GeoPackageDataType dataType = attributesColumn.getDataType();

							switch (attributesRow
									.getRowColumnType(attributesColumn
											.getIndex())) {

							case UserCoreResultUtils.FIELD_TYPE_STRING:
                                if(dataType == GeoPackageDataType.DATE || dataType == GeoPackageDataType.DATETIME){
                                    if (updatedDate == null) {
                                        updatedDate = new Date();
                                    }
                                    DateConverter converter = DateConverter.converter(dataType);
                                    if(Math.random() < .5){
                                        attributesRow.setValue(
                                                attributesColumn.getIndex(),
                                                updatedDate);
                                    }else{
                                        attributesRow.setValue(
                                                attributesColumn.getIndex(),
                                                converter.stringValue(updatedDate));
                                    }
                                }else {
                                    if (updatedString == null) {
                                        updatedString = UUID.randomUUID()
                                                .toString();
                                    }
                                    if (attributesColumn.getMax() != null) {
                                        if (updatedLimitedString == null) {
                                            if (updatedString.length() > attributesColumn
                                                    .getMax()) {
                                                updatedLimitedString = updatedString
                                                        .substring(0,
                                                                attributesColumn
                                                                        .getMax()
                                                                        .intValue());
                                            } else {
                                                updatedLimitedString = updatedString;
                                            }
                                        }
                                        attributesRow.setValue(
                                                attributesColumn.getIndex(),
                                                updatedLimitedString);
                                    } else {
                                        attributesRow.setValue(
                                                attributesColumn.getIndex(),
                                                updatedString);
                                    }
                                }
								break;
							case UserCoreResultUtils.FIELD_TYPE_INTEGER:
								switch (attributesColumn.getDataType()) {
								case BOOLEAN:
									if (updatedBoolean == null) {
										updatedBoolean = !((Boolean) attributesRow
												.getValue(attributesColumn
														.getIndex()));
									}
									attributesRow.setValue(
											attributesColumn.getIndex(),
											updatedBoolean);
									break;
								case TINYINT:
									if (updatedByte == null) {
										updatedByte = (byte) (((int) (Math
												.random() * (Byte.MAX_VALUE + 1))) * (Math
												.random() < .5 ? 1 : -1));
									}
									attributesRow.setValue(
											attributesColumn.getIndex(),
											updatedByte);
									break;
								case SMALLINT:
									if (updatedShort == null) {
										updatedShort = (short) (((int) (Math
												.random() * (Short.MAX_VALUE + 1))) * (Math
												.random() < .5 ? 1 : -1));
									}
									attributesRow.setValue(
											attributesColumn.getIndex(),
											updatedShort);
									break;
								case MEDIUMINT:
									if (updatedInteger == null) {
										updatedInteger = (int) (((int) (Math
												.random() * (Integer.MAX_VALUE + 1))) * (Math
												.random() < .5 ? 1 : -1));
									}
									attributesRow.setValue(
											attributesColumn.getIndex(),
											updatedInteger);
									break;
								case INT:
								case INTEGER:
									if (updatedLong == null) {
										updatedLong = (long) (((int) (Math
												.random() * (Long.MAX_VALUE + 1))) * (Math
												.random() < .5 ? 1 : -1));
									}
									attributesRow.setValue(
											attributesColumn.getIndex(),
											updatedLong);
									break;
								default:
									TestCase.fail("Unexpected integer type: "
											+ attributesColumn.getDataType());
								}
								break;
							case UserCoreResultUtils.FIELD_TYPE_FLOAT:
								switch (attributesColumn.getDataType()) {
								case FLOAT:
									if (updatedFloat == null) {
										updatedFloat = (float) Math.random()
												* Float.MAX_VALUE;
									}
									attributesRow.setValue(
											attributesColumn.getIndex(),
											updatedFloat);
									break;
								case DOUBLE:
								case REAL:
									if (updatedDouble == null) {
										updatedDouble = Math.random()
												* Double.MAX_VALUE;
									}
									attributesRow.setValue(
											attributesColumn.getIndex(),
											updatedDouble);
									break;
								default:
									TestCase.fail("Unexpected float type: "
											+ attributesColumn.getDataType());
								}
								break;
							case UserCoreResultUtils.FIELD_TYPE_BLOB:
								if (updatedBytes == null) {
									updatedBytes = UUID.randomUUID().toString()
											.getBytes();
								}
								if (attributesColumn.getMax() != null) {
									if (updatedLimitedBytes == null) {
										if (updatedBytes.length > attributesColumn
												.getMax()) {
											updatedLimitedBytes = new byte[attributesColumn
													.getMax().intValue()];
											ByteBuffer.wrap(
													updatedBytes,
													0,
													attributesColumn.getMax()
															.intValue()).get(
													updatedLimitedBytes);
										} else {
											updatedLimitedBytes = updatedBytes;
										}
									}
									attributesRow.setValue(
											attributesColumn.getIndex(),
											updatedLimitedBytes);
								} else {
									attributesRow.setValue(
											attributesColumn.getIndex(),
											updatedBytes);
								}
								break;
							default:
							}

						}
					}

					cursor.close();

					TestCase.assertEquals(1, dao.update(attributesRow));

					long id = attributesRow.getId();
					AttributesRow readRow = dao.queryForIdRow(id);
					TestCase.assertNotNull(readRow);
					TestCase.assertEquals(originalRow.getId(), readRow.getId());

					for (String readColumnName : readRow.getColumnNames()) {

						AttributesColumn readAttributesColumn = readRow
								.getColumn(readColumnName);
						if (!readAttributesColumn.isPrimaryKey()) {

                            GeoPackageDataType dataType = readAttributesColumn.getDataType();

							switch (readRow.getRowColumnType(readColumnName)) {
							case UserCoreResultUtils.FIELD_TYPE_STRING:
                                if(dataType == GeoPackageDataType.DATE || dataType == GeoPackageDataType.DATETIME){
                                    DateConverter converter = DateConverter.converter(dataType);
                                    Object value = readRow.getValue(readAttributesColumn
                                            .getIndex());
                                    Date date = null;
                                    if(value instanceof Date){
                                        date = (Date) value;
                                    }else{
                                        date = converter.dateValue((String)value);
                                    }
                                    Date compareDate = updatedDate;
                                    if(dataType == GeoPackageDataType.DATE){
                                        compareDate = converter.dateValue(converter.stringValue(compareDate));
                                    }
                                    TestCase.assertEquals(
                                            compareDate.getTime(),
                                            date.getTime());
                                }else {
                                    if (readAttributesColumn.getMax() != null) {
                                        TestCase.assertEquals(
                                                updatedLimitedString,
                                                readRow.getValue(readAttributesColumn
                                                        .getIndex()));
                                    } else {
                                        TestCase.assertEquals(
                                                updatedString,
                                                readRow.getValue(readAttributesColumn
                                                        .getIndex()));
                                    }
                                }
								break;
							case UserCoreResultUtils.FIELD_TYPE_INTEGER:
								switch (readAttributesColumn.getDataType()) {
								case BOOLEAN:
									TestCase.assertEquals(
											updatedBoolean,
											readRow.getValue(readAttributesColumn
													.getIndex()));
									break;
								case TINYINT:
									TestCase.assertEquals(updatedByte, readRow
											.getValue(readAttributesColumn
													.getIndex()));
									break;
								case SMALLINT:
									TestCase.assertEquals(updatedShort, readRow
											.getValue(readAttributesColumn
													.getIndex()));
									break;
								case MEDIUMINT:
									TestCase.assertEquals(
											updatedInteger,
											readRow.getValue(readAttributesColumn
													.getIndex()));
									break;
								case INT:
								case INTEGER:
									TestCase.assertEquals(updatedLong, readRow
											.getValue(readAttributesColumn
													.getIndex()));
									break;
								default:
									TestCase.fail("Unexpected integer type: "
											+ readAttributesColumn
													.getDataType());
								}
								break;
							case UserCoreResultUtils.FIELD_TYPE_FLOAT:
								switch (readAttributesColumn.getDataType()) {
								case FLOAT:
									TestCase.assertEquals(updatedFloat, readRow
											.getValue(readAttributesColumn
													.getIndex()));
									break;
								case DOUBLE:
								case REAL:
									TestCase.assertEquals(
											updatedDouble,
											readRow.getValue(readAttributesColumn
													.getIndex()));
									break;
								default:
									TestCase.fail("Unexpected float type: "
											+ readAttributesColumn
													.getDataType());
								}
								break;
							case UserCoreResultUtils.FIELD_TYPE_BLOB:
								if (readAttributesColumn.getMax() != null) {
									GeoPackageGeometryDataUtils
											.compareByteArrays(
													updatedLimitedBytes,
													(byte[]) readRow
															.getValue(readAttributesColumn
																	.getIndex()));
								} else {
									GeoPackageGeometryDataUtils
											.compareByteArrays(
													updatedBytes,
													(byte[]) readRow
															.getValue(readAttributesColumn
																	.getIndex()));
								}
								break;
							default:
							}
						}

					}

				}
				cursor.close();
			}
		}

	}

	/**
	 * Test create
	 *
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testCreate(GeoPackage geoPackage) throws SQLException {

		List<String> tables = geoPackage.getAttributesTables();

		if (!tables.isEmpty()) {

			for (String tableName : tables) {

				AttributesDao dao = geoPackage.getAttributesDao(tableName);
				TestCase.assertNotNull(dao);

				AttributesCursor cursor = dao.queryForAll();
				int count = cursor.getCount();
				if (count > 0) {

					// Choose random attribute
					int random = (int) (Math.random() * count);
					cursor.moveToPosition(random);

					AttributesRow attributesRow = cursor.getRow();
					cursor.close();

					// Create new row from existing
					long id = attributesRow.getId();
					attributesRow.resetId();
					long newRowId = dao.create(attributesRow);

					TestCase.assertEquals(newRowId, attributesRow.getId());

					// Verify original still exists and new was created
					attributesRow = dao.queryForIdRow(id);
					TestCase.assertNotNull(attributesRow);
					AttributesRow queryAttributesRow = dao
							.queryForIdRow(newRowId);
					TestCase.assertNotNull(queryAttributesRow);
					cursor = dao.queryForAll();
					TestCase.assertEquals(count + 1, cursor.getCount());
					cursor.close();

					// Create new row with copied values from another
					AttributesRow newRow = dao.newRow();
					for (AttributesColumn column : dao.getTable().getColumns()) {

						if (column.isPrimaryKey()) {
							try {
								newRow.setValue(column.getName(), 10);
								TestCase.fail("Set primary key on new row");
							} catch (GeoPackageException e) {
								// Expected
							}
						} else {
							newRow.setValue(column.getName(),
									attributesRow.getValue(column.getName()));
						}
					}

					long newRowId2 = dao.create(newRow);

					TestCase.assertEquals(newRowId2, newRow.getId());

					// Verify new was created
					AttributesRow queryAttributesRow2 = dao
							.queryForIdRow(newRowId2);
					TestCase.assertNotNull(queryAttributesRow2);
					cursor = dao.queryForAll();
					TestCase.assertEquals(count + 2, cursor.getCount());
					cursor.close();

					// Test copied row
					AttributesRow copyRow = new AttributesRow(
							queryAttributesRow2);
					for (AttributesColumn column : dao.getTable().getColumns()) {
						if (column.getDataType() == GeoPackageDataType.BLOB) {
							byte[] blob1 = (byte[]) queryAttributesRow2
									.getValue(column.getName());
							byte[] blob2 = (byte[]) copyRow.getValue(column
									.getName());
							if (blob1 == null) {
								TestCase.assertNull(blob2);
							} else {
								GeoPackageGeometryDataUtils.compareByteArrays(
										blob1, blob2);
							}
						} else {
							TestCase.assertEquals(queryAttributesRow2
									.getValue(column.getName()), copyRow
									.getValue(column.getName()));
						}
					}

					copyRow.resetId();

					long newRowId3 = dao.create(copyRow);

					TestCase.assertEquals(newRowId3, copyRow.getId());

					// Verify new was created
					AttributesRow queryAttributesRow3 = dao
							.queryForIdRow(newRowId3);
					TestCase.assertNotNull(queryAttributesRow3);
					cursor = dao.queryForAll();
					TestCase.assertEquals(count + 3, cursor.getCount());
					cursor.close();

					for (AttributesColumn column : dao.getTable().getColumns()) {
						if (column.isPrimaryKey()) {
							TestCase.assertNotSame(queryAttributesRow2
											.getValue(column.getName()),
									queryAttributesRow3.getValue(column
											.getName()));
						} else if (column.getDataType() == GeoPackageDataType.BLOB) {
							byte[] blob1 = (byte[]) queryAttributesRow2
									.getValue(column.getName());
							byte[] blob2 = (byte[]) queryAttributesRow3
									.getValue(column.getName());
							if (blob1 == null) {
								TestCase.assertNull(blob2);
							} else {
								GeoPackageGeometryDataUtils.compareByteArrays(
										blob1, blob2);
							}
						} else {
							TestCase.assertEquals(queryAttributesRow2
											.getValue(column.getName()),
									queryAttributesRow3.getValue(column
											.getName()));
						}
					}
				}
				cursor.close();
			}
		}

	}

	/**
	 * Test delete
	 *
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		List<String> tables = geoPackage.getAttributesTables();

		if (!tables.isEmpty()) {

			for (String tableName : tables) {

				AttributesDao dao = geoPackage.getAttributesDao(tableName);
				TestCase.assertNotNull(dao);

				AttributesCursor cursor = dao.queryForAll();
				int count = cursor.getCount();
				if (count > 0) {

					// Choose random attribute
					int random = (int) (Math.random() * count);
					cursor.moveToPosition(random);

					AttributesRow attributesRow = cursor.getRow();
					cursor.close();

					// Delete row
					TestCase.assertEquals(1, dao.delete(attributesRow));

					// Verify deleted
					AttributesRow queryAttributesRow = dao
							.queryForIdRow(attributesRow.getId());
					TestCase.assertNull(queryAttributesRow);
					cursor = dao.queryForAll();
					TestCase.assertEquals(count - 1, cursor.getCount());
					cursor.close();
				}
				cursor.close();
			}
		}
	}

}

#Change Log
All notable changes to this project will be documented in this file.
Adheres to [Semantic Versioning](http://semver.org/).

---

## 2.0.2 (TBD)

* TBD

## [2.0.1](https://github.com/ngageoint/geopackage-android/releases/tag/2.0.1) (02-14-2018)

* Coverage Data extension (previously Elevation Extension)
* RTree Index Extension minimal support (user functions not supported)
* Tile Generator contents bounding box fix to use the requested bounds
* Handle table names containing spaces within the cursor factory
* Open GeoPackage with writable flag option
* Turn off Android auto backup
* GeoPackage creation example
* geopackage-core version updated to 2.0.1

## [2.0.0](https://github.com/ngageoint/geopackage-android/releases/tag/2.0.0) (11-20-2017)

* WARNING - BoundingBox.java (geopackage-core) coordinate constructor arguments order changed to (min lon, min lat, max lon, max lat)
  Pre-existing calls to BoundingBox coordinate constructor should swap the min lat and max lon values
* WARNING - TileGrid.java (geopackage-core) constructor arguments order changed to (minX, minY, maxX, maxY)
  Pre-existing calls to TileGrid constructor should swap the minY and maxX values
* geopackage-core version updated to 2.0.0
* User Invalid Cursor support for reading large blobs
* Attribute, Feature, and Tile Invalid Cursor implementations
* Attribute, Feature, and Tile User Row Sync implementations
* Query support for "columns as"
* Feature Indexer and Feature Table Index row syncing
* Improved feature row geometry blob handling
* Feature Tiles geometry simplifications
* Multiple Results and List Results implementations of Feature Index results
* Feature Index Manager index type specific improvements
* User Query object representation and support
* tiff version updated to 2.0.0
* gradle plugin updated to 2.3.3
* android maven gradle plugin updated to 2.0
* maven google dependency
* compile SDK version 26
* build tools version updated to 26.0.1
* min SDK version updated to 14
* target SDK version updated to 26
* Android support library updated to 26.0.2

## [1.4.1](https://github.com/ngageoint/geopackage-android/releases/tag/1.4.1) (07-13-2017)

* geopackage-core version updated to 1.3.1
* Improved handling of unknown Contents bounding boxes
* Feature Tile max feature number drawn tiles default padding and text size
* Minor color deprecation and Javadoc updates

## [1.4.0](https://github.com/ngageoint/geopackage-android/releases/tag/1.4.0) (06-27-2017)

* geopackage-core version updated to 1.3.0
* tiff version updated to 1.0.3
* Copy constructors for user table (features, tiles, attributes) row objects
* Improved date column support for user tables (features, tiles, attributes)

## [1.3.2](https://github.com/ngageoint/geopackage-android/releases/tag/1.3.2) (06-12-2017)

* geopackage-core version updated to 1.2.2
* tiff version updated to 1.0.2
* Elevation Extension scale and offset columns changed to be non nullable
* Android support library updated to 25.2.0
* min SDK lowered from 14 to 13
* build tools version updated to 25.0.3
* gradle plugin updated to 2.3.2
* Android Manifest cleanup
* URL Tile Generator and GeoPackage download handle URL redirects

## [1.3.1](https://github.com/ngageoint/geopackage-android/releases/tag/1.3.1) (02-02-2017)

* Elevation Extension support (PNG & TIFF)
* geopackage-core version updated to 1.2.1
* Moved Play Services and Android Map Utility dependencies to [geopackage-android-map](http://github.com/ngageoint/geopackage-android-map)
* MapFeatureTiles replaced by DefaultFeatureTiles, removes Map library dependency & fixes geometries drawn over the International Date Line
* User Attributes table support
* tiff-java dependency for TIFF support
* Elevation query algorithms including Nearest Neighbor, Bilinear, and Bicubic
* Elevation unbounded results elevation queries
* Table and column name SQL quotations to allow uncommon but valid names
* Zoom level determination using width and height
* GeoPackage application id and user version
* Updated Android, Gradle, & Maven build & SDK libraries
* OrmLite Android version updated to 5.0

## [1.3.0](https://github.com/ngageoint/geopackage-android/releases/tag/1.3.0) (06-23-2016)

* geopackage-core version updated to 1.2.0
* Improved tile drawing on bounds for tiles not lining up with requests
* Tile Creator providing common tile generation functionality
* Tile reprojections between different unit types (ex. WGS84 GeoPackage tiles)
* Tile DAO changed to work with any projection units
* Tile Generator support for multiple projections, such as WGS84 in addition to Web Mercator
* URL Tile Generator changed to use provided projection in place of parsing URL

## [1.2.9](https://github.com/ngageoint/geopackage-android/releases/tag/1.2.9) (05-10-2016)

* GeoPackage 1.1.0 spec updates
* geopackage-core version updated to 1.1.8
* GeoPackage Connection column exists and query single result method implementations
* Use updated projection calls by passing Spatial Reference Systems
* Feature Overlay Query use of Data Column names in place of the column name when available

## [1.2.8](https://github.com/ngageoint/geopackage-android/releases/tag/1.2.8) (04-19-2016)

* GeoPackage Manager import GeoPackage as override fix to prevent multiple same named metadata entries
* GeoPackage metadata unique constraint added to name column
* Gradle and Android library updates

## [1.2.7](https://github.com/ngageoint/geopackage-android/releases/tag/1.2.7) (04-18-2016)

* geopackage-core version updated to 1.1.7
* GeoPackage Manager name like queries
* GeoPackage Manager import as external link with override option methods
* GeoPackage Manager automatically delete database records where the file no longer exists
* Get longitude distance bug fix in Tile Bounding Box Map Utilities
* Feature Overlay Query and Feature Tiles close methods to close wrapped Index Manager connections
* Feature Overlay Query map click message improvements and map click Feature Table Data implementations
* Map click Feature Table Data JSON compatible conversions

## [1.2.6](https://github.com/ngageoint/geopackage-android/releases/tag/1.2.6) (02-19-2016)

* geopackage-core version updated to 1.1.6
* Feature Tile Table Linker implementation with methods for retrieving data access objects
* Bounded Overlay check if a specified tile exists
* Feature Overlay ignore drawing tiles that already exist in a linked tile table
* Feature Overlay Query improved determination if a tile exists before querying features
* Manager create GeoPackage as an external file methods

## [1.2.5](https://github.com/ngageoint/geopackage-android/releases/tag/1.2.5) (02-02-2016)

* geopackage-core version updated to 1.1.5
* Feature Tile Generator linking between feature and tile tables
* Feature Overlay Query updates to support linked feature and tile tables

## [1.2.4](https://github.com/ngageoint/geopackage-android/releases/tag/1.2.4) (01-25-2016)

* Updated Android compile and target SDK version to 23 (Marshmallow)
* Fixed GeoPackage Manager count to also include external GeoPackages
* Added methods for retrieving and counting only internal or external GeoPackages

## [1.2.3](https://github.com/ngageoint/geopackage-android/releases/tag/1.2.3) (01-15-2016)

* geopackage-core version updated to 1.1.4 for proj4j dependency location change
* Removed intermediate projection conversions in the map shape converter. Fixes EPSG 27700 (British National Grid) conversions.

## [1.2.2](https://github.com/ngageoint/geopackage-android/releases/tag/1.2.2) (12-16-2015)

* geopackage-core version updated to 1.1.3 for Geometry projection transformations

## [1.2.1](https://github.com/ngageoint/geopackage-android/releases/tag/1.2.1) (12-14-2015)

* geopackage-core version updated to 1.1.2 - [Core Issue #14](https://github.com/ngageoint/geopackage-core-java/issues/14)
* GeoPackage methods: execute SQL, query, foreign key check, integrity check, quick integrity check
* URL Tile Generator TMS support
* ORMLite log level changed from debug to info
* Tile Generator fix for updating a GeoPackage and replacing an existing tile

## [1.2.0](https://github.com/ngageoint/geopackage-android/releases/tag/1.2.0) (11-24-2015)

* Separation of Google Play Services Google Maps logic from GeoPackage functionality
* Tile retrieval logic separated from overlays
* Feature Tiles Map specific tile drawing separated into child implementation
* Separation of Map specific Tile Bounding Box utilities

## [1.1.1](https://github.com/ngageoint/geopackage-android/releases/tag/1.1.1) (11-20-2015)

* Javadoc project name and external API links
* Project Feature DAO bounding box when not in the same projection
* Feature Overlay Query message builder, check if features are indexed first
* geopackage-core version updated to 1.1.1 - [Issue #13](https://github.com/ngageoint/geopackage-android/issues/13)
* min and max column query methods - [Issue #15](https://github.com/ngageoint/geopackage-android/issues/15)
* TileDao methods, query for tile grid or bounding box at zoom level - [Issue #14](https://github.com/ngageoint/geopackage-android/issues/14)
* Database header and integrity validation options and methods. Validate imported GeoPackage headers by default. - [Issue #16](https://github.com/ngageoint/geopackage-android/issues/16)
* Manager methods - delete all externally linked databases with missing files, retrieve database name by externally linked path

## [1.1.0](https://github.com/ngageoint/geopackage-android/releases/tag/1.1.0) (10-08-2015)

* NGA Table Index Extension implementation - http://ngageoint.github.io/GeoPackage/docs/extensions/geometry-index.html
* Feature Index Manager to combine existing metadata indexing with the NGA Table Index Extension
* Feature Tile improvements, including max features per tile settings and custom max feature tile drawing
* Feature and Tile DAO get bounding box method
* Feature Overlay Query for querying the features behind the drawn feature tiles

## [1.0.1](https://github.com/ngageoint/geopackage-android/releases/tag/1.0.1) (09-23-2015)

* Upgrading geopackage-core version to 1.0.1 to get added GeoPackageCache functionality

## [1.0.0](https://github.com/ngageoint/geopackage-android/releases/tag/1.0.0) (09-15-2015)

* Initial Release

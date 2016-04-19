#Change Log
All notable changes to this project will be documented in this file.
Adheres to [Semantic Versioning](http://semver.org/).

---

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

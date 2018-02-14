# GeoPackage Android

### GeoPackage Android Lib ####

The [GeoPackage Libraries](http://ngageoint.github.io/GeoPackage/) were developed at the [National Geospatial-Intelligence Agency (NGA)](http://www.nga.mil/) in collaboration with [BIT Systems](http://www.bit-sys.com/). The government has "unlimited rights" and is releasing this software to increase the impact of government investments by providing developers with the opportunity to take things in new directions. The software use, modification, and distribution rights are stipulated within the [MIT license](http://choosealicense.com/licenses/mit/).

### Pull Requests ###
If you'd like to contribute to this project, please make a pull request. We'll review the pull request and discuss the changes. All pull request contributions to this project will be released under the MIT license.

Software source code previously released under an open source license and then modified by NGA staff is considered a "joint work" (see 17 USC § 101); it is partially copyrighted, partially public domain, and as a whole is protected by the copyrights of the non-government authors and must be released according to the terms of the original open source license.

### About ###

[GeoPackage Android](http://ngageoint.github.io/geopackage-android/) is a [GeoPackage Library](http://ngageoint.github.io/GeoPackage/) SDK implementation of the Open Geospatial Consortium [GeoPackage](http://www.geopackage.org/) [spec](http://www.geopackage.org/spec/).  It is listed as an [OGC GeoPackage Implementation](http://www.geopackage.org/#implementations_nga) by the National Geospatial-Intelligence Agency.

The GeoPackage SDK provides the ability to manage GeoPackage files providing read, write, import, export, share, and open support. Open GeoPackage files provide read and write access to features and tiles. Feature support includes Well-Known Binary translations. Tile generation supports creation by URL or features.

### Usage ###

View the latest [Javadoc](http://ngageoint.github.io/geopackage-android/docs/api/)

#### Implementations ####

##### GeoPackage Android Map #####

The [GeoPackage Android Map](https://github.com/ngageoint/geopackage-android-map) SDK adds Android Map implementations to this base library.

##### GeoPackage MapCache #####

The [GeoPackage MapCache](https://github.com/ngageoint/geopackage-mapcache-android) app provides an extensive standalone example on how to use both this and the [GeoPackage Android Map](https://github.com/ngageoint/geopackage-android-map) SDKs.

##### MAGE #####

The [Mobile Awareness GEOINT Environment (MAGE)](https://github.com/ngageoint/mage-android) app provides mobile situational awareness capabilities. It [uses the SDK](https://github.com/ngageoint/mage-android/search?q=GeoPackage&type=Code) to provide GeoPackage functionality.

##### DICE #####

The [Disconnected Interactive Content Explorer (DICE)](https://github.com/ngageoint/disconnected-content-explorer-android) app allows users to load and display interactive content without a network connection. It [uses the SDK](https://github.com/ngageoint/disconnected-content-explorer-android/search?q=GeoPackage&type=Code) to provide GeoPackage functionality on the map and within reports.

#### Example ####

```java

// Context context = ...;
// File geoPackageFile = ...;

// Get a manager
GeoPackageManager manager = GeoPackageFactory.getManager(context);

// Available databases
List<String> databases = manager.databases();

// Import database
boolean imported = manager.importGeoPackage(geoPackageFile);

// Open database
GeoPackage geoPackage = manager.open(databases.get(0));

// GeoPackage Table DAOs
SpatialReferenceSystemDao srsDao = geoPackage.getSpatialReferenceSystemDao();
ContentsDao contentsDao = geoPackage.getContentsDao();
GeometryColumnsDao geomColumnsDao = geoPackage.getGeometryColumnsDao();
TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();
DataColumnConstraintsDao dataColumnConstraintsDao = geoPackage.getDataColumnConstraintsDao();
MetadataDao metadataDao = geoPackage.getMetadataDao();
MetadataReferenceDao metadataReferenceDao = geoPackage.getMetadataReferenceDao();
ExtensionsDao extensionsDao = geoPackage.getExtensionsDao();

// Feature and tile tables
List<String> features = geoPackage.getFeatureTables();
List<String> tiles = geoPackage.getTileTables();

// Query Features
String featureTable = features.get(0);
FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
FeatureCursor featureCursor = featureDao.queryForAll();
try{
    while(featureCursor.moveToNext()){
        FeatureRow featureRow = featureCursor.getRow();
        GeoPackageGeometryData geometryData = featureRow.getGeometry();
        Geometry geometry = geometryData.getGeometry();
        // ...
    }
}finally{
    featureCursor.close();
}

// Query Tiles
String tileTable = tiles.get(0);
TileDao tileDao = geoPackage.getTileDao(tileTable);
TileCursor tileCursor = tileDao.queryForAll();
try{
    while(tileCursor.moveToNext()){
        TileRow tileRow = tileCursor.getRow();
        byte[] tileBytes = tileRow.getTileData();
        Bitmap tileBitmap = tileRow.getTileDataBitmap();
        // ...
    }
}finally{
    tileCursor.close();
}

// Index Features
FeatureIndexManager indexer = new FeatureIndexManager(context, geoPackage, featureDao);
indexer.setIndexLocation(FeatureIndexType.GEOPACKAGE);
int indexedCount = indexer.index();

// Draw tiles from features
FeatureTiles featureTiles = new DefaultFeatureTiles(context, featureDao);
featureTiles.setMaxFeaturesPerTile(1000); // Set max features to draw per tile
NumberFeaturesTile numberFeaturesTile = new NumberFeaturesTile(context); // Custom feature tile implementation
featureTiles.setMaxFeaturesTileDraw(numberFeaturesTile); // Draw feature count tiles when max features passed
featureTiles.setIndexManager(indexer); // Set index manager to query feature indices
Bitmap tile = featureTiles.drawTile(2, 2, 2);

BoundingBox boundingBox = new BoundingBox();
Projection projection = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

// URL Tile Generator (generate tiles from a URL)
TileGenerator urlTileGenerator = new UrlTileGenerator(context, geoPackage,
                "url_tile_table", "http://url/{z}/{x}/{y}.png", 2, 7, boundingBox, projection);
int urlTileCount = urlTileGenerator.generateTiles();

// Feature Tile Generator (generate tiles from features)
TileGenerator featureTileGenerator = new FeatureTileGenerator(context, geoPackage,
                featureTable + "_tiles", featureTiles, 10, 15, boundingBox, projection);
int featureTileCount = featureTileGenerator.generateTiles();

// Close database when done
geoPackage.close();

```

### Installation ###

Pull from the [Maven Central Repository](http://search.maven.org/#artifactdetails|mil.nga.geopackage|geopackage-android|2.0.1|aar) (AAR, POM, Source, Javadoc)

    compile "mil.nga.geopackage:geopackage-android:2.0.1"

### Build ###

Build this repository using Android Studio and/or Gradle.

#### Project Setup ####

Include as repositories in your project build.gradle:

    repositories {
        jcenter()
        mavenLocal()
    }

##### Normal Build #####

Include the dependency in your module build.gradle with desired version number:

    compile "mil.nga.geopackage:geopackage-android:2.0.1"

As part of the build process, run the "uploadArchives" task on the geopackage-android Gradle script to update the Maven local repository.

##### Local Build #####

Replace the normal build dependency in your module build.gradle with:

    compile project(':geopackage-sdk')

Include in your settings.gradle:

    include ':geopackage-sdk'

From your project directory, link the cloned SDK directory:

    ln -s ../geopackage-android/geopackage-sdk geopackage-sdk

### Remote Dependencies ###

* [GeoPackage Core Java](https://github.com/ngageoint/geopackage-core-java) (The MIT License (MIT)) - GeoPackage Library
* [TIFF](https://github.com/ngageoint/tiff-java) (The MIT License (MIT)) - Tagged Image File Format Lib
* [OrmLite](http://ormlite.com/) (Open Source License) - Object Relational Mapping (ORM) Library
* [PNGJ](http://github.com/leonbloy/pngj) (Apache License, Version 2.0) - PNGJ: Java library for PNG encoding

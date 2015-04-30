# GeoPackage Android

GeoPackage Android is a SDK implementation of the Open Geospatial Consortium [GeoPackage](http://www.geopackage.org/) [spec](http://www.geopackage.org/spec/).

The GeoPackage SDK provides the ability to manage GeoPackage files providing read, write, import, export, share, and open support. Open GeoPackage files provide read and write access to features and tiles. Feature support includes Well-Known Binary and Google Map shape translations. Tile generation supports creation by URL or features. Tile providers supporting GeoPackage format, Google tile API, and feature tile generation.

The GeoPackage SDK was developed at the National Geospatial-Intelligence Agency (NGA) in collaboration with [BIT Systems](https://www.bit-sys.com/index.jsp). The government has "unlimited rights" and is releasing this software to increase the impact of government investments by providing developers with the opportunity to take things in new directions. The software use, modification, and distribution rights are stipulated within the [MIT license](http://choosealicense.com/licenses/mit/).

### Pull Requests ###
If you'd like to contribute to this project, please make a pull request. We'll review the pull request and discuss the changes. All pull request contributions to this project will be released under the MIT license.

Software source code previously released under an open source license and then modified by NGA staff is considered a "joint work" (see 17 USC § 101); it is partially copyrighted, partially public domain, and as a whole is protected by the copyrights of the non-government authors and must be released according to the terms of the original open source license.

### Usage ###

#### GeoPackage SDK Sample ####

The [GeoPackage SDK Sample](https://git.geointapps.org/geopackage/geopackage-sample-android) app provides an extensive standalone example on how to use the SDK.

#### Example ####

    // Context context = ...;
    // File geoPackageFile = ...;
    // GoogleMap map = ...;
    
    // Get a manager
    GeoPackageManager manager = GeoPackageFactory.getManager(context);
    
    // Available databases
    List<String> databases = manager.databases();
    
    // Import database
    boolean imported = manager.importGeoPackage(geoPackageFile);
    
    // Open database
    GeoPackage geoPackage = manager.open(databases.get(0));
    
    // GeoPackage Table DAOs
    SpatialReferenceSystemDao srsDao = getSpatialReferenceSystemDao();
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
    GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
            featureDao.getProjection());
    FeatureCursor featureCursor = featureDao.queryForAll();
    try{
        while(featureCursor.moveToNext()){
            FeatureRow featureRow = featureCursor.getRow();
            GeoPackageGeometryData geometryData = featureRow.getGeometry();
            Geometry geometry = geometryData.getGeometry();
            GoogleMapShape shape = converter.toShape(geometry);
            GoogleMapShape mapShape = GoogleMapShapeConverter
                    .addShapeToMap(map, shape);
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
    
    // Tile Provider (GeoPackage or Google API)
    TileProvider overlay = GeoPackageOverlayFactory
            .getTileProvider(tileDao);
    TileOverlayOptions overlayOptions = new TileOverlayOptions();
    overlayOptions.tileProvider(overlay);
    overlayOptions.zIndex(-1);
    map.addTileOverlay(overlayOptions);
    
    // Feature Tile Provider
    FeatureTiles featureTiles = new FeatureTiles(context, featureDao);
    TileProvider featureOverlay = new FeatureOverlay(featureTiles);
    TileOverlayOptions featureOverlayOptions = new TileOverlayOptions();
    featureOverlayOptions.tileProvider(featureOverlay);
    featureOverlayOptions.zIndex(-1);
    map.addTileOverlay(featureOverlayOptions);
    
    // URL Tile Generator
    TileGenerator urlTileGenerator = new UrlTileGenerator(context, geoPackage,
                    "url_tile_table", "http://url/{z}/{x}/{y}.png", 2, 7);
    int urlTileCount = urlTileGenerator.generateTiles();
    
    // Feature Tile Generator
    TileGenerator featureTileGenerator = new FeatureTileGenerator(context, geoPackage,
                    featureTable + "_tiles", featureTiles, 10, 15);
    int featureTileCount = featureTileGenerator.generateTiles();
    
    // Close database when done
    geoPackage.close();

### License ###

    The MIT License (MIT)

    Copyright (c) 2015 BIT Systems

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.

### Remote Dependencies ###

* [GeoPackage Core](https://git.geointapps.org/geopackage/geopackage-core) (The MIT License (MIT)) - GeoPackage Library
* [OrmLite](http://ormlite.com/) (Open Source License) - Object Relational Mapping (ORM) Library

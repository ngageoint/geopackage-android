package mil.nga.geopackage.test.io;

import mil.nga.geopackage.io.GeoPackageProgress;

public class TestGeoPackageProgress implements GeoPackageProgress {

    private Integer max = null;
    private int progress = 0;
    private boolean active = true;

    @Override
    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public void addProgress(int progress) {
        this.progress += progress;
    }

    @Override
    public boolean isActive() {
        return active && (max == null || progress < max);
    }

    @Override
    public boolean cleanupOnCancel() {
        return false;
    }

    public void cancel() {
        active = false;
    }

    public Integer getMax() {
        return max;
    }

    public int getProgress() {
        return progress;
    }

}

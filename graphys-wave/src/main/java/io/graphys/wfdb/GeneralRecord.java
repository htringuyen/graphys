package io.graphys.wfdb;

import lombok.Builder;
import wfdb.*;
import java.util.List;
import java.util.stream.IntStream;

public class GeneralRecord implements Record {
    private final DatabaseInfo dbInfo;

    private final String name;

    private final RecordPath recordPath;

    private List<SignalInfo> signalInfos;

    GeneralRecord(DatabaseInfo dbInfo, String name, RecordPath path) {
        this.dbInfo = dbInfo;
        this.name = name;
        this.recordPath = path;

        loadSignalInfos();
    }

    @Override
    public DatabaseInfo getDbInfo() {
        return dbInfo;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return dbInfo.name() + "-" + dbInfo.version() + "-" + name;
    }

    @Override
    public int getNumOfSignals() {
        return getSignalInfos().size();
    }

    @Override
    public RecordPath getPath() {
        return recordPath;
    }

    @Override
    public List<SignalInfo> getSignalInfos() {
        if (signalInfos == null
                || signalInfos.isEmpty()) {
            loadSignalInfos();
        }
        return signalInfos;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Record other) {
            return this.name.equals(other.getName())
                    && this.dbInfo.equals(other.getDbInfo());
        }
        return false;
    }

    // call jni method to query signal info array then copy to signal objects
    private void loadSignalInfos() {
        wfdb.wfdbquit();

        var nSig = wfdb.isigopen(recordPath.getFullPath(), null, 0);
        var siArray = new WFDB_SiginfoArray(nSig);
        wfdb.isigopen(recordPath.getFullPath(), siArray.cast(), -nSig);

        signalInfos = IntStream.range(0, nSig)
                .mapToObj(siArray::getitem)
                .map(SignalInfo::from)
                .toList();
    }
}



















package io.graphys.wfdb;

import java.util.List;

public interface Record {
    public DatabaseInfo getDbInfo();

    public String getName();

    public String getId();

    public RecordPath getPath();

    public List<SignalInfo> getSignalInfos();

    public int getNumOfSignals();
}

package com.gmail.tracebachi.DeltaSkins.Shared.Interfaces;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 9/16/16.
 */
public interface IDeltaSkinsLogger
{
    void info(String message);

    void severe(String message);

    void debug(String message);

    void debugApi(String message);
}

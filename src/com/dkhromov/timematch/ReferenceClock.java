package com.dkhromov.timematch;

import java.util.Calendar;

public interface ReferenceClock {
	Calendar getCurrentUTCTime();
	int getRangeStart();
	int getRangeLength();
}

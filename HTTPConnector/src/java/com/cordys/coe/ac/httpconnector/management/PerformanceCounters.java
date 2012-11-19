package com.cordys.coe.ac.httpconnector.management;

import com.cordys.coe.ac.httpconnector.Messages;
import com.eibus.management.IManagedComponent;
import com.eibus.management.counters.CounterFactory;
import com.eibus.management.counters.ITimerEventValueCounter;

public class PerformanceCounters {

	private IManagedComponent processingInfo;
	private ITimerEventValueCounter requestTransformation;
	private ITimerEventValueCounter httpProcessing;
	private ITimerEventValueCounter responseTransformation;

	public PerformanceCounters(IManagedComponent managedComponent) {
		processingInfo = managedComponent.createSubComponent("Processing",
				"Request Processing", Messages.REQUEST_PROCESSING, this);
		requestTransformation = (ITimerEventValueCounter) processingInfo
				.createPerformanceCounter("RequestTransformation",
						Messages.REQUEST_TRANSFORMATION_TIME,
						CounterFactory.TIMER_EVENT_VALUE_COUNTER);
		httpProcessing = (ITimerEventValueCounter) processingInfo
				.createPerformanceCounter("HTTPRequestProcessing",
						Messages.HTTP_SEND_AND_RECEIVE,
						CounterFactory.TIMER_EVENT_VALUE_COUNTER);

		responseTransformation = (ITimerEventValueCounter) processingInfo
				.createPerformanceCounter("ResponseTransformation",
						Messages.RESPONSE_TRANSFORMATION_TIME,
						CounterFactory.TIMER_EVENT_VALUE_COUNTER);
	}

	public long getStartTime() {
		return requestTransformation.start();
	}

	public void finishRequestTransformation(long startTime) {
		requestTransformation.finish(startTime);
	}

	public void finishHTTP(long startTime) {
		httpProcessing.finish(startTime);
	}

	public void finishResponseTransformation(long startTime) {
		responseTransformation.finish(startTime);
	}
}

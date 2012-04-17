/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright � 2010 France Telecom S.A.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.orangelabs.rcs.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Periodic refresher
 *
 * @author JM. Auffret
 */
public abstract class PeriodicRefresher {
	/**
     * Keep alive manager
     */
    private KeepAlive alarmReceiver = new KeepAlive(); 

    /**
     * Alarm intent
     */
    private PendingIntent alarmIntent;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     */
    public PeriodicRefresher() {
    	// Create a unique pending intent
    	String action = this.toString(); // Unique action ID 
    	this.alarmIntent = PendingIntent.getBroadcast(
    			AndroidFactory.getApplicationContext(),
    			0,
    			new Intent(action),
    			0);
 
        // Register the alarm receiver
    	AndroidFactory.getApplicationContext().registerReceiver(alarmReceiver, new IntentFilter(action));		
    }
    
    /**
     * Periodic processing
     */
    public abstract void periodicProcessing();
    
    /**
     * Start the timer
     * 
     * @param expirePeriod Expiration period in seconds
     * @param delta Delta to apply on the expire period in percentage
     */
    public void startTimer(int expirePeriod, double delta) {
		// Calculate the effective refresh period
    	int period = (int)(expirePeriod * delta);
    	if (logger.isActivated()) {
    		logger.debug("Start timer at period=" + period +  "s (expiration=" + expirePeriod + "s)");
    	}

    	// Start alarm from now to the expire value
        AlarmManager am = (AlarmManager)AndroidFactory.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+period*1000, alarmIntent);
    }

    /**
     * Stop the timer
     */
    public void stopTimer() {
    	if (logger.isActivated()) {
    		logger.debug("Stop timer");
    	}

    	// Cancel alarm
        AlarmManager am = (AlarmManager)AndroidFactory.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.cancel(alarmIntent);
    }

    /**
     * Keep alive manager
     */
    private class KeepAlive extends BroadcastReceiver {
    	public void onReceive(Context context, Intent intent) {
    		periodicProcessing();
    	}
    }    
}
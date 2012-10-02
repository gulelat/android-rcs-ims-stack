/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0.0
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
package com.orangelabs.rcs.service.api.server.toip;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.orangelabs.rcs.core.ims.service.toip.ToIpError;
import com.orangelabs.rcs.core.ims.service.toip.ToIpSessionListener;
import com.orangelabs.rcs.service.api.client.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.IMediaRenderer;
import com.orangelabs.rcs.service.api.client.toip.IToIpEventListener;
import com.orangelabs.rcs.service.api.client.toip.IToIpSession;
import com.orangelabs.rcs.service.api.server.RemoteMediaPlayer;
import com.orangelabs.rcs.service.api.server.RemoteMediaRenderer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * ToIP session
 * 
 * @author jexa7410
 */
public class ToIpSession extends IToIpSession.Stub implements ToIpSessionListener {
	
	/**
	 * Core session
	 */
	private com.orangelabs.rcs.core.ims.service.toip.ToIpSession session;
	
	/**
	 * List of listeners
	 */
	private RemoteCallbackList<IToIpEventListener> listeners = new RemoteCallbackList<IToIpEventListener>();

    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param session Session
	 */
	public ToIpSession(com.orangelabs.rcs.core.ims.service.toip.ToIpSession session) {
		this.session = session;
		
		session.addListener(this);
	}
	
	/**
	 * Get session ID
	 * 
	 * @return Session ID
	 */
	public String getSessionID() {
		return session.getSessionID();
	}
	
	/**
	 * Get remote contact
	 * 
	 * @return Contact
	 */
	public String getRemoteContact() {
		return session.getRemoteContact();
	}
	
	/**
	 * Accept the session invitation
	 */
	public void acceptSession() {
		if (logger.isActivated()) {
			logger.info("Accept session invitation");
		}

		// Accept invitation
		session.acceptSession();
	}
	
	/**
	 * Reject the session invitation
	 */
	public void rejectSession() {
		if (logger.isActivated()) {
			logger.info("Reject session invitation");
		}
		
		// Reject invitation
		session.rejectSession();
	}

	/**
	 * Cancel the session
	 */
	public void cancelSession() {
		if (logger.isActivated()) {
			logger.info("Abort session");
		}

		// Abort the session
		session.abortSession();
	}

	/**
	 * Set the media player
	 * 
	 * @param player Media player
	 */
	public void setMediaPlayer(IMediaPlayer player) {
		if (logger.isActivated()) {
			logger.info("Set a media player");
		}
		
		session.setMediaPlayer(new RemoteMediaPlayer(player));
	}

	/**
	 * Set the media renderer
	 * 
	 * @param renderer Media renderer
	 */
	public void setMediaRenderer(IMediaRenderer renderer) {
		if (logger.isActivated()) {
			logger.info("Set a media renderer");
		}
		
		session.setMediaRenderer(new RemoteMediaRenderer(renderer));
	}
	
	/**
	 * Add session listener
	 * 
	 * @param listener Listener
	 */
	public void addSessionListener(IToIpEventListener listener) {
		if (logger.isActivated()) {
			logger.info("Add an event listener");
		}

		listeners.register(listener);
	}

	/**
	 * Remove session listener
	 * 
	 * @param listener Listener
	 */
	public void removeSessionListener(IToIpEventListener listener) {
		if (logger.isActivated()) {
			logger.info("Remove an event listener");
		}

		listeners.unregister(listener);
	}

	/**
	 * Session is started
	 */
    public void handleSessionStarted() {
		if (logger.isActivated()) {
			logger.info("Session started");
		}

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionStarted();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }

    /**
     * Session has been aborted
     */
    public void handleSessionAborted() {
		if (logger.isActivated()) {
			logger.info("Session aborted");
		}

		// Remove the notification
		// TODO

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionAborted();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }
    
    /**
     * Session has been terminated
     */
    public void handleSessionTerminated() {
		if (logger.isActivated()) {
			logger.info("Session terminated");
		}

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionTerminated();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }
    
    /**
     * Session has been terminated by remote
     */
    public void handleSessionTerminatedByRemote() {
		if (logger.isActivated()) {
			logger.info("Session terminated by remote");
		}

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionTerminatedByRemote();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }
	
    /**
     * ToIP error
     * 
     * @param error Error
     */
    public void handleToIpError(ToIpError error) {
		if (logger.isActivated()) {
			logger.info("Session error");
		}

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleToIpError(error.getErrorCode());
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }
}
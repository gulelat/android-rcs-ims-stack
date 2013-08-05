/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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

package com.orangelabs.rcs.core.ims.service.im.filetransfer.http;

import javax2.sip.header.ContactHeader;

import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.im.InstantMessagingService;
import com.orangelabs.rcs.core.ims.service.im.chat.imdn.ImdnDocument;
import com.orangelabs.rcs.core.ims.service.im.filetransfer.FileSharingError;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Terminating file transfer HTTP session
 * 
 * @author vfml3370
 */
public class TerminatingHttpFileSharingSession extends HttpFileTransferSession implements HttpTransferEventListener {

    /**
     * HTTP download manager
     */
    private HttpDownloadManager downloadManager;

    /**
     * ID of the incoming transfer message 
     */
    private String msgID;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param parent IMS service
     * @param invite initial INVITE
     * @param fileTransferInfo File transfer info
     * @param msgID Message ID
	 * @param chatSessionId Chat session ID
     */
    public TerminatingHttpFileSharingSession(ImsService parent, SipRequest invite, FileTransferHttpInfoDocument fileTransferInfo, String msgID, String chatSessionID) {
        super(parent, ContentManager.createMmContentFromMime(fileTransferInfo.getFileUrl(),
                fileTransferInfo.getFileType(), fileTransferInfo.getFileSize()), invite.getFromUri(), null, chatSessionID);

        this.msgID = msgID;

        // Create dialog path
        createTerminatingDialogPath(invite);

		// Instantiate the download manager
		downloadManager = new HttpDownloadManager(getContent(), this, fileTransferInfo.getFilename());
		
		// Download thumbnail
		if (fileTransferInfo.getFileThumbnail() != null) {
			setThumbnail(downloadManager.downloadThumbnail(fileTransferInfo.getFileThumbnail()));
		}
    }
    
    /**
     * Posts an interrupt request to this Thread
     */
    @Override
    public void interrupt(){
    	super.interrupt();
    	
    	// Interrupt the download
    	downloadManager.interrupt();
    }
    

    /**
     * Background processing
     */
    public void run() {
        try {
            if (logger.isActivated()) {
                logger.info("Initiate a new HTTP file transfer session as terminating");
            }
            
            // Check content
            if (getContent() == null) {
                if (logger.isActivated()) {
                    logger.debug("MIME type is not supported");
                }
                // Unsupported media type
                handleError(new FileSharingError(FileSharingError.UNSUPPORTED_MEDIA_TYPE));
                return;
            }

            if (RcsSettings.getInstance().isFileTransferAutoAccepted()) {
                if (logger.isActivated()) {
                    logger.debug("Auto accept file transfer invitation");
                }
            } else {
                if (logger.isActivated()) {
                    logger.debug("Accept manually file transfer invitation");
                }

                // Wait invitation answer
                int answer = waitInvitationAnswer();
                if (answer == ImsServiceSession.INVITATION_REJECTED) {
                    if (logger.isActivated()) {
                        logger.debug("Transfer has been rejected by user");
                    }

                    // Remove the current session
                    getImsService().removeSession(this);

                    // Notify listeners
                    for (int i = 0; i < getListeners().size(); i++) {
                        getListeners().get(i).handleSessionAborted(
                                ImsServiceSession.TERMINATION_BY_USER);
                    }
                    return;
                } else
                if (answer == ImsServiceSession.INVITATION_NOT_ANSWERED) {
                    if (logger.isActivated()) {
                        logger.debug("Transfer has been rejected on timeout");
                    }

                    // Remove the current session
                    getImsService().removeSession(this);

                    // Notify listeners
                    for (int j = 0; j < getListeners().size(); j++) {
                        getListeners().get(j).handleSessionAborted(
                                ImsServiceSession.TERMINATION_BY_TIMEOUT);
                    }
                    return;
                } else
                if (answer == ImsServiceSession.INVITATION_CANCELED) {
                    if (logger.isActivated()) {
                        logger.debug("Transfer has been canceled");
                    }
                    return;
                }
            }

            // Notify listeners
            for (int j = 0; j < getListeners().size(); j++) {
                getListeners().get(j).handleSessionStarted();
            }

            // Download file from the HTTP server
			if (downloadManager.downloadFile()) {
				if (logger.isActivated()){
					logger.debug("Download file with success");
				}

                // Set filename
                getContent().setUrl(downloadManager.getFilename());

                // File transfered
                handleFileTransfered();

                // Send SIP MESSAGE transfered
				if (msgID != null) {
                    String remoteInstanceId = null;
                    ContactHeader inviteContactHeader = (ContactHeader)getDialogPath().getInvite().getHeader(ContactHeader.NAME);
                    if (inviteContactHeader != null) {
                        remoteInstanceId = inviteContactHeader.getParameter(SipUtils.SIP_INSTANCE_PARAM);
                    }
					// Send message delivery status via a SIP MESSAGE
                    ((InstantMessagingService) getImsService()).getImdnManager().sendMessageDeliveryStatusImmediately(
                                    SipUtils.getAssertedIdentity(getDialogPath().getInvite()),
                                    msgID, ImdnDocument.DISPLAY, remoteInstanceId);
				}
			} else {
				if (downloadManager.isCancelled()) {
					return;
				}

				if (logger.isActivated()){
					logger.info("Download file has failed");
				}

                // Upload error
    			handleError(new FileSharingError(FileSharingError.MEDIA_DOWNLOAD_FAILED));
			}
        } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Transfer has failed", e);
        	}

        	// Unexpected error
			handleError(new FileSharingError(FileSharingError.UNEXPECTED_EXCEPTION, e.getMessage()));
		}
	}
}

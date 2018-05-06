package com.vishrant.cbsync.clipboard;

import com.vishrant.cbsync.socket.SocketConnection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vishrant
 */
public class ClipBoardListener extends Thread implements ClipboardOwner {

    private final Set<String> clipboardData = new HashSet<String>();
    private int syncInternal = 500;

    private final Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    private SocketConnection socketConnection;

    /**
     *
     * @param socketConnection
     * @param syncInternal
     */
    public ClipBoardListener(SocketConnection socketConnection, final int syncInternal) {
        this.socketConnection = socketConnection;

        if (syncInternal > 500) {
            this.syncInternal = syncInternal;
        }

    }

    private ClipBoardListener() {
        setName("Clipboard Listener");
    }

    @Override
    public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
        try {

            String tempText;
            final Transferable trans = contents;

            if (trans != null ? trans.isDataFlavorSupported(DataFlavor.stringFlavor) : false) {
                tempText = (String) trans.getTransferData(DataFlavor.stringFlavor);

                if (!this.clipboardData.contains(tempText)) {

                    this.clipboardData.clear();

                    if (this.clipboardData.add(tempText)) {
                        if (socketConnection != null) {
                            socketConnection.inform(tempText);
                        }
                    }
                }

                tempText = null;
            }

        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {

        while (true) {
            try {

                Thread.sleep(this.syncInternal);

                if (socketConnection.isConnected()) {

                    final Transferable trans = this.sysClip.getContents(this);
                    this.sysClip.setContents(trans, this);
                    this.lostOwnership(this.sysClip, trans);

                } else {
                    break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        this.clipboardData.clear();
        this.socketConnection = null;

    }
}

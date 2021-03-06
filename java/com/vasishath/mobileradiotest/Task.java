package com.vasishath.mobileradiotest;

import android.content.Context;

import com.vasishath.Mobielradiotest.net.StreamUtil;
import com.vasishath.mobileradiotest.util.MyLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLSocketFactory;

public class Task {
	private static final String TAG = "Task";

	private static final int CONNECT_TIMEOUT = 30 * 1000;
	private static final int DATA_TIMEOUT = 60 * 1000;

	private static final String SERVER = "imap.gmail.com";
	private static final int PORT = 993;

	public Task(Context context, int startId) {
		mContext = context;
	}

	public void execute() {
		try {
			testNetworking();
		} catch (Exception x) {
			MyLog.w(TAG, "Error in networking test", x);
		}
	}

	private void testNetworking() throws IOException {
		Socket socket = null;
		InputStream streamInput = null;
		OutputStream streamOutput = null;

		try {
			/*
			 * Log that we're starting up
			 */
			MyLog.i(TAG, "testNetworking begin");

			/*
			 * Connect at socket level
			 */
			final SSLSocketFactory socketFactory = getSSLSocketFactory();

			final long nTimeStart = System.currentTimeMillis();

			final InetSocketAddress sockAddr = new InetSocketAddress(SERVER, PORT);
			MyLog.i(TAG, "Trying: %s", sockAddr);

			socket = socketFactory.createSocket();
			try {
				socket.connect(sockAddr, CONNECT_TIMEOUT);
				MyLog.i(TAG, "Socket connection completed");
			} catch (IOException x) {
				StreamUtil.closeSocket(socket);
				socket = null;
				throw x;
			}

			if (socket == null || !socket.isConnected()) {
				throw new ConnectException("Could not connect to " + SERVER);
			}

			try {
				/*
				 * Get streams, this negotiates SSL
				 */
				socket.setSoTimeout(DATA_TIMEOUT);
				streamInput = socket.getInputStream();
				streamOutput = socket.getOutputStream();

				long nTimeConnect = System.currentTimeMillis() - nTimeStart;

				MyLog.i(TAG, "Connection to %s:%d completed: %s, time = %.2f sec", SERVER, PORT,
						socket.getRemoteSocketAddress(), nTimeConnect / 1000.0f);

			} catch (RuntimeException x) {
				throw new IOException(x);
			} catch (IOException x) {
				throw x;
			}

			try {
				/*
				 * Read IMAP greeting, send CAPABILITY and read response. Then close connection.
				 */
				final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(streamOutput,
						StandardCharsets.US_ASCII), 1024);
				final BufferedReader reader = new BufferedReader(new InputStreamReader(streamInput,
						StandardCharsets.US_ASCII), 8192);

				// Read greeting
				readImapResponse(reader, null);

				// Send "CAPABILITY" and read response
				sendImapCommand(writer, "k1", "CAPABILITY");
				readImapResponse(reader, "k1");

				/*
				 * Gracefully close connection
				 */
				sendImapCommand(writer, "k2", "LOGOUT");
				readImapResponse(reader, "k2");

			} catch (Exception x) {
				throw new IOException(x);
			}

		} finally {
			StreamUtil.closeSocket(socket);
			StreamUtil.closeStream(streamInput);
			StreamUtil.closeStream(streamOutput);

			/*
			 * Log that we're done
			 */
			MyLog.i(TAG, "testNetworking end");
		}
	}

	private void sendImapCommand(BufferedWriter w, String tag, String command) throws IOException {
		MyLog.i(TAG, "Send: %s %s", tag, command);
		final String s = tag + " " + command + "\r\n";
		w.write(s);
		w.flush();
	}

	private void readImapResponse(BufferedReader r, String tag) throws IOException {
		String s;
		while ((s = r.readLine()) != null) {
			MyLog.i(TAG, "Read: %s", s);
			if (tag == null || s.startsWith(tag)) {
				break;
			}
		}
	}

	private SSLSocketFactory getSSLSocketFactory() {
		synchronized (Task.class) {
			if (gFactory == null) {
				gFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			}
			return gFactory;
		}
	}

	private static SSLSocketFactory gFactory;

	@SuppressWarnings("unused")
	private Context mContext;
}

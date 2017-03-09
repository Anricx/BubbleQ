package com.chinaroad.bubble;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinaroad.bubble.adapter.BubbleHandlerAdapter;
import com.chinaroad.bubble.adapter.SessionAdapter;
import com.chinaroad.bubble.biz.BubbleBiz;
import com.chinaroad.bubble.filter.ProtoFilter;
import com.chinaroad.bubble.proto.Protocol;
import com.chinaroad.foundation.transfer.SocketAcceptor;
import com.chinaroad.foundation.transfer.session.Session;
import com.chinaroad.foundation.utils.NumberUtils;

public class Application {

	public static Map<String, Session> CLIENTS = new ConcurrentHashMap<String, Session>();	/* identifier => session */
	public static Map<String, LinkedList<Session>> TOPICS = new HashMap<String, LinkedList<Session>>();
	public static Map<String, LinkedList<Session>> LISTENERS = new HashMap<String, LinkedList<Session>>();
	
	private static Logger logger = LoggerFactory.getLogger(Application.class);
	private static String VERSION = "2.0.0 Beta";
	
	public static void main(String[] args) throws Exception {
		// create Options object
		Options options = new Options();
		// add t option
		options.addOption("b", "bind", true, "start server with specified address");
		options.addOption("p", "port", true, "start server with specified port");
        options.addOption("h", "help", false, "print this help message");
        options.addOption("v", "version", false, "print product version and exit");

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse( options, args);
		if(cmd.hasOption("h")) {
			HelpFormatter formater = new HelpFormatter();
			formater.printHelp("BubbleServer", options);
			System.exit(0);
		} else if (cmd.hasOption("v")) {
			System.out.println("BubbleServer Version \"" + VERSION + "\"");
			System.exit(0);
		}
		
		String host = cmd.getOptionValue("b", "0.0.0.0");
		int port = NumberUtils.toInt(cmd.getOptionValue("p"), 1883);
		
		SocketAcceptor acceptor = new SocketAcceptor(host, port);
		acceptor.getFilterChain().addLast("ProtoFilter", new ProtoFilter());
		
		// sometimes, you should init before...
		acceptor.init();
		
		// Custom setting...
		acceptor.setSelectTimeout(10000);
		acceptor.setSendBufferSize(1024);
		acceptor.setReceiveBufferSize(1024);
		acceptor.setReuseAddress(true);
		
		// Inject the biz handler.
		acceptor.setHandler(new BubbleHandler());
		
		logger.info("[Bubble][S] - Listening tcp://" + host + ":" + port + "...");
		
		// Start service
		// Thread will be blocked in here.
		acceptor.start();
	}
	
	static class BubbleHandler extends BubbleHandlerAdapter {
		
		private BubbleBiz bubbleBiz = new BubbleBiz();
		
		@Override
		public void dataReceived(Session session, Object data) throws Exception {
			Protocol protocol = (Protocol) data;

			if (Protocol.Type.HELLO == protocol.getType()) {  /* Client HELLO */
				StringBuffer identifier = new StringBuffer();
				Protocol.Hello status = bubbleBiz.hello(protocol, session, identifier); 
				switch (status) {
					case ACCEPTED:	/* Connect Accepted */
						// #Register Client~~~
						Application.CLIENTS.put(identifier.toString(), session);
						logger.info("[Bubble][H][" + SessionAdapter.clientAddress(session) + "] - Connect Accepted, Identifier \"" + identifier + "\".");
						break;
					
					/* Connect Refused */
					case UNACCEPTABLE_PROTOCOL:
						logger.warn("[Bubble][H][" + SessionAdapter.clientAddress(session) + "] - Connect Refused: The Server does not support the level of the BubbleQ protocol requested by the Client.");
						break;
					case IDENTIFIER_REJECTED:
						logger.warn("[Bubble][H][" + SessionAdapter.clientAddress(session) + "] - Connect Refused: The Client identifier is correct but not allowed by the Server.");
						break;
					case SERVER_UNAVAILABLE:
						logger.warn("[Bubble][H][" + SessionAdapter.clientAddress(session) + "] - Connect Refused: The Network Connection has been made but the BubbleQ service is unavailable.");
						break;
					case BAD_CERTIFICATES:
						logger.warn("[Bubble][H][" + SessionAdapter.clientAddress(session) + "] - Connect Refused: The data in the user name or password is malformed.");
						break;
					case NOT_AUTHORIZED:
						logger.warn("[Bubble][H][" + SessionAdapter.clientAddress(session) + "] - Connect Refused: The Client is not authorized to connect.");
						break;
					default:
						logger.warn("[Bubble][H][" + SessionAdapter.clientAddress(session) + "] - Connect Refused: Unkown Hello Result Status => " + status + ".");
						break;
				}
			} else if (Protocol.Type.SUBSCRIBE == protocol.getType()) {  /* Client SUBSCRIBE Topic */
				StringBuffer topic = new StringBuffer();
				Protocol.Subscribe status = bubbleBiz.subscribe(protocol, session, topic); 
				switch (status) {
					case ACCEPTED:	/* Subscribe Accepted */
						logger.info("[Bubble][S][" + SessionAdapter.clientAddress(session) + "] - Subscribe Topic:\"" + topic + "\" Accepted.");
						break;
					default:
						logger.warn("[Bubble][S][" + SessionAdapter.clientAddress(session) + "] - Subscribe Topic:\"" + topic + "\" Refused!");
						break;
				}
			} else if (Protocol.Type.LISTEN == protocol.getType()) {  /* Client LISTEN Topic */
				StringBuffer topic = new StringBuffer();
				Protocol.Listen status = bubbleBiz.listen(protocol, session, topic); 
				switch (status) {
					case ACCEPTED:	/* Listen Accepted */
						logger.info("[Bubble][L][" + SessionAdapter.clientAddress(session) + "] - Listen Topic:\"" + topic + "\" Accepted.");
						break;
					default:
						logger.warn("[Bubble][L][" + SessionAdapter.clientAddress(session) + "] - Listen Topic:\"" + topic + "\" Refused!");
						break;
				}
			} else if (Protocol.Type.PUBLISH == protocol.getType()) {  /* Client PUBLISH Topic */
				StringBuffer topic = new StringBuffer();
				StringBuffer msgid = new StringBuffer();
				Protocol.Publish status = bubbleBiz.publish(protocol, session, topic, msgid);
				switch (status) {
					case ACCEPTED:	/* Publish Accepted */
						logger.info("[Bubble][P][" + SessionAdapter.clientAddress(session) + "] - Publish Topic:\"" + topic + "\", msgid(" + msgid + ") Accepted.");
						break;
						
					default:
						logger.warn("[Bubble][P][" + SessionAdapter.clientAddress(session) + "] - Publish Topic:\"" + topic + "\", msgid(" + msgid + ") Refused!");
						break;
				}
			} else if (Protocol.Type.FEEDBACK == protocol.getType()) {	/* Client FEEDBACK */
				StringBuffer target = new StringBuffer();
				StringBuffer topic = new StringBuffer();
				StringBuffer msgid = new StringBuffer();
				Protocol.Feedback status = bubbleBiz.feedback(protocol, session, target, topic, msgid);
				switch (status) {
					case ACCEPTED:	/* Feedback Accepted */
						logger.info("[Bubble][F][" + SessionAdapter.clientAddress(session) + "] - Feedback Topic:\"" + topic + "\", msgid(" + msgid + "), Target:\"" + target + "\" Accepted.");
						break;
						
					default:
						logger.warn("[Bubble][F][" + SessionAdapter.clientAddress(session) + "] - Feedback Topic:\"" + topic + "\", msgid(" + msgid + "), Target:\"" + target + "\" Refused!");
						break;
				}
			} else if (Protocol.Type.PUSH == protocol.getType()) {  /* Client PUSH Topic */
				// Protocol.Push status = bubbleBiz.push(protocol, session);
			} else if (Protocol.Type.PING == protocol.getType()) {  /* Client PING */
				// 
			} else if (Protocol.Type.BYE == protocol.getType()) {  /* Client BYE */
				logger.info("[Bubble][B][" + SessionAdapter.clientAddress(session) + "] - Connection Release...");
				session.close();
			} else {
				logger.error("[Bubble][E][" + SessionAdapter.clientAddress(session) + "] - Type: " + protocol.getType() + " Unkown, Kick!");
				session.close();
			}			
		}
		
	}

}

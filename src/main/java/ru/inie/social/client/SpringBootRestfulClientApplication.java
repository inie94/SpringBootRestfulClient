package ru.inie.social.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class SpringBootRestfulClientApplication {
//	@Autowired
//	private WebSocketStompClient stompClient;

//	public static void main(String[] args) {
//		SpringApplication.run(SpringBootRestfulClientApplication.class, args);
//	}
	static final String URL_ROOT = "http://localhost:8080";
	static final String URL_WS = "ws://localhost:8080/ws";

	public static final String USER_NAME = "jjellico0@fema.gov";
	public static final String PASSWORD = "iM3M0VMjT";

	public static final RestTemplate restTemplate = new RestTemplate();

	public static HttpHeaders headers;
	private static String token;
//	private static List<String> cookie;

	private static WebSocketStompClient stompClient;
	private static MyStompSessionHandler sessionHandler;



	public static void main(String[] args) {

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
			System.out.print("Write your email: ");
			System.out.println();
			System.out.print("Write your username: ");
			while(true) {
				if (reader.ready()) {
//					String email = reader.readLine();
//					System.out.print("Write your password: ");
//					String password = reader.readLine();
//					User user = new User();
//					if(password.isEmpty() && email.isEmpty()) {
//						System.out.println("Default authorized");
//						user = authorizedUser(USER_NAME, PASSWORD);
//					} else {
//						user = authorizedUser(email, password);
//					}
//					System.out.println(user.toString());

					String username = reader.readLine();

					connectToWebSocket(URL_WS, username);

					ChatMessage message = new ChatMessage();
					message.setSender(username);
					message.setType(ChatMessage.MessageType.CHAT);
					message.setContent(reader.readLine());

					sessionHandler.getSession().send("/app/chat.sendMessage", message);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static User authorizedUser(String email, String password) throws JsonProcessingException {
		headers = createHeaders(email, password);

		HttpEntity<String> entity = new HttpEntity<>(headers);
		// Send request with GET method, and Headers.
		ResponseEntity<String> response = restTemplate.exchange(URL_ROOT + "/csrf",
				HttpMethod.GET, entity, String.class);

		token = response.getBody();

		response = restTemplate.exchange(URL_ROOT,
				HttpMethod.GET, entity, String.class);

		ObjectMapper mapper = new ObjectMapper();
		User user = mapper.readValue(response.getBody(), User.class);

		return user;
	}

	static HttpHeaders createHeaders(String username, String password){
		return new HttpHeaders() {{
			String auth = username + ":" + password;
			byte[] encodedAuth = Base64.encodeBase64(
					auth.getBytes(Charset.forName("US-ASCII")) );
			String authHeader = "Basic " + new String( encodedAuth );
			set( "Authorization", authHeader );
		}};
	}

	private static void connectToWebSocket(String url, String username) {

		List<Transport> transports = new ArrayList<>(2);

		StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
		transports.add(new WebSocketTransport(standardWebSocketClient));

		RestTemplateXhrTransport restTemplateXhrTransport = new RestTemplateXhrTransport();

		//setting the authentication token
//		HttpHeaders httpHeaders = new HttpHeaders();
//		httpHeaders.add("X-Auth-Token", token);
//		restTemplateXhrTransport.(httpHeaders);

		transports.add(restTemplateXhrTransport);

		WebSocketClient client = new SockJsClient(transports);

		stompClient = new WebSocketStompClient(client);
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());

		sessionHandler = new MyStompSessionHandler(username);
		stompClient.connect(url, sessionHandler);
	}

}

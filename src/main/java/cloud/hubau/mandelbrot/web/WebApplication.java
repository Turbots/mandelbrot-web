package cloud.hubau.mandelbrot.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
public class WebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}

	@Bean
	public UnicastProcessor<Event> eventPublisher() {
		return UnicastProcessor.create();
	}

	@Bean
	public Flux<Event> events(UnicastProcessor<Event> eventPublisher) {
		return eventPublisher
			.publish()
			.autoConnect();
	}

	@Bean
	public RouterFunction<ServerResponse> routes() {
		return RouterFunctions.route(
			GET("/"),
			request -> ServerResponse.ok().body(BodyInserters.fromResource(new ClassPathResource("index.html")))
		);
	}

	@Bean
	public HandlerMapping webSocketMapping(UnicastProcessor<Event> eventPublisher,
		Flux<Event> events) {
		Map<String, Object> map = new HashMap<>();
		map.put("/websocket/calculate", new ReactiveWebSocketHandler(eventPublisher, events));

		SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
		simpleUrlHandlerMapping.setUrlMap(map);

		// Order is required to avoid clashing with regular HTTP requests
		simpleUrlHandlerMapping.setOrder(10);

		return simpleUrlHandlerMapping;
	}

	@Bean
	public WebSocketHandlerAdapter handlerAdapter() {
		return new WebSocketHandlerAdapter();
	}
}

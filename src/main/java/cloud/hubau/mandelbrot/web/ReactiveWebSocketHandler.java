package cloud.hubau.mandelbrot.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

import java.io.IOException;

public class ReactiveWebSocketHandler implements WebSocketHandler {

	private UnicastProcessor<Event> eventPublisher;
	private Flux<String> outputEvents;
	private ObjectMapper mapper;

	public ReactiveWebSocketHandler(UnicastProcessor<Event> eventPublisher, Flux<Event> events) {
		this.eventPublisher = eventPublisher;
		this.mapper = new ObjectMapper();
		this.outputEvents = Flux.from(events)
			// only pass RESULT events to the clients
			.filter(event -> event.getType() == Event.Type.RESULT)
			.map(this::toJSON);
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		WebSocketMessageSubscriber subscriber = new WebSocketMessageSubscriber(eventPublisher);
		session.receive()
			.map(WebSocketMessage::getPayloadAsText)
			.map(this::toEvent)
			.subscribe(subscriber::onNext, subscriber::onError, subscriber::onComplete);
		return session.send(outputEvents.map(session::textMessage));
	}

	private Event toEvent(String json) {
		try {
			return mapper.readValue(json, Event.class);
		} catch (IOException e) {
			throw new RuntimeException("Invalid JSON:" + json, e);
		}
	}

	private String toJSON(Event event) {
		try {
			return mapper.writeValueAsString(event);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}

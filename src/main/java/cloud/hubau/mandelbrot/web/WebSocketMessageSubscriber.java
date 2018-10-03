package cloud.hubau.mandelbrot.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.UnicastProcessor;

@Slf4j class WebSocketMessageSubscriber {

	private static final int SIZE = 200;
	public static final String GRIDS_CHANNEL_INTERNAL_URL = "http://grids-channel.default.svc.cluster.local";

	private final UnicastProcessor<Event> eventPublisher;
	private final WebClient webClient;

	WebSocketMessageSubscriber(UnicastProcessor<Event> eventPublisher) {
		this.eventPublisher = eventPublisher;
		webClient = WebClient.builder().baseUrl(GRIDS_CHANNEL_INTERNAL_URL)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	void onNext(Event e) {
		if (e.getType() == Event.Type.CALCULATION) {
			log.info("Calling [{}] to calculate a [{},{}] grid", GRIDS_CHANNEL_INTERNAL_URL, SIZE, SIZE);
			webClient.post()
				.body(BodyInserters
					.fromObject(new Grid(e.getX1(), e.getY1(), e.getX2(), e.getY2(), SIZE, SIZE, e.getDepth())))
				.retrieve().bodyToFlux(ColoredPixel.class).log().subscribe();
		}
		eventPublisher.onNext(e);
	}

	void onError(Throwable error) {
		log.error("Error when sending over Websocket connection", error);
	}

	void onComplete() {
		log.info("Connection Completed");
	}
}

package cloud.hubau.mandelbrot.web;

import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

class WebSocketMessageSubscriber {

	private static final int SIZE = 400;

	private final UnicastProcessor<Event> eventPublisher;
	private final WebClient webClient;

	private static final Logger log = LoggerFactory.getLogger(WebSocketMessageSubscriber.class);

	WebSocketMessageSubscriber(UnicastProcessor<Event> eventPublisher) {
		this.eventPublisher = eventPublisher;
		webClient = WebClient.builder().baseUrl("http://mandelbrot.default.svc.cluster.local")
			.clientConnector(
				new ReactorClientHttpConnector(o -> o.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 600000)))
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	void onNext(Event e) {
		if (e.getType() == Event.Type.CALCULATION) {
			log.info("Generating Grid [({},{}),({},{})] with size [{},{}]", e.getX1(), e.getY1(), e.getX2(), e.getY2(),
				SIZE, SIZE);

			Flux<Integer> xFlux = Flux.range(0, SIZE);

			xFlux.zipWith(Flux.interval(Duration.ofMillis(100)))
				.map(t ->
					calculateColumn(e.getX1(), e.getY1(), e.getX2(), e.getY2(), t.getT1(), e.getDepth())
						.subscribe(p -> eventPublisher.onNext(
							new Event(Event.Type.RESULT, p.getX(), p.getY(), 0, 0, 0, p.getColor()))))
				.doOnComplete(() -> log.info("Done."))
				.subscribe();
		}
		eventPublisher.onNext(e);
	}

	private Flux<ColoredPixel> calculateColumn(double x1, double y1, double x2, double y2, int x, int depth) {
		log.info("Calculating column [{}]", x);

		double stepX = (x2 - x1) / SIZE;
		double stepY = (y2 - y1) / SIZE;
		double translatedX = x * stepX;
		double real = x1 + translatedX;

		List<Calculation> calculations = new ArrayList<>();
		for (int y = 0; y < SIZE; y++) {
			double translatedY = y * stepY;
			double imaginary = y1 + translatedY;

			calculations.add(new Calculation(real, imaginary, x, y, depth * 255));
		}

		return this.webClient
			.post()
			.body(BodyInserters.fromObject(calculations))
			.retrieve()
			.bodyToFlux(ColoredPixel.class);
	}

	void onError(Throwable error) {
		log.error("Error when sending over Websocket connection", error);
	}

	void onComplete() {
		log.info("Connection Completed");
	}
}

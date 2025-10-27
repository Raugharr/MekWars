// unittests/mekwars/server/util/discord/DiscordMessageHandlerTest.java
package mekwars.server.util.discord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
class DiscordMessageHandlerTest {
    private HttpClient http;

    @BeforeEach
    public void setup() {
        http = mock(HttpClient.class);
    }

    @Test
    public void testWebhookSend() throws Exception {
        @SuppressWarnings("unchecked")// We know the return type
        HttpResponse<String> resp = (HttpResponse<String>) mock(HttpResponse.class);

        when(resp.statusCode()).thenReturn(204);
        when(http.send(any(HttpRequest.class), any(BodyHandler.class)))
                .thenReturn(resp);

        String webhook = "https://fakediscord.com/webhook";
        DiscordMessageHandler handler = new DiscordMessageHandler(webhook, http);

        handler.post("mekwars update!");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(http, times(1)).send(requestCaptor.capture(), any());

        HttpRequest httpRequest = requestCaptor.getValue();
        assertEquals(URI.create(webhook), httpRequest.uri());
        assertEquals("POST", httpRequest.method());

        var ct = httpRequest.headers().firstValue("Content-Type");
        assertTrue(ct.isPresent());
        assertTrue(ct.get().startsWith("application/x-www-form-urlencoded"));
    }

    @Test
    public void testNoPostWithInvalidWebhooksSpaces() throws Exception {
        HttpClient http = mock(HttpClient.class);
        DiscordMessageHandler handler = new DiscordMessageHandler("   ", http);
        handler.post("ignored");
        verify(http, never()).send(any(), any());
    }

    @Test
    public void testNoPostWithInvalidWebhooksEmpty() throws Exception {
        HttpClient http = mock(HttpClient.class);
        DiscordMessageHandler handler = new DiscordMessageHandler("", http);
        handler.post("ignored");
        verify(http, never()).send(any(), any());
    }

    @Test
    public void testNoPostWithInvalidWebhooksNull() throws Exception {
        HttpClient http = mock(HttpClient.class);
        DiscordMessageHandler handler = new DiscordMessageHandler(null, http);
        handler.post("ignored");
        verify(http, never()).send(any(), any());
    }
}

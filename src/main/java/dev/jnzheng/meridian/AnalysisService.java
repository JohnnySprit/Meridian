package dev.jnzheng.meridian;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AnalysisService {

    private static final String UNAVAILABLE =
            "Analysis unavailable. Numbers and headlines are still from your account and Alpaca news.";

    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    public AnalysisService(OpenAiProperties openAiProperties, ObjectMapper objectMapper) {
        this.openAiProperties = openAiProperties;
        this.objectMapper = objectMapper;
    }

    public String analyze(
            List<PositionLine> positions,
            BigDecimal totalValue,
            BigDecimal totalPnl,
            BigDecimal volatility,
            String largestSymbol,
            BigDecimal largestWeight,
            List<Headline> headlines
    ) {
        if (openAiProperties.apiKey() == null || openAiProperties.apiKey().isBlank()) {
            return "Analysis unavailable (no OpenAI API key).";
        }
        if (positions == null || positions.isEmpty()) {
            return "No open positions. Nothing to analyze.";
        }

        try {
            String body = objectMapper.writeValueAsString(new ChatRequest(
                    model(),
                    List.of(
                            new ChatMessage("system", systemPrompt()),
                            new ChatMessage("user", userPrompt(
                                    positions,
                                    totalValue,
                                    totalPnl,
                                    volatility,
                                    largestSymbol,
                                    largestWeight,
                                    headlines
                            ))
                    )
            ));

            String responseJson = restClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + openAiProperties.apiKey())
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asString().isBlank()) {
                return UNAVAILABLE;
            }
            return content.asString();
        } catch (Exception e) {
            return UNAVAILABLE;
        }
    }

    private String model() {
        if (openAiProperties.model() == null || openAiProperties.model().isBlank()) {
            return "gpt-4o-mini";
        }
        return openAiProperties.model();
    }

    private static String systemPrompt() {
        return "Write a short portfolio briefing for a retail investing account. "
                + "This is not financial advice, make sure to mention it. "
                + "Use only the real numbers and real headlines provided, do not fabricate any numbers or headlines from your training. "
                + "If any one position is overexposing the account, then mention it. "
                + "Cite headlines by quoting a few words. "
                + "If there are no headlines/information for a stock in the portfolio, mention that. "
                + "Two or three short paragraphs. No bullet lists of tickers the user already sees.";
    }

    private static String userPrompt(
            List<PositionLine> positions,
            BigDecimal totalValue,
            BigDecimal totalPnl,
            BigDecimal volatility,
            String largestSymbol,
            BigDecimal largestWeight,
            List<Headline> headlines
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Totals: market value ").append(totalValue);
        sb.append(", unrealized P&L ").append(totalPnl);
        sb.append(", portfolio volatility (daily-return stddev, value-weighted) ").append(volatility);
        sb.append(".\n");
        if (largestSymbol != null) {
            sb.append("Largest position: ").append(largestSymbol);
            sb.append(" at weight ").append(largestWeight).append(".\n");
        }
        sb.append("Holdings:\n");
        for (int i = 0; i < positions.size(); i++) {
            PositionLine line = positions.get(i);
            sb.append("- ").append(line.symbol());
            sb.append(" qty=").append(line.qty());
            sb.append(" value=").append(line.marketValue());
            sb.append(" pnl=").append(line.pnl());
            sb.append(" weight=").append(line.weight());
            sb.append("\n");
        }
        sb.append("Headlines:\n");
        if (headlines == null || headlines.isEmpty()) {
            sb.append("(none)\n");
        } else {
            for (int i = 0; i < headlines.size(); i++) {
                Headline h = headlines.get(i);
                sb.append("- ").append(h.headline());
                sb.append(" (").append(h.source()).append(")\n");
            }
        }
        return sb.toString();
    }

    private record ChatRequest(String model, List<ChatMessage> messages) {}

    private record ChatMessage(String role, String content) {}
}
